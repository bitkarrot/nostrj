# Getting Started with NostrJ

## ✅ Project Complete!

Your NostrJ implementation is ready to use. Here's what has been created:

## 📦 What You Have

### 1. **nostrj-core** (JAR Library)
- ✅ Nostr key generation with secp256k1-jdk
- ✅ Bech32 encoding (npub/nsec) using bitcoinj
- ✅ Schnorr signature signing and verification
- ✅ Event creation for any Nostr kind
- ✅ Full test coverage

**Location**: `nostrj-core/`

### 2. **nostrj-client** (JAR Library)
- ✅ WebSocket client for connecting to relays
- ✅ Multi-relay support
- ✅ Event publishing and subscription
- ✅ Filter-based event queries
- ✅ Message parsing (EVENT, REQ, CLOSE, EOSE, OK, NOTICE)

**Location**: `nostrj-client/`

### 3. **nostrj-server** (JAR Library)
- ✅ Framework-independent relay components
- ✅ SQLite event storage with full indexing
- ✅ Query engine with tag support
- ✅ Customizable relay policies
- ✅ Event validation and verification

**Location**: `nostrj-server/`

### 4. **nostrj-relay-app** (Micronaut Application)
- ✅ Complete WebSocket relay server
- ✅ HTTP API with NIP-11 support
- ✅ Health check endpoints
- ✅ YAML configuration
- ✅ Production logging

**Location**: `nostrj-relay-app/`

## 🚀 Quick Start

### Prerequisites
Make sure you have **Java 21** installed:
```bash
java -version
# Should show version 21 or higher
```

### Build Everything
```bash
cd /Users/bk/github/nostrj
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Start the Relay Server
```bash
./gradlew :nostrj-relay-app:run
```

The relay will start on `http://localhost:8080`

## 📚 Documentation

- **[README.md](README.md)** - Main documentation and architecture
- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute tutorial
- **[EXAMPLES.md](EXAMPLES.md)** - Comprehensive code examples
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Technical details
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines

## 🔧 Next Steps

### 1. Try the Core Module
```java
import org.nostrj.core.*;

NostrKeys keys = NostrKeys.generate();
System.out.println("npub: " + keys.getPublicKeyBech32());

NostrEvent event = new NostrEventBuilder()
    .kind(NostrKind.TEXT_NOTE)
    .content("Hello Nostr!")
    .buildAndSign(keys);
```

### 2. Connect to a Relay
```java
import org.nostrj.client.*;

NostrClient client = new NostrClient();
client.connectToRelay("wss://relay.damus.io").join();

client.publishEvent(event, URI.create("wss://relay.damus.io")).join();
```

### 3. Run Your Own Relay
```bash
# Edit configuration
nano nostrj-relay-app/src/main/resources/application.yml

# Start relay
./gradlew :nostrj-relay-app:run

# Test it
curl http://localhost:8080/
```

## 📁 Project Structure

```
nostrj/
├── nostrj-core/          # Core crypto & events
├── nostrj-client/        # Relay client
├── nostrj-server/        # Relay server components
├── nostrj-relay-app/     # Complete relay app
├── README.md             # Main docs
├── QUICKSTART.md         # Quick tutorial
├── EXAMPLES.md           # Code examples
└── build.gradle          # Build config
```

## 🎯 Key Features Implemented

### Cryptography
- ✅ secp256k1 key generation
- ✅ Schnorr signatures
- ✅ Bech32 encoding (npub/nsec)
- ✅ SHA-256 hashing

### Protocol Support
- ✅ NIP-01: Basic protocol
- ✅ NIP-11: Relay information
- ✅ NIP-12: Generic tag queries
- ✅ NIP-15: End of stored events
- ✅ NIP-20: Command results

### Storage
- ✅ SQLite with full indexing
- ✅ Tag-based queries
- ✅ Event validation
- ✅ Transaction support

### Networking
- ✅ WebSocket client
- ✅ WebSocket server
- ✅ Multi-relay support
- ✅ Async I/O with Netty

## 🛠️ Build Commands Reference

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :nostrj-core:build
./gradlew :nostrj-client:build
./gradlew :nostrj-server:build
./gradlew :nostrj-relay-app:build

# Run tests
./gradlew test
./gradlew :nostrj-core:test

# Run relay
./gradlew :nostrj-relay-app:run

# Create distribution
./gradlew :nostrj-relay-app:distZip

# Clean build
./gradlew clean build
```

## 📖 Learn More

1. **Read QUICKSTART.md** for a 5-minute tutorial
2. **Browse EXAMPLES.md** for code examples
3. **Check PROJECT_SUMMARY.md** for technical details
4. **Visit [Nostr NIPs](https://github.com/nostr-protocol/nips)** for protocol specs

## 🐛 Troubleshooting

### "Unsupported class file major version"
Make sure you're using Java 21:
```bash
java -version
```

### "Connection refused"
Make sure the relay is running:
```bash
./gradlew :nostrj-relay-app:run
```

### "Module not found"
Build the project first:
```bash
./gradlew build
```

## 💡 Tips

- Start with the examples in `EXAMPLES.md`
- Use the QUICKSTART guide for a fast introduction
- Check the tests for usage patterns
- Customize relay policies in `nostrj-server`
- Configure the relay app via `application.yml`

## 🎉 You're Ready!

Everything is set up and ready to use. Start building your Nostr applications!

```bash
# Quick test
./gradlew :nostrj-core:test
./gradlew :nostrj-relay-app:run
```

Happy coding! ⚡
