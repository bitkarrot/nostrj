package org.nostrj.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nostrj.core.NostrEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteEventStore implements EventStore {
    private static final Logger log = LoggerFactory.getLogger(SqliteEventStore.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final String dbPath;
    private Connection connection;

    public SqliteEventStore(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public void initialize() throws EventStoreException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTables();
            log.info("SQLite event store initialized at: {}", dbPath);
        } catch (SQLException e) {
            throw new EventStoreException("Failed to initialize database", e);
        }
    }

    private void createTables() throws SQLException {
        String createEventsTable = """
            CREATE TABLE IF NOT EXISTS events (
                id TEXT PRIMARY KEY,
                pubkey TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                kind INTEGER NOT NULL,
                tags TEXT NOT NULL,
                content TEXT NOT NULL,
                sig TEXT NOT NULL,
                indexed_at INTEGER NOT NULL
            )
            """;

        String createTagsTable = """
            CREATE TABLE IF NOT EXISTS event_tags (
                event_id TEXT NOT NULL,
                tag_name TEXT NOT NULL,
                tag_value TEXT NOT NULL,
                FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
            )
            """;

        String createIndexes = """
            CREATE INDEX IF NOT EXISTS idx_events_pubkey ON events(pubkey);
            CREATE INDEX IF NOT EXISTS idx_events_kind ON events(kind);
            CREATE INDEX IF NOT EXISTS idx_events_created_at ON events(created_at);
            CREATE INDEX IF NOT EXISTS idx_event_tags_name_value ON event_tags(tag_name, tag_value);
            CREATE INDEX IF NOT EXISTS idx_event_tags_event_id ON event_tags(event_id);
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createEventsTable);
            stmt.execute(createTagsTable);
            for (String index : createIndexes.split(";")) {
                if (!index.trim().isEmpty()) {
                    stmt.execute(index);
                }
            }
        }
    }

    @Override
    public void saveEvent(NostrEvent event) throws EventStoreException {
        String insertEvent = """
            INSERT OR REPLACE INTO events (id, pubkey, created_at, kind, tags, content, sig, indexed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String deleteOldTags = "DELETE FROM event_tags WHERE event_id = ?";
        String insertTag = "INSERT INTO event_tags (event_id, tag_name, tag_value) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(insertEvent)) {
                stmt.setString(1, event.getId());
                stmt.setString(2, event.getPubkey());
                stmt.setLong(3, event.getCreatedAt());
                stmt.setInt(4, event.getKind());
                stmt.setString(5, MAPPER.writeValueAsString(event.getTags()));
                stmt.setString(6, event.getContent());
                stmt.setString(7, event.getSig());
                stmt.setLong(8, System.currentTimeMillis() / 1000);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(deleteOldTags)) {
                stmt.setString(1, event.getId());
                stmt.executeUpdate();
            }

            if (event.getTags() != null && !event.getTags().isEmpty()) {
                try (PreparedStatement stmt = connection.prepareStatement(insertTag)) {
                    for (List<String> tag : event.getTags()) {
                        if (tag.size() >= 2) {
                            stmt.setString(1, event.getId());
                            stmt.setString(2, tag.get(0));
                            stmt.setString(3, tag.get(1));
                            stmt.addBatch();
                        }
                    }
                    stmt.executeBatch();
                }
            }

            connection.commit();
        } catch (SQLException | JsonProcessingException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                log.error("Failed to rollback transaction", ex);
            }
            throw new EventStoreException("Failed to save event", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.error("Failed to reset auto-commit", e);
            }
        }
    }

    @Override
    public NostrEvent getEventById(String eventId) throws EventStoreException {
        String query = "SELECT id, pubkey, created_at, kind, tags, content, sig FROM events WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, eventId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvent(rs);
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            throw new EventStoreException("Failed to get event by ID", e);
        }

        return null;
    }

    @Override
    public List<NostrEvent> queryEvents(EventQuery query) throws EventStoreException {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT e.id, e.pubkey, e.created_at, e.kind, e.tags, e.content, e.sig FROM events e");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (query.getEventTags() != null || query.getPubkeyTags() != null) {
            sql.append(" LEFT JOIN event_tags et ON e.id = et.event_id");
        }

        sql.append(" WHERE 1=1");

        if (query.getIds() != null && !query.getIds().isEmpty()) {
            conditions.add("e.id IN (" + placeholders(query.getIds().size()) + ")");
            params.addAll(query.getIds());
        }

        if (query.getAuthors() != null && !query.getAuthors().isEmpty()) {
            conditions.add("e.pubkey IN (" + placeholders(query.getAuthors().size()) + ")");
            params.addAll(query.getAuthors());
        }

        if (query.getKinds() != null && !query.getKinds().isEmpty()) {
            conditions.add("e.kind IN (" + placeholders(query.getKinds().size()) + ")");
            params.addAll(query.getKinds());
        }

        if (query.getEventTags() != null && !query.getEventTags().isEmpty()) {
            conditions.add("(et.tag_name = 'e' AND et.tag_value IN (" + placeholders(query.getEventTags().size()) + "))");
            params.addAll(query.getEventTags());
        }

        if (query.getPubkeyTags() != null && !query.getPubkeyTags().isEmpty()) {
            conditions.add("(et.tag_name = 'p' AND et.tag_value IN (" + placeholders(query.getPubkeyTags().size()) + "))");
            params.addAll(query.getPubkeyTags());
        }

        if (query.getSince() != null) {
            conditions.add("e.created_at >= ?");
            params.add(query.getSince());
        }

        if (query.getUntil() != null) {
            conditions.add("e.created_at <= ?");
            params.add(query.getUntil());
        }

        for (String condition : conditions) {
            sql.append(" AND ").append(condition);
        }

        sql.append(" ORDER BY e.created_at DESC");

        if (query.getLimit() != null) {
            sql.append(" LIMIT ?");
            params.add(query.getLimit());
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            List<NostrEvent> events = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToEvent(rs));
                }
            }
            return events;
        } catch (SQLException | JsonProcessingException e) {
            throw new EventStoreException("Failed to query events", e);
        }
    }

    @Override
    public boolean deleteEvent(String eventId) throws EventStoreException {
        String sql = "DELETE FROM events WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, eventId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new EventStoreException("Failed to delete event", e);
        }
    }

    @Override
    public long countEvents(EventQuery query) throws EventStoreException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT e.id) FROM events e");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (query.getEventTags() != null || query.getPubkeyTags() != null) {
            sql.append(" LEFT JOIN event_tags et ON e.id = et.event_id");
        }

        sql.append(" WHERE 1=1");

        if (query.getAuthors() != null && !query.getAuthors().isEmpty()) {
            conditions.add("e.pubkey IN (" + placeholders(query.getAuthors().size()) + ")");
            params.addAll(query.getAuthors());
        }

        if (query.getKinds() != null && !query.getKinds().isEmpty()) {
            conditions.add("e.kind IN (" + placeholders(query.getKinds().size()) + ")");
            params.addAll(query.getKinds());
        }

        for (String condition : conditions) {
            sql.append(" AND ").append(condition);
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new EventStoreException("Failed to count events", e);
        }

        return 0;
    }

    @Override
    public void close() throws EventStoreException {
        if (connection != null) {
            try {
                connection.close();
                log.info("SQLite event store closed");
            } catch (SQLException e) {
                throw new EventStoreException("Failed to close database connection", e);
            }
        }
    }

    private NostrEvent mapResultSetToEvent(ResultSet rs) throws SQLException, JsonProcessingException {
        NostrEvent event = new NostrEvent();
        event.setId(rs.getString("id"));
        event.setPubkey(rs.getString("pubkey"));
        event.setCreatedAt(rs.getLong("created_at"));
        event.setKind(rs.getInt("kind"));
        event.setTags(MAPPER.readValue(rs.getString("tags"), new TypeReference<List<List<String>>>() {}));
        event.setContent(rs.getString("content"));
        event.setSig(rs.getString("sig"));
        return event;
    }

    private String placeholders(int count) {
        return String.join(",", "?".repeat(count).split(""));
    }
}
