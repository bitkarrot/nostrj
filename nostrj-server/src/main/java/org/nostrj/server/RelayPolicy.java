package org.nostrj.server;

import org.nostrj.core.NostrEvent;

public interface RelayPolicy {
    boolean acceptEvent(NostrEvent event);
    
    boolean acceptKind(int kind);
    
    int getMaxContentLength();
    
    int getMaxTagsCount();
}
