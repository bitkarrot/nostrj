package org.nostrj.server;

import org.nostrj.core.NostrEvent;

import java.util.List;

public interface EventStore extends AutoCloseable {
    
    void saveEvent(NostrEvent event) throws EventStoreException;
    
    NostrEvent getEventById(String eventId) throws EventStoreException;
    
    List<NostrEvent> queryEvents(EventQuery query) throws EventStoreException;
    
    boolean deleteEvent(String eventId) throws EventStoreException;
    
    long countEvents(EventQuery query) throws EventStoreException;
    
    void initialize() throws EventStoreException;
    
    @Override
    void close() throws EventStoreException;
}
