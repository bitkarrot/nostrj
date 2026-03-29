# NostrJ - Nostr Protocol Implementation for Java

A comprehensive Java implementation of the Nostr protocol, providing modular libraries for building Nostr clients, relays, and applications.

## Project Structure

NostrJ is organized as a multi-module Gradle project:

### 1. **nostrj-core** (JAR)
Core Nostr functionality including:
- **Key Generation**: Generate Nostr public/private key pairs using secp256k1
- **Bech32 Encoding**: Support for `npub` and `nsec` key formats
- **Message Signing**: Sign Nostr events with Schnorr signatures
- **Event Creation**: Builder pattern for creating any Nostr event kind
- **Signature Verification**: Verify event signatures

### 2. **nostrj-client** (JAR)
Client library for connecting to Nostr relays:
- **WebSocket Connections**: Connect to multiple relays simultaneously
- **Event Publishing**: Publish events to one or more relays
- **Subscriptions**: Subscribe to events with flexible filters
- **Message Handling**: Parse and handle all Nostr message types (EVENT, REQ, CLOSE, EOSE, OK, NOTICE)

### 3. **nostrj-server** (JAR)
Framework-independent relay server components:
- **Event Storage**: Pluggable event store interface
- **SQLite Implementation**: Production-ready SQLite event storage
- **Relay Handler**: Protocol-compliant message handling
- **Query Engine**: Efficient event querying with filters
- **Policy System**: Customizable relay policies

### 4. **nostrj-relay-app** (Application)
Complete Nostr relay server using Micronaut:
- **WebSocket Support**: Full Nostr protocol over WebSocket
- **HTTP API**: NIP-11 relay information endpoint
- **Health Checks**: Monitoring endpoints
- **Configuration**: YAML-based configuration
- **Production Ready**: Logging, error handling, and performance optimized

## Requirements

- Java 21 or later
- Gradle 8.5 or later (wrapper included)

## Dependencies

- **secp256k1-jdk** v0.2.0 - Cryptographic operations
- **bitcoinj** v0.17.0 - Bech32 encoding reference
- **Micronaut** v4.2.0 - Web framework for relay app
- **Jackson** - JSON serialization
- **SQLite JDBC** - Database storage

## Building

Build all modules:
```bash
./gradlew build
```

Build specific module:
```bash
./gradlew :nostrj-core:build
./gradlew :nostrj-client:build
./gradlew :nostrj-server:build
./gradlew :nostrj-relay-app:build
```

## Usage Examples

### Core Module - Key Generation and Event Signing

```java
import org.nostrj.core.*;

// Generate new keys
NostrKeys keys = NostrKeys.generate();
System.out.println("Private key (hex): " + keys.getPrivateKeyHex());
System.out.println("Public key (hex): " + keys.getPublicKeyHex());
System.out.println("Private key (bech32): " + keys.getPrivateKeyBech32());
System.out.println("Public key (bech32): " + keys.getPublicKeyBech32());

// Create and sign an event
NostrEvent event = new NostrEventBuilder()
    .kind(NostrKind.TEXT_NOTE)
    .content("Hello Nostr from Java!")
    .tag("t", "nostrj")
    .buildAndSign(keys);

// Verify event
boolean valid = NostrSigner.verify(event);
System.out.println("Event valid: " + valid);
```

### Client Module - Connect to Relays

```java
import org.nostrj.client.*;
import java.net.URI;

// Create client
NostrClient client = new NostrClient();

// Connect to relays
client.connectToRelay("wss://relay.damus.io").join();
client.connectToRelay("wss://nos.lol").join();

// Publish event
NostrEvent event = new NostrEventBuilder()
    .kind(NostrKind.TEXT_NOTE)
    .content("Hello from NostrJ!")
    .buildAndSign(keys);

client.publishEvent(event, 
    URI.create("wss://relay.damus.io"),
    URI.create("wss://nos.lol")
).join();

// Subscribe to events
NostrFilter filter = NostrFilter.builder()
    .kinds(NostrKind.TEXT_NOTE)
    .authors(keys.getPublicKeyHex())
    .limit(10)
    .build();

client.subscribeToAll(filter, (subId, receivedEvent) -> {
    System.out.println("Received: " + receivedEvent.getContent());
});

// Close connections
client.close();
```

### Server Module - Custom Relay

```java
import org.nostrj.server.*;

// Initialize event store
EventStore store = new SqliteEventStore("relay.db");
store.initialize();

// Create relay handler
RelayHandler handler = new RelayHandler(store);

// Handle incoming message (integrate with your WebSocket framework)
String response = handler.handleMessage(sessionId, incomingMessage);

// Query events
EventQuery query = EventQuery.builder()
    .kinds(List.of(1))
    .limit(20)
    .build();

List<NostrEvent> events = store.queryEvents(query);
```

### Relay Application - Run a Relay

Configure `application.yml`:
```yaml
relay:
  name: My Nostr Relay
  description: A relay powered by NostrJ
  contact: admin@example.com
  db-path: nostr-relay.db
  enable-nip11: true

micronaut:
  server:
    port: 8080
```

Run the relay:
```bash
./gradlew :nostrj-relay-app:run
```

Or build and run as standalone:
```bash
./gradlew :nostrj-relay-app:build
java -jar nostrj-relay-app/build/libs/nostrj-relay-app-0.1.0-SNAPSHOT-all.jar
```

Connect to your relay:
- WebSocket: `ws://localhost:8080/`
- Relay Info (NIP-11): `http://localhost:8080/`
- Health Check: `http://localhost:8080/health`

## Testing

Run all tests:
```bash
./gradlew test
```

Run tests for specific module:
```bash
./gradlew :nostrj-core:test
```

## Supported NIPs

- **NIP-01**: Basic protocol flow
- **NIP-09**: Event deletion
- **NIP-11**: Relay information document
- **NIP-12**: Generic tag queries
- **NIP-15**: End of stored events notice
- **NIP-16**: Event treatment
- **NIP-20**: Command results
- **NIP-22**: Event created_at limits
- **NIP-33**: Parameterized replaceable events
- **NIP-40**: Expiration timestamp

## Architecture

```
┌─────────────────────────────────────────────────┐
│           nostrj-relay-app (Application)        │
│  ┌──────────────┐         ┌─────────────────┐  │
│  │  WebSocket   │         │   HTTP API      │  │
│  │   Handler    │         │  (NIP-11, etc)  │  │
│  └──────┬───────┘         └────────┬────────┘  │
│         │                          │            │
│         └──────────┬───────────────┘            │
│                    │                            │
│         ┌──────────▼──────────┐                 │
│         │   Micronaut Core    │                 │
│         └──────────┬──────────┘                 │
└────────────────────┼────────────────────────────┘
                     │
         ┌───────────▼───────────┐
         │  nostrj-server (JAR)  │
         │  ┌─────────────────┐  │
         │  │ RelayHandler    │  │
         │  ├─────────────────┤  │
         │  │ EventStore      │  │
         │  │  - SQLite impl  │  │
         │  ├─────────────────┤  │
         │  │ RelayPolicy     │  │
         │  └─────────────────┘  │
         └───────────┬───────────┘
                     │
         ┌───────────▼───────────┐
         │  nostrj-client (JAR)  │
         │  ┌─────────────────┐  │
         │  │ NostrClient     │  │
         │  ├─────────────────┤  │
         │  │ RelayConnection │  │
         │  ├─────────────────┤  │
         │  │ NostrFilter     │  │
         │  └─────────────────┘  │
         └───────────┬───────────┘
                     │
         ┌───────────▼───────────┐
         │   nostrj-core (JAR)   │
         │  ┌─────────────────┐  │
         │  │ NostrKeys       │  │
         │  ├─────────────────┤  │
         │  │ NostrEvent      │  │
         │  ├─────────────────┤  │
         │  │ NostrSigner     │  │
         │  ├─────────────────┤  │
         │  │ Bech32Util      │  │
         │  └─────────────────┘  │
         │         │             │
         │  ┌──────▼──────────┐  │
         │  │  secp256k1-jdk  │  │
         │  └─────────────────┘  │
         └───────────────────────┘
```

## License

Apache License 2.0

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

## References

- [Nostr Protocol](https://github.com/nostr-protocol/nostr)
- [NIPs (Nostr Implementation Possibilities)](https://github.com/nostr-protocol/nips)
- [secp256k1-jdk](https://github.com/bitcoinj/secp256k1-jdk)
- [bitcoinj](https://github.com/bitcoinj/bitcoinj)
