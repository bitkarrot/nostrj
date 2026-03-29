package org.nostrj.client;

public interface RelayMessageListener {
    void onMessage(NostrMessage message);
    
    void onNotice(String notice);
    
    void onEose(String subscriptionId);
    
    void onOk(String eventId, boolean accepted, String message);
}
