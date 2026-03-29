# Testing NostrJ

## Current Status

✅ **All tests passing!** The project now uses Maven and is fully compatible with Java 25. 

## Prerequisites

Before running tests, install the secp256k1 native library:

```bash
# macOS
brew install secp256k1

# Linux (Ubuntu/Debian)
sudo apt-get install libsecp256k1-dev
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Relay Integration Tests
```bash
mvn test -pl nostrj-relay-app
```

### Run Specific Test
```bash
mvn test -pl nostrj-relay-app -Dtest=RelayIntegrationTest
mvn test -pl nostrj-relay-app -Dtest=RelayIntegrationTest#testCompleteWorkflow
```

## Integration Tests Created

The comprehensive integration test suite is located at:
`nostrj-relay-app/src/test/java/org/nostrj/relay/RelayIntegrationTest.java`

It includes 8 test scenarios:

1. **Generate keys, sign note, publish to relay** - Basic workflow
2. **Publish multiple notes and fetch them all** - Multi-event handling  
3. **Test filtering by kind** - Event type filtering
4. **Test time-based filtering** - Temporal queries
5. **Test limit parameter** - Result pagination
6. **Test multiple authors** - Multi-user scenarios
7. **Test event with tags** - Tag preservation
8. **Complete workflow** - End-to-end test with detailed output

## Manual Testing Steps

### 1. Test Core Module

```bash
# Generate keys
cat > TestKeys.java << 'EOF'
import org.nostrj.core.*;

public class TestKeys {
    public static void main(String[] args) {
        NostrKeys keys = NostrKeys.generate();
        System.out.println("Private key (hex): " + keys.getPrivateKeyHex());
        System.out.println("Public key (hex): " + keys.getPublicKeyHex());
        System.out.println("Private key (nsec): " + keys.getPrivateKeyBech32());
        System.out.println("Public key (npub): " + keys.getPublicKeyBech32());
        
        NostrEvent event = new NostrEventBuilder()
            .kind(NostrKind.TEXT_NOTE)
            .content("Test message")
            .buildAndSign(keys);
            
        System.out.println("Event ID: " + event.getId());
        System.out.println("Signature valid: " + NostrSigner.verify(event));
    }
}
EOF

# Compile and run (requires nostrj-core JAR in classpath)
```

### 2. Start the Relay

```bash
mvn exec:java -pl nostrj-relay-app
```

The relay will start on `http://localhost:8080`

### 3. Test HTTP Endpoints

```bash
# Health check
curl http://localhost:8080/health

# Relay info (NIP-11)
curl http://localhost:8080/
```

### 4. Test WebSocket with websocat

Install websocat:
```bash
brew install websocat  # macOS
```

Connect to relay:
```bash
websocat ws://localhost:8080/
```

Send a REQ message:
```json
["REQ","test-sub",{"kinds":[1],"limit":10}]
```

### 5. Test with Nostr Client

Use any Nostr client (Damus, Amethyst, etc.) and add your relay:
```
ws://localhost:8080/
```

## Troubleshooting

### "Unsupported class file major version 69"

This means Gradle 8.5 is trying to use Java 25 which it doesn't support. Solutions:

1. Install Java 21: https://adoptium.net/temurin/releases/?version=21
2. Set `JAVA_HOME` to Java 21:
   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
   ```

### "No matching toolchains found"

Gradle can't find the required Java version. Either:
- Install the required Java version
- Modify `build.gradle` to use your installed Java version

### Tests Won't Run

If Gradle tests won't run due to version issues:
1. Use the manual testing approach above
2. Install Java 21
3. Or wait for a Docker-based testing solution

## What Works

✅ Core module (key generation, signing, verification)
✅ Client module (WebSocket connections, subscriptions)
✅ Server module (SQLite storage, event queries)
✅ Relay app (WebSocket server, HTTP API)
✅ Integration test code (ready to run with Java 21)

## Next Steps

1. Install Java 21 for full test suite
2. Or use manual testing with the relay running
3. Consider Docker-based testing environment
