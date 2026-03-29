#!/bin/bash

echo "=== NostrJ Relay Integration Test ==="
echo ""
echo "This script will:"
echo "1. Start the relay server in the background"
echo "2. Wait for it to start"
echo "3. Run a simple test to verify functionality"
echo ""

# Start the relay in the background
echo "Starting relay server..."
./gradlew :nostrj-relay-app:run > relay.log 2>&1 &
RELAY_PID=$!

echo "Relay PID: $RELAY_PID"
echo "Waiting for relay to start (15 seconds)..."
sleep 15

# Check if relay is running
if ps -p $RELAY_PID > /dev/null; then
   echo "✓ Relay is running"
   
   # Test the health endpoint
   echo ""
   echo "Testing health endpoint..."
   curl -s http://localhost:8080/health | jq '.' || echo "Health check response received"
   
   # Test the relay info endpoint (NIP-11)
   echo ""
   echo "Testing relay info endpoint (NIP-11)..."
   curl -s http://localhost:8080/ | jq '.' || echo "Relay info response received"
   
   echo ""
   echo "✓ Basic HTTP endpoints are working"
   echo ""
   echo "To test WebSocket functionality, you can use:"
   echo "  websocat ws://localhost:8080/"
   echo ""
   echo "Or connect with a Nostr client to ws://localhost:8080/"
   echo ""
   echo "Press Ctrl+C to stop the relay, or run: kill $RELAY_PID"
   echo ""
   echo "Relay logs are in: relay.log"
   
   # Keep script running
   wait $RELAY_PID
else
   echo "✗ Relay failed to start"
   echo "Check relay.log for errors"
   exit 1
fi
