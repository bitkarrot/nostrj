package org.nostrj.relay.config;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.nostrj.server.EventStore;
import org.nostrj.server.EventStoreException;
import org.nostrj.server.SqliteEventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class EventStoreFactory {
    private static final Logger log = LoggerFactory.getLogger(EventStoreFactory.class);

    @Singleton
    public EventStore eventStore(RelayConfiguration config) throws EventStoreException {
        log.info("Initializing event store at: {}", config.getDbPath());
        SqliteEventStore store = new SqliteEventStore(config.getDbPath());
        store.initialize();
        return store;
    }
}
