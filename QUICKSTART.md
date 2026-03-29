# NostrJ Quick Start Guide

Get started with NostrJ in 5 minutes!

## Installation

Add NostrJ modules to your Gradle project:

```gradle
dependencies {
    implementation 'org.nostrj:nostrj-core:0.1.0-SNAPSHOT'
    implementation 'org.nostrj:nostrj-client:0.1.0-SNAPSHOT'
    // Optional: for building relays
    implementation 'org.nostrj:nostrj-server:0.1.0-SNAPSHOT'
}
```

Or build from source:

```bash
git clone https://github.com/yourusername/nostrj.git
cd nostrj
./gradlew build
```

## 1. Generate Keys (30 seconds)

```java
import org.nostrj.core.NostrKeys;

NostrKeys keys = NostrKeys.generate();
System.out.println("Your npub: " + keys.getPublicKeyBech32());
System.out.println("Your nsec: " + keys.getPrivateKeyBech32());
```

**Output:**
```
Your npub: npub1abc123...
Your nsec: nsec1xyz789...
```

## 2. Create and Sign an Event (1 minute)

```java
import org.nostrj.core.*;

NostrEvent event = new NostrEventBuilder()
    .kind(NostrKind.TEXT_NOTE)
    .content("Hello Nostr from Java! 👋")
    .buildAndSign(keys);

System.out.println("Event ID: " + event.getId());
System.out.println("Signature: " + event.getSig());
```

## 3. Connect to a Relay (2 minutes)

```java
import org.nostrj.client.*;
import java.net.URI;

NostrClient client = new NostrClient();

// Connect to a public relay
client.connectToRelay("wss://relay.damus.io").join();

// Publish your event
client.publishEvent(event, URI.create("wss://relay.damus.io")).join();

System.out.println("Event published! 🎉");
```

## 4. Subscribe to Events (2 minutes)

```java
// Subscribe to recent text notes
NostrFilter filter = NostrFilter.builder()
    .kinds(NostrKind.TEXT_NOTE)
    .limit(5)
    .build();

client.subscribeToAll(filter, (subscriptionId, receivedEvent) -> {
    System.out.println("📨 " + receivedEvent.getContent());
});

// Keep listening for 10 seconds
Thread.sleep(10000);
client.close();
```

## Complete Example

Here's everything together:

```java
import org.nostrj.client.*;
import org.nostrj.core.*;
import java.net.URI;

public class NostrQuickStart {
    public static void main(String[] args) throws Exception {
        // 1. Generate keys
        NostrKeys keys = NostrKeys.generate();
        System.out.println("Generated keys!");
        
        // 2. Create client and connect
        NostrClient client = new NostrClient();
        client.connectToRelay("wss://relay.damus.io").join();
        System.out.println("Connected to relay!");
        
        // 3. Create and publish event
        NostrEvent event = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Hello Nostr from NostrJ! 🚀")
            .buildAndSign(keys);
        
        client.publishEvent(event, 
            URI.create("wss://relay.damus.io")).join();
        System.out.println("Published event: " + event.getId());
        
        // 4. Subscribe to recent notes
        NostrFilter filter = NostrFilter.builder()
            .kinds(NostrKind.TEXT_NOTE)
            .limit(5)
            .build();
        
        client.subscribeToAll(filter, (subId, e) -> {
            System.out.println("📨 " + e.getContent());
        });
        
        // Listen for 10 seconds
        Thread.sleep(10000);
        client.close();
        System.out.println("Done! ✨");
    }
}
```

## Run Your Own Relay (5 minutes)

### 1. Configure the relay

Edit `nostrj-relay-app/src/main/resources/application.yml`:

```yaml
relay:
  name: My First Relay
  description: Learning NostrJ
  contact: me@example.com
```

### 2. Start the relay

```bash
./gradlew :nostrj-relay-app:run
```

### 3. Test it

```bash
# Check relay info
curl http://localhost:8080/

# Connect with your client
client.connectToRelay("ws://localhost:8080/").join();
```

## Next Steps

- 📖 Read the full [README.md](README.md)
- 💡 Check out [EXAMPLES.md](EXAMPLES.md) for more examples
- 🔧 Learn about [custom relay policies](EXAMPLES.md#8-custom-relay-policy)
- 🌐 Explore [Nostr NIPs](https://github.com/nostr-protocol/nips)

## Common Issues

### "Connection refused"
Make sure the relay is running and accessible. Try a different public relay.

### "Signature verification failed"
Ensure you're using the same keys to sign and verify events.

### "Module not found"
Run `./gradlew build` to build all modules first.

## Getting Help

- 📝 Open an issue on GitHub
- 💬 Join the Nostr community
- 📚 Read the [Contributing Guide](CONTRIBUTING.md)

Happy Nostr-ing! ⚡
