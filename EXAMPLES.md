# NostrJ Examples

## Core Module Examples

### 1. Generate Keys and Encode to Bech32

```java
import org.nostrj.core.NostrKeys;

public class KeyGenerationExample {
    public static void main(String[] args) {
        // Generate new keys
        NostrKeys keys = NostrKeys.generate();
        
        // Display keys in different formats
        System.out.println("Private Key (Hex): " + keys.getPrivateKeyHex());
        System.out.println("Public Key (Hex): " + keys.getPublicKeyHex());
        System.out.println("Private Key (nsec): " + keys.getPrivateKeyBech32());
        System.out.println("Public Key (npub): " + keys.getPublicKeyBech32());
        
        // Restore keys from hex
        NostrKeys restored = NostrKeys.fromPrivateKeyHex(keys.getPrivateKeyHex());
        System.out.println("Keys match: " + 
            keys.getPublicKeyHex().equals(restored.getPublicKeyHex()));
    }
}
```

### 2. Create and Sign Different Event Types

```java
import org.nostrj.core.*;

public class EventCreationExample {
    public static void main(String[] args) {
        NostrKeys keys = NostrKeys.generate();
        
        // Text note (kind 1)
        NostrEvent textNote = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Hello Nostr!")
            .buildAndSign(keys);
        
        // Metadata (kind 0)
        String metadata = "{\"name\":\"Alice\",\"about\":\"Nostr enthusiast\"}";
        NostrEvent metadataEvent = new NostrEventBuilder()
            .kind(NostrKind.METADATA)
            .content(metadata)
            .buildAndSign(keys);
        
        // Reaction (kind 7)
        NostrEvent reaction = new NostrEventBuilder()
            .kind(NostrKind.REACTION)
            .content("🔥")
            .tag("e", "event-id-to-react-to")
            .tag("p", "author-pubkey")
            .buildAndSign(keys);
        
        // Long-form content (kind 30023)
        NostrEvent article = new NostrEventBuilder()
            .kind(NostrKind.LONG_FORM_CONTENT)
            .content("# My Article\n\nThis is a long-form article...")
            .tag("title", "My First Article")
            .tag("published_at", String.valueOf(System.currentTimeMillis() / 1000))
            .tag("t", "nostr")
            .tag("t", "java")
            .buildAndSign(keys);
        
        System.out.println("Text Note ID: " + textNote.getId());
        System.out.println("Metadata Event ID: " + metadataEvent.getId());
        System.out.println("Reaction ID: " + reaction.getId());
        System.out.println("Article ID: " + article.getId());
    }
}
```

### 3. Verify Event Signatures

```java
import org.nostrj.core.*;

public class SignatureVerificationExample {
    public static void main(String[] args) {
        NostrKeys keys = NostrKeys.generate();
        
        NostrEvent event = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Verify this!")
            .buildAndSign(keys);
        
        // Verify valid event
        boolean valid = NostrSigner.verify(event);
        System.out.println("Valid signature: " + valid);
        
        // Tamper with content
        event.setContent("Tampered content");
        boolean stillValid = NostrSigner.verify(event);
        System.out.println("Still valid after tampering: " + stillValid);
    }
}
```

## Client Module Examples

### 4. Connect to Multiple Relays

```java
import org.nostrj.client.*;
import org.nostrj.core.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class MultiRelayExample {
    public static void main(String[] args) throws Exception {
        NostrClient client = new NostrClient();
        
        // Connect to multiple relays
        client.connectToRelay("wss://relay.damus.io").join();
        client.connectToRelay("wss://nos.lol").join();
        client.connectToRelay("wss://relay.nostr.band").join();
        
        System.out.println("Connected to " + 
            client.getAllConnections().size() + " relays");
        
        // Publish to all connected relays
        NostrKeys keys = NostrKeys.generate();
        NostrEvent event = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Broadcasting to all relays!")
            .buildAndSign(keys);
        
        client.publishEvent(event,
            URI.create("wss://relay.damus.io"),
            URI.create("wss://nos.lol"),
            URI.create("wss://relay.nostr.band")
        ).join();
        
        System.out.println("Event published!");
        
        client.close();
    }
}
```

### 5. Subscribe and Listen to Events

```java
import org.nostrj.client.*;
import org.nostrj.core.*;
import java.util.concurrent.CountDownLatch;

public class SubscriptionExample {
    public static void main(String[] args) throws Exception {
        NostrClient client = new NostrClient();
        client.connectToRelay("wss://relay.damus.io").join();
        
        CountDownLatch latch = new CountDownLatch(10);
        
        // Subscribe to recent text notes
        NostrFilter filter = NostrFilter.builder()
            .kinds(NostrKind.TEXT_NOTE)
            .limit(10)
            .build();
        
        client.subscribeToAll(filter, (subId, event) -> {
            System.out.println("Received event from: " + 
                event.getPubkey().substring(0, 8) + "...");
            System.out.println("Content: " + event.getContent());
            System.out.println("---");
            latch.countDown();
        });
        
        // Wait for events
        latch.await();
        client.close();
    }
}
```

### 6. Advanced Filtering

```java
import org.nostrj.client.*;
import org.nostrj.core.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AdvancedFilterExample {
    public static void main(String[] args) throws Exception {
        NostrClient client = new NostrClient();
        client.connectToRelay("wss://relay.damus.io").join();
        
        NostrKeys myKeys = NostrKeys.generate();
        
        // Filter for events from specific authors in the last hour
        long oneHourAgo = Instant.now()
            .minus(1, ChronoUnit.HOURS)
            .getEpochSecond();
        
        NostrFilter filter = NostrFilter.builder()
            .authors("author-pubkey-1", "author-pubkey-2")
            .kinds(NostrKind.TEXT_NOTE, NostrKind.REACTION)
            .since(oneHourAgo)
            .limit(50)
            .build();
        
        // Filter for events that mention you
        NostrFilter mentionFilter = NostrFilter.builder()
            .pubkeyTags(myKeys.getPublicKeyHex())
            .kinds(NostrKind.TEXT_NOTE)
            .limit(20)
            .build();
        
        // Filter for replies to a specific event
        NostrFilter replyFilter = NostrFilter.builder()
            .eventTags("event-id-here")
            .kinds(NostrKind.TEXT_NOTE)
            .build();
        
        client.subscribeToAll(filter, (subId, event) -> {
            System.out.println("Filtered event: " + event.getContent());
        });
        
        Thread.sleep(5000);
        client.close();
    }
}
```

## Server Module Examples

### 7. Custom Event Store Implementation

```java
import org.nostrj.server.*;
import org.nostrj.core.*;
import java.util.List;

public class EventStoreExample {
    public static void main(String[] args) throws Exception {
        // Initialize SQLite event store
        EventStore store = new SqliteEventStore("my-relay.db");
        store.initialize();
        
        // Save an event
        NostrKeys keys = NostrKeys.generate();
        NostrEvent event = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Stored in database")
            .buildAndSign(keys);
        
        store.saveEvent(event);
        System.out.println("Event saved!");
        
        // Retrieve by ID
        NostrEvent retrieved = store.getEventById(event.getId());
        System.out.println("Retrieved: " + retrieved.getContent());
        
        // Query events
        EventQuery query = EventQuery.builder()
            .authors(List.of(keys.getPublicKeyHex()))
            .kinds(List.of(NostrKind.TEXT_NOTE))
            .limit(10)
            .build();
        
        List<NostrEvent> events = store.queryEvents(query);
        System.out.println("Found " + events.size() + " events");
        
        // Count events
        long count = store.countEvents(query);
        System.out.println("Total count: " + count);
        
        store.close();
    }
}
```

### 8. Custom Relay Policy

```java
import org.nostrj.server.*;
import org.nostrj.core.*;

public class CustomPolicyExample implements RelayPolicy {
    private static final int MAX_CONTENT = 5000;
    private static final int MAX_TAGS = 100;
    
    @Override
    public boolean acceptEvent(NostrEvent event) {
        // Reject events with too much content
        if (event.getContent() != null && 
            event.getContent().length() > MAX_CONTENT) {
            return false;
        }
        
        // Reject events with too many tags
        if (event.getTags() != null && 
            event.getTags().size() > MAX_TAGS) {
            return false;
        }
        
        // Only accept certain kinds
        return acceptKind(event.getKind());
    }
    
    @Override
    public boolean acceptKind(int kind) {
        // Only accept text notes, metadata, and reactions
        return kind == NostrKind.TEXT_NOTE || 
               kind == NostrKind.METADATA || 
               kind == NostrKind.REACTION;
    }
    
    @Override
    public int getMaxContentLength() {
        return MAX_CONTENT;
    }
    
    @Override
    public int getMaxTagsCount() {
        return MAX_TAGS;
    }
    
    public static void main(String[] args) throws Exception {
        EventStore store = new SqliteEventStore("custom-relay.db");
        store.initialize();
        
        RelayPolicy policy = new CustomPolicyExample();
        RelayHandler handler = new RelayHandler(store, policy);
        
        // Handler will now use custom policy
        System.out.println("Relay handler created with custom policy");
        
        store.close();
    }
}
```

## Relay Application Examples

### 9. Custom Configuration

Create `application.yml`:

```yaml
micronaut:
  application:
    name: my-nostr-relay
  server:
    port: 7777
    netty:
      max-header-size: 32KB

relay:
  name: My Custom Relay
  description: A specialized relay for my community
  contact: admin@myrelay.com
  db-path: /var/lib/nostr/relay.db
  enable-nip11: true

logger:
  levels:
    org.nostrj: DEBUG
    io.micronaut: WARN
```

### 10. Running the Relay

```bash
# Development mode
mvn -pl nostrj-relay-app mn:run

# Build standalone JAR
mvn -pl nostrj-relay-app package

# Run standalone
java -jar nostrj-relay-app/target/nostrj-relay-app-0.1.0-SNAPSHOT.jar

# With custom config
java -Dmicronaut.config.files=custom-config.yml \
     -jar nostrj-relay-app/target/nostrj-relay-app-0.1.0-SNAPSHOT.jar

# With environment variables
MICRONAUT_SERVER_PORT=8888 \
RELAY_NAME="Production Relay" \
java -jar nostrj-relay-app/target/nostrj-relay-app-0.1.0-SNAPSHOT.jar
```

### 11. Testing Your Relay

```bash
# Check relay info (NIP-11)
curl -H "Accept: application/nostr+json" http://localhost:8080/

# Health check
curl http://localhost:8080/health

# Connect with websocat
websocat ws://localhost:8080/

# Send a REQ message
["REQ","sub1",{"kinds":[1],"limit":10}]

# Send an EVENT
["EVENT",{"id":"...","pubkey":"...","created_at":...,"kind":1,"tags":[],"content":"Hello","sig":"..."}]
```

## Integration Example

### 12. Complete Client-Server Flow

```java
import org.nostrj.client.*;
import org.nostrj.core.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class CompleteFlowExample {
    public static void main(String[] args) throws Exception {
        // Assume relay is running on localhost:8080
        NostrClient client = new NostrClient();
        
        // Connect to local relay
        client.connectToRelay("ws://localhost:8080/").join();
        System.out.println("Connected to relay");
        
        // Generate keys
        NostrKeys alice = NostrKeys.generate();
        NostrKeys bob = NostrKeys.generate();
        
        // Alice publishes a note
        NostrEvent aliceNote = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Hello from Alice!")
            .buildAndSign(alice);
        
        client.publishEvent(aliceNote, URI.create("ws://localhost:8080/")).join();
        System.out.println("Alice published note");
        
        // Bob subscribes to Alice's notes
        CountDownLatch latch = new CountDownLatch(1);
        
        NostrFilter filter = NostrFilter.builder()
            .authors(alice.getPublicKeyHex())
            .kinds(NostrKind.TEXT_NOTE)
            .build();
        
        client.subscribeToAll(filter, (subId, event) -> {
            System.out.println("Bob received: " + event.getContent());
            latch.countDown();
        });
        
        // Wait for Bob to receive
        latch.await();
        
        // Bob reacts to Alice's note
        NostrEvent bobReaction = new NostrEventBuilder()
            .kind(NostrKind.REACTION)
            .content("👍")
            .tag("e", aliceNote.getId())
            .tag("p", alice.getPublicKeyHex())
            .buildAndSign(bob);
        
        client.publishEvent(bobReaction, URI.create("ws://localhost:8080/")).join();
        System.out.println("Bob reacted to Alice's note");
        
        client.close();
    }
}
```
