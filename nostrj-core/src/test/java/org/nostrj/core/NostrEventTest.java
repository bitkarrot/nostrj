package org.nostrj.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NostrEventTest {

    @Test
    void testCreateAndSignEvent() {
        NostrKeys keys = NostrKeys.generate();
        
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Hello Nostr!")
                .buildAndSign(keys);
        
        assertNotNull(event.getId());
        assertNotNull(event.getSig());
        assertEquals(keys.getPublicKeyHex(), event.getPubkey());
        assertEquals(NostrKind.TEXT_NOTE, event.getKind());
        assertEquals("Hello Nostr!", event.getContent());
    }

    @Test
    void testVerifySignedEvent() {
        NostrKeys keys = NostrKeys.generate();
        
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Test message")
                .buildAndSign(keys);
        
        assertTrue(NostrSigner.verify(event));
    }

    @Test
    void testVerifyFailsWithTamperedContent() {
        NostrKeys keys = NostrKeys.generate();
        
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Original message")
                .buildAndSign(keys);
        
        event.setContent("Tampered message");
        
        assertFalse(NostrSigner.verify(event));
    }

    @Test
    void testEventWithTags() {
        NostrKeys keys = NostrKeys.generate();
        
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Tagged message")
                .tag("e", "event-id-here")
                .tag("p", "pubkey-here")
                .buildAndSign(keys);
        
        assertEquals(2, event.getTags().size());
        assertEquals("e", event.getTags().get(0).get(0));
        assertEquals("p", event.getTags().get(1).get(0));
        assertTrue(NostrSigner.verify(event));
    }

    @Test
    void testCustomCreatedAt() {
        NostrKeys keys = NostrKeys.generate();
        long customTime = Instant.parse("2024-01-01T00:00:00Z").getEpochSecond();
        
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Time travel")
                .createdAt(customTime)
                .buildAndSign(keys);
        
        assertEquals(customTime, event.getCreatedAt());
        assertTrue(NostrSigner.verify(event));
    }
}
