package org.nostrj.client;

import org.nostrj.core.NostrEvent;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public interface RelayConnection extends AutoCloseable {
    
    CompletableFuture<Void> connect();
    
    void disconnect();
    
    boolean isConnected();
    
    URI getRelayUri();
    
    CompletableFuture<Void> sendEvent(NostrEvent event);
    
    String subscribe(NostrFilter filter, RelayEventListener listener);
    
    String subscribe(NostrFilter[] filters, RelayEventListener listener);
    
    void unsubscribe(String subscriptionId);
    
    void setMessageListener(RelayMessageListener listener);
    
    @Override
    void close();
}
