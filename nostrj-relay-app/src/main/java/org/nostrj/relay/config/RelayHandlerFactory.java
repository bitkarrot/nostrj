package org.nostrj.relay.config;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.nostrj.server.EventStore;
import org.nostrj.server.RelayHandler;

@Factory
public class RelayHandlerFactory {

    @Singleton
    public RelayHandler relayHandler(EventStore eventStore) {
        return new RelayHandler(eventStore);
    }
}
