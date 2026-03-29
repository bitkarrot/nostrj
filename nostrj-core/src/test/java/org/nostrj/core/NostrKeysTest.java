package org.nostrj.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NostrKeysTest {

    @Test
    void testGenerateKeys() {
        NostrKeys keys = NostrKeys.generate();
        
        assertNotNull(keys.getPrivateKeyHex());
        assertNotNull(keys.getPublicKeyHex());
        assertEquals(64, keys.getPrivateKeyHex().length());
        assertEquals(64, keys.getPublicKeyHex().length());
    }

    @Test
    void testBech32Encoding() {
        NostrKeys keys = NostrKeys.generate();
        
        String nsec = keys.getPrivateKeyBech32();
        String npub = keys.getPublicKeyBech32();
        
        assertTrue(nsec.startsWith("nsec1"));
        assertTrue(npub.startsWith("npub1"));
    }

    @Test
    void testFromPrivateKeyHex() {
        NostrKeys original = NostrKeys.generate();
        String privateKeyHex = original.getPrivateKeyHex();
        
        NostrKeys restored = NostrKeys.fromPrivateKeyHex(privateKeyHex);
        
        assertEquals(original.getPrivateKeyHex(), restored.getPrivateKeyHex());
        assertEquals(original.getPublicKeyHex(), restored.getPublicKeyHex());
    }

    @Test
    void testFromPrivateKeyBytes() {
        NostrKeys original = NostrKeys.generate();
        byte[] privateKeyBytes = original.getPrivateKeyBytes();
        
        NostrKeys restored = NostrKeys.fromPrivateKey(privateKeyBytes);
        
        assertArrayEquals(original.getPrivateKeyBytes(), restored.getPrivateKeyBytes());
        assertArrayEquals(original.getPublicKeyBytes(), restored.getPublicKeyBytes());
    }
}
