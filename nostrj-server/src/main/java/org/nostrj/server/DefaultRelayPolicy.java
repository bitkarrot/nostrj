package org.nostrj.server;

import org.nostrj.core.NostrEvent;

public class DefaultRelayPolicy implements RelayPolicy {
    private static final int MAX_CONTENT_LENGTH = 100_000;
    private static final int MAX_TAGS_COUNT = 2000;

    @Override
    public boolean acceptEvent(NostrEvent event) {
        if (event.getContent() != null && event.getContent().length() > MAX_CONTENT_LENGTH) {
            return false;
        }
        
        if (event.getTags() != null && event.getTags().size() > MAX_TAGS_COUNT) {
            return false;
        }
        
        return acceptKind(event.getKind());
    }

    @Override
    public boolean acceptKind(int kind) {
        return true;
    }

    @Override
    public int getMaxContentLength() {
        return MAX_CONTENT_LENGTH;
    }

    @Override
    public int getMaxTagsCount() {
        return MAX_TAGS_COUNT;
    }
}
