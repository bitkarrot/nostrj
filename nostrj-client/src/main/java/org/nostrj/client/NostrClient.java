package org.nostrj.client;

import org.nostrj.core.NostrEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NostrClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(NostrClient.class);
    
    private final Map<URI, RelayConnection> connections;

    public NostrClient() {
        this.connections = new ConcurrentHashMap<>();
    }

    public CompletableFuture<RelayConnection> connectToRelay(String relayUrl) {
        return connectToRelay(URI.create(relayUrl));
    }

    public CompletableFuture<RelayConnection> connectToRelay(URI relayUri) {
        RelayConnection existing = connections.get(relayUri);
        if (existing != null && existing.isConnected()) {
            return CompletableFuture.completedFuture(existing);
        }

        RelayConnection connection = new WebSocketRelayConnection(relayUri);
        connections.put(relayUri, connection);
        
        return connection.connect().thenApply(v -> connection);
    }

    public void disconnectFromRelay(URI relayUri) {
        RelayConnection connection = connections.remove(relayUri);
        if (connection != null) {
            connection.disconnect();
        }
    }

    public RelayConnection getConnection(URI relayUri) {
        return connections.get(relayUri);
    }

    public List<RelayConnection> getAllConnections() {
        return new ArrayList<>(connections.values());
    }

    public CompletableFuture<Void> publishEvent(NostrEvent event, URI... relays) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (URI relay : relays) {
            RelayConnection connection = connections.get(relay);
            if (connection != null && connection.isConnected()) {
                futures.add(connection.sendEvent(event));
            } else {
                log.warn("Not connected to relay: {}", relay);
            }
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public void subscribeToAll(NostrFilter filter, RelayEventListener listener) {
        for (RelayConnection connection : connections.values()) {
            if (connection.isConnected()) {
                connection.subscribe(filter, listener);
            }
        }
    }

    @Override
    public void close() {
        for (RelayConnection connection : connections.values()) {
            connection.close();
        }
        connections.clear();
    }
}
