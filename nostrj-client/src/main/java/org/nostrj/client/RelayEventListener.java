package org.nostrj.client;

import org.nostrj.core.NostrEvent;

@FunctionalInterface
public interface RelayEventListener {
    void onEvent(String subscriptionId, NostrEvent event);
}
