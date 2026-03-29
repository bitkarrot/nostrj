package org.nostrj.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nostrj.core.NostrEvent;
import org.nostrj.core.NostrSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RelayHandler {
    private static final Logger log = LoggerFactory.getLogger(RelayHandler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final EventStore eventStore;
    private final Map<String, Subscription> subscriptions;
    private final RelayPolicy policy;

    public RelayHandler(EventStore eventStore, RelayPolicy policy) {
        this.eventStore = eventStore;
        this.policy = policy;
        this.subscriptions = new ConcurrentHashMap<>();
    }

    public RelayHandler(EventStore eventStore) {
        this(eventStore, new DefaultRelayPolicy());
    }

    public String handleMessage(String sessionId, String message) {
        try {
            JsonNode node = MAPPER.readTree(message);
            if (!node.isArray() || node.size() < 1) {
                return createNotice("Invalid message format");
            }

            String type = node.get(0).asText();
            
            return switch (type) {
                case "EVENT" -> handleEvent(node);
                case "REQ" -> handleReq(sessionId, node);
                case "CLOSE" -> handleClose(sessionId, node);
                default -> createNotice("Unknown message type: " + type);
            };
        } catch (Exception e) {
            log.error("Error handling message", e);
            return createNotice("Error processing message: " + e.getMessage());
        }
    }

    private String handleEvent(JsonNode node) throws JsonProcessingException, EventStoreException {
        if (node.size() < 2) {
            return createNotice("Invalid EVENT message");
        }

        NostrEvent event = MAPPER.treeToValue(node.get(1), NostrEvent.class);
        
        if (!policy.acceptEvent(event)) {
            return createOk(event.getId(), false, "blocked: event rejected by policy");
        }

        if (!NostrSigner.verify(event)) {
            return createOk(event.getId(), false, "invalid: signature verification failed");
        }

        try {
            eventStore.saveEvent(event);
            return createOk(event.getId(), true, "");
        } catch (EventStoreException e) {
            log.error("Failed to save event", e);
            return createOk(event.getId(), false, "error: failed to save event");
        }
    }

    private String handleReq(String sessionId, JsonNode node) throws JsonProcessingException, EventStoreException {
        if (node.size() < 2) {
            return createNotice("Invalid REQ message");
        }

        String subscriptionId = node.get(1).asText();
        List<EventQuery> queries = new ArrayList<>();

        for (int i = 2; i < node.size(); i++) {
            EventQuery query = parseFilter(node.get(i));
            queries.add(query);
        }

        String subKey = sessionId + ":" + subscriptionId;
        Subscription subscription = new Subscription(subscriptionId, queries);
        subscriptions.put(subKey, subscription);

        StringBuilder response = new StringBuilder();
        for (EventQuery query : queries) {
            List<NostrEvent> events = eventStore.queryEvents(query);
            for (NostrEvent event : events) {
                response.append(createEvent(subscriptionId, event)).append("\n");
            }
        }

        response.append(createEose(subscriptionId));
        return response.toString();
    }

    private String handleClose(String sessionId, JsonNode node) {
        if (node.size() < 2) {
            return createNotice("Invalid CLOSE message");
        }

        String subscriptionId = node.get(1).asText();
        String subKey = sessionId + ":" + subscriptionId;
        subscriptions.remove(subKey);
        
        return null;
    }

    public void removeSession(String sessionId) {
        subscriptions.keySet().removeIf(key -> key.startsWith(sessionId + ":"));
    }

    private EventQuery parseFilter(JsonNode filterNode) throws JsonProcessingException {
        EventQuery.Builder builder = EventQuery.builder();

        if (filterNode.has("ids")) {
            List<String> ids = new ArrayList<>();
            filterNode.get("ids").forEach(id -> ids.add(id.asText()));
            builder.ids(ids);
        }

        if (filterNode.has("authors")) {
            List<String> authors = new ArrayList<>();
            filterNode.get("authors").forEach(author -> authors.add(author.asText()));
            builder.authors(authors);
        }

        if (filterNode.has("kinds")) {
            List<Integer> kinds = new ArrayList<>();
            filterNode.get("kinds").forEach(kind -> kinds.add(kind.asInt()));
            builder.kinds(kinds);
        }

        if (filterNode.has("#e")) {
            List<String> eventTags = new ArrayList<>();
            filterNode.get("#e").forEach(tag -> eventTags.add(tag.asText()));
            builder.eventTags(eventTags);
        }

        if (filterNode.has("#p")) {
            List<String> pubkeyTags = new ArrayList<>();
            filterNode.get("#p").forEach(tag -> pubkeyTags.add(tag.asText()));
            builder.pubkeyTags(pubkeyTags);
        }

        if (filterNode.has("since")) {
            builder.since(filterNode.get("since").asLong());
        }

        if (filterNode.has("until")) {
            builder.until(filterNode.get("until").asLong());
        }

        if (filterNode.has("limit")) {
            builder.limit(filterNode.get("limit").asInt());
        }

        return builder.build();
    }

    private String createOk(String eventId, boolean accepted, String message) {
        try {
            Object[] response = new Object[]{"OK", eventId, accepted, message};
            return MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to create OK message", e);
            return "";
        }
    }

    private String createEvent(String subscriptionId, NostrEvent event) {
        try {
            Object[] response = new Object[]{"EVENT", subscriptionId, event};
            return MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to create EVENT message", e);
            return "";
        }
    }

    private String createEose(String subscriptionId) {
        try {
            Object[] response = new Object[]{"EOSE", subscriptionId};
            return MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to create EOSE message", e);
            return "";
        }
    }

    private String createNotice(String message) {
        try {
            Object[] response = new Object[]{"NOTICE", message};
            return MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to create NOTICE message", e);
            return "";
        }
    }

    private static class Subscription {
        private final String id;
        private final List<EventQuery> queries;

        public Subscription(String id, List<EventQuery> queries) {
            this.id = id;
            this.queries = queries;
        }

        public String getId() {
            return id;
        }

        public List<EventQuery> getQueries() {
            return queries;
        }
    }
}
