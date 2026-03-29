package org.nostrj.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.nostrj.core.NostrEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketRelayConnection implements RelayConnection {
    private static final Logger log = LoggerFactory.getLogger(WebSocketRelayConnection.class);
    
    private final URI relayUri;
    private final WebSocketClient client;
    private final Map<String, RelayEventListener> subscriptions;
    private RelayMessageListener messageListener;
    private CompletableFuture<Void> connectFuture;

    public WebSocketRelayConnection(URI relayUri) {
        this.relayUri = relayUri;
        this.subscriptions = new ConcurrentHashMap<>();
        this.client = new WebSocketClient(relayUri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                log.info("Connected to relay: {}", relayUri);
                if (connectFuture != null) {
                    connectFuture.complete(null);
                }
            }

            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("Disconnected from relay: {} (code: {}, reason: {})", relayUri, code, reason);
            }

            @Override
            public void onError(Exception ex) {
                log.error("WebSocket error for relay: {}", relayUri, ex);
                if (connectFuture != null && !connectFuture.isDone()) {
                    connectFuture.completeExceptionally(ex);
                }
            }
        };
    }

    @Override
    public CompletableFuture<Void> connect() {
        connectFuture = new CompletableFuture<>();
        client.connect();
        return connectFuture;
    }

    @Override
    public void disconnect() {
        client.close();
    }

    @Override
    public boolean isConnected() {
        return client.isOpen();
    }

    @Override
    public URI getRelayUri() {
        return relayUri;
    }

    @Override
    public CompletableFuture<Void> sendEvent(NostrEvent event) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            String message = NostrMessage.createEventMessage(event);
            client.send(message);
            future.complete(null);
        } catch (JsonProcessingException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public String subscribe(NostrFilter filter, RelayEventListener listener) {
        return subscribe(new NostrFilter[]{filter}, listener);
    }

    @Override
    public String subscribe(NostrFilter[] filters, RelayEventListener listener) {
        String subscriptionId = UUID.randomUUID().toString();
        subscriptions.put(subscriptionId, listener);
        
        try {
            String message = NostrMessage.createReqMessage(subscriptionId, filters);
            client.send(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to create subscription message", e);
            subscriptions.remove(subscriptionId);
            throw new RuntimeException("Failed to subscribe", e);
        }
        
        return subscriptionId;
    }

    @Override
    public void unsubscribe(String subscriptionId) {
        subscriptions.remove(subscriptionId);
        try {
            String message = NostrMessage.createCloseMessage(subscriptionId);
            client.send(message);
        } catch (JsonProcessingException e) {
            log.error("Failed to create close message", e);
        }
    }

    @Override
    public void setMessageListener(RelayMessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void close() {
        disconnect();
    }

    private void handleMessage(String message) {
        try {
            NostrMessage nostrMessage = NostrMessage.parse(message);
            
            if (messageListener != null) {
                messageListener.onMessage(nostrMessage);
            }
            
            switch (nostrMessage.getType()) {
                case EVENT:
                    handleEvent(nostrMessage);
                    break;
                case EOSE:
                    handleEose(nostrMessage);
                    break;
                case OK:
                    handleOk(nostrMessage);
                    break;
                case NOTICE:
                    handleNotice(nostrMessage);
                    break;
                default:
                    log.debug("Received message type: {}", nostrMessage.getType());
            }
        } catch (Exception e) {
            log.error("Failed to handle message: {}", message, e);
        }
    }

    private void handleEvent(NostrMessage message) throws JsonProcessingException {
        String subscriptionId = message.getSubscriptionId();
        NostrEvent event = message.getEvent();
        
        RelayEventListener listener = subscriptions.get(subscriptionId);
        if (listener != null && event != null) {
            listener.onEvent(subscriptionId, event);
        }
    }

    private void handleEose(NostrMessage message) {
        if (messageListener != null) {
            messageListener.onEose(message.getSubscriptionId());
        }
    }

    private void handleOk(NostrMessage message) {
        if (messageListener != null) {
            String eventId = message.getSubscriptionId();
            boolean accepted = message.isOk();
            String okMessage = message.getOkMessage();
            messageListener.onOk(eventId, accepted, okMessage);
        }
    }

    private void handleNotice(NostrMessage message) {
        if (messageListener != null) {
            messageListener.onNotice(message.getNotice());
        }
    }
}
