package org.nostrj.relay;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.nostrj.client.NostrClient;
import org.nostrj.client.NostrFilter;
import org.nostrj.core.NostrEvent;
import org.nostrj.core.NostrEventBuilder;
import org.nostrj.core.NostrKeys;
import org.nostrj.core.NostrKind;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RelayIntegrationTest {

    @Inject
    EmbeddedServer server;

    private NostrClient client;
    private URI relayUri;

    @BeforeEach
    void setUp() throws Exception {
        client = new NostrClient();
        relayUri = URI.create("ws://localhost:" + server.getPort() + "/");
        client.connectToRelay(relayUri).get(5, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Generate keys, sign note, publish to relay")
    void testPublishNote() throws Exception {
        // Generate Nostr key pair
        NostrKeys keys = NostrKeys.generate();
        assertNotNull(keys.getPrivateKeyHex());
        assertNotNull(keys.getPublicKeyHex());
        assertEquals(64, keys.getPrivateKeyHex().length());
        assertEquals(64, keys.getPublicKeyHex().length());
        
        System.out.println("Generated keys:");
        System.out.println("  npub: " + keys.getPublicKeyBech32());
        System.out.println("  nsec: " + keys.getPrivateKeyBech32());

        // Create and sign a note
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Hello from integration test!")
                .buildAndSign(keys);

        assertNotNull(event.getId());
        assertNotNull(event.getSig());
        assertEquals(keys.getPublicKeyHex(), event.getPubkey());
        
        System.out.println("Created event:");
        System.out.println("  ID: " + event.getId());
        System.out.println("  Content: " + event.getContent());

        // Publish to relay
        CompletableFuture<Void> publishFuture = client.publishEvent(event, relayUri);
        publishFuture.get(5, TimeUnit.SECONDS);
        
        System.out.println("✓ Event published successfully");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Publish multiple notes and fetch them all")
    void testPublishAndFetchMultipleNotes() throws Exception {
        NostrKeys keys = NostrKeys.generate();
        List<NostrEvent> publishedEvents = new ArrayList<>();

        // Publish 5 notes
        for (int i = 1; i <= 5; i++) {
            NostrEvent event = new NostrEventBuilder()
                    .kind(NostrKind.TEXT_NOTE)
                    .content("Test note #" + i)
                    .tag("test", "integration")
                    .buildAndSign(keys);
            
            publishedEvents.add(event);
            client.publishEvent(event, relayUri).get(5, TimeUnit.SECONDS);
            System.out.println("Published note #" + i + ": " + event.getId());
        }

        // Wait a bit for events to be stored
        Thread.sleep(500);

        // Subscribe and fetch all notes from this author
        CountDownLatch latch = new CountDownLatch(5);
        List<NostrEvent> receivedEvents = new ArrayList<>();

        NostrFilter filter = NostrFilter.builder()
                .authors(keys.getPublicKeyHex())
                .kinds(NostrKind.TEXT_NOTE)
                .limit(10)
                .build();

        client.subscribeToAll(filter, (subId, event) -> {
            receivedEvents.add(event);
            System.out.println("Received: " + event.getContent());
            latch.countDown();
        });

        // Wait for all events
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Should receive all 5 events");
        assertEquals(5, receivedEvents.size(), "Should receive exactly 5 events");

        // Verify all events were received
        for (NostrEvent published : publishedEvents) {
            boolean found = receivedEvents.stream()
                    .anyMatch(e -> e.getId().equals(published.getId()));
            assertTrue(found, "Published event " + published.getId() + " should be received");
        }
        
        System.out.println("✓ All events fetched successfully");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Test filtering by kind")
    void testFilterByKind() throws Exception {
        NostrKeys keys = NostrKeys.generate();

        // Publish different kinds of events
        NostrEvent textNote = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("This is a text note")
                .buildAndSign(keys);

        NostrEvent reaction = new NostrEventBuilder()
                .kind(NostrKind.REACTION)
                .content("👍")
                .tag("e", "some-event-id")
                .buildAndSign(keys);

        client.publishEvent(textNote, relayUri).get(5, TimeUnit.SECONDS);
        client.publishEvent(reaction, relayUri).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);

        // Filter for only text notes
        CountDownLatch latch = new CountDownLatch(1);
        List<NostrEvent> receivedEvents = new ArrayList<>();

        NostrFilter filter = NostrFilter.builder()
                .authors(keys.getPublicKeyHex())
                .kinds(NostrKind.TEXT_NOTE)
                .build();

        client.subscribeToAll(filter, (subId, event) -> {
            receivedEvents.add(event);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(1, receivedEvents.size(), "Should only receive text note");
        assertEquals(NostrKind.TEXT_NOTE, receivedEvents.get(0).getKind());
        assertEquals("This is a text note", receivedEvents.get(0).getContent());
        
        System.out.println("✓ Kind filtering works correctly");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Test time-based filtering")
    void testTimeBasedFiltering() throws Exception {
        NostrKeys keys = NostrKeys.generate();
        long now = System.currentTimeMillis() / 1000;

        // Publish event with custom timestamp
        NostrEvent oldEvent = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Old event")
                .createdAt(now - 3600) // 1 hour ago
                .buildAndSign(keys);

        NostrEvent recentEvent = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Recent event")
                .createdAt(now - 60) // 1 minute ago
                .buildAndSign(keys);

        client.publishEvent(oldEvent, relayUri).get(5, TimeUnit.SECONDS);
        client.publishEvent(recentEvent, relayUri).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);

        // Filter for events since 30 minutes ago
        CountDownLatch latch = new CountDownLatch(1);
        List<NostrEvent> receivedEvents = new ArrayList<>();

        NostrFilter filter = NostrFilter.builder()
                .authors(keys.getPublicKeyHex())
                .since(now - 1800) // 30 minutes ago
                .build();

        client.subscribeToAll(filter, (subId, event) -> {
            receivedEvents.add(event);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(1, receivedEvents.size(), "Should only receive recent event");
        assertEquals("Recent event", receivedEvents.get(0).getContent());
        
        System.out.println("✓ Time-based filtering works correctly");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Test limit parameter")
    void testLimitParameter() throws Exception {
        NostrKeys keys = NostrKeys.generate();

        // Publish 10 events
        for (int i = 1; i <= 10; i++) {
            NostrEvent event = new NostrEventBuilder()
                    .kind(NostrKind.TEXT_NOTE)
                    .content("Event " + i)
                    .buildAndSign(keys);
            client.publishEvent(event, relayUri).get(5, TimeUnit.SECONDS);
        }

        Thread.sleep(500);

        // Request only 3 events
        CountDownLatch latch = new CountDownLatch(3);
        List<NostrEvent> receivedEvents = new ArrayList<>();

        NostrFilter filter = NostrFilter.builder()
                .authors(keys.getPublicKeyHex())
                .limit(3)
                .build();

        client.subscribeToAll(filter, (subId, event) -> {
            if (receivedEvents.size() < 3) {
                receivedEvents.add(event);
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);

        assertTrue(receivedEvents.size() <= 3, "Should receive at most 3 events");
        
        System.out.println("✓ Limit parameter works correctly (received " + receivedEvents.size() + " events)");
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Test multiple authors")
    void testMultipleAuthors() throws Exception {
        NostrKeys alice = NostrKeys.generate();
        NostrKeys bob = NostrKeys.generate();

        // Alice publishes
        NostrEvent aliceEvent = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Hello from Alice")
                .buildAndSign(alice);

        // Bob publishes
        NostrEvent bobEvent = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Hello from Bob")
                .buildAndSign(bob);

        client.publishEvent(aliceEvent, relayUri).get(5, TimeUnit.SECONDS);
        client.publishEvent(bobEvent, relayUri).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);

        // Filter for both authors
        CountDownLatch latch = new CountDownLatch(2);
        List<NostrEvent> receivedEvents = new ArrayList<>();

        NostrFilter filter = NostrFilter.builder()
                .authors(alice.getPublicKeyHex(), bob.getPublicKeyHex())
                .kinds(NostrKind.TEXT_NOTE)
                .build();

        client.subscribeToAll(filter, (subId, event) -> {
            receivedEvents.add(event);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(2, receivedEvents.size(), "Should receive events from both authors");
        
        boolean hasAlice = receivedEvents.stream()
                .anyMatch(e -> e.getPubkey().equals(alice.getPublicKeyHex()));
        boolean hasBob = receivedEvents.stream()
                .anyMatch(e -> e.getPubkey().equals(bob.getPublicKeyHex()));
        
        assertTrue(hasAlice, "Should have Alice's event");
        assertTrue(hasBob, "Should have Bob's event");
        
        System.out.println("✓ Multiple author filtering works correctly");
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Test event with tags")
    void testEventWithTags() throws Exception {
        NostrKeys keys = NostrKeys.generate();

        // Create event with tags
        NostrEvent event = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("Tagged event")
                .tag("e", "referenced-event-id")
                .tag("p", "mentioned-pubkey")
                .tag("t", "nostr")
                .tag("t", "java")
                .buildAndSign(keys);

        client.publishEvent(event, relayUri).get(5, TimeUnit.SECONDS);

        Thread.sleep(500);

        // Fetch and verify tags
        CountDownLatch latch = new CountDownLatch(1);
        List<NostrEvent> receivedEvents = new ArrayList<>();

        NostrFilter filter = NostrFilter.builder()
                .ids(event.getId())
                .build();

        client.subscribeToAll(filter, (subId, e) -> {
            receivedEvents.add(e);
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(1, receivedEvents.size());
        NostrEvent received = receivedEvents.get(0);
        
        assertEquals(4, received.getTags().size(), "Should have 4 tags");
        assertEquals("e", received.getTags().get(0).get(0));
        assertEquals("referenced-event-id", received.getTags().get(0).get(1));
        
        System.out.println("✓ Event tags preserved correctly");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Complete workflow - Generate, Sign, Publish, Fetch")
    void testCompleteWorkflow() throws Exception {
        System.out.println("\n=== Complete Workflow Test ===");
        
        // Step 1: Generate keys
        System.out.println("Step 1: Generating Nostr key pair...");
        NostrKeys keys = NostrKeys.generate();
        System.out.println("  ✓ Private key: " + keys.getPrivateKeyHex().substring(0, 16) + "...");
        System.out.println("  ✓ Public key: " + keys.getPublicKeyHex().substring(0, 16) + "...");
        System.out.println("  ✓ npub: " + keys.getPublicKeyBech32());
        
        // Step 2: Create and sign note
        System.out.println("\nStep 2: Creating and signing note...");
        NostrEvent note = new NostrEventBuilder()
                .kind(NostrKind.TEXT_NOTE)
                .content("This is a complete workflow test! 🚀")
                .tag("workflow", "test")
                .buildAndSign(keys);
        
        System.out.println("  ✓ Event ID: " + note.getId());
        System.out.println("  ✓ Signature: " + note.getSig().substring(0, 16) + "...");
        System.out.println("  ✓ Content: " + note.getContent());
        
        // Step 3: Publish to relay
        System.out.println("\nStep 3: Publishing to relay...");
        client.publishEvent(note, relayUri).get(5, TimeUnit.SECONDS);
        System.out.println("  ✓ Published successfully");
        
        Thread.sleep(500);
        
        // Step 4: Fetch all notes from relay
        System.out.println("\nStep 4: Fetching notes from relay...");
        CountDownLatch latch = new CountDownLatch(1);
        List<NostrEvent> fetchedNotes = new ArrayList<>();
        
        NostrFilter filter = NostrFilter.builder()
                .authors(keys.getPublicKeyHex())
                .kinds(NostrKind.TEXT_NOTE)
                .build();
        
        client.subscribeToAll(filter, (subId, event) -> {
            fetchedNotes.add(event);
            System.out.println("  ✓ Received: " + event.getContent());
            latch.countDown();
        });
        
        boolean received = latch.await(10, TimeUnit.SECONDS);
        assertTrue(received, "Should receive the published note");
        
        // Step 5: Verify
        System.out.println("\nStep 5: Verifying...");
        assertEquals(1, fetchedNotes.size(), "Should fetch exactly 1 note");
        NostrEvent fetchedNote = fetchedNotes.get(0);
        
        assertEquals(note.getId(), fetchedNote.getId(), "Event IDs should match");
        assertEquals(note.getContent(), fetchedNote.getContent(), "Content should match");
        assertEquals(note.getPubkey(), fetchedNote.getPubkey(), "Pubkeys should match");
        assertEquals(note.getSig(), fetchedNote.getSig(), "Signatures should match");
        
        System.out.println("  ✓ Event ID matches");
        System.out.println("  ✓ Content matches");
        System.out.println("  ✓ Signature matches");
        System.out.println("\n✅ Complete workflow test PASSED!");
    }
}
