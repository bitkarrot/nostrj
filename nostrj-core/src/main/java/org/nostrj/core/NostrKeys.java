package org.nostrj.core;

import org.bitcoinj.secp.Secp256k1;
import org.bitcoinj.secp.SecpKeyPair;
import org.bitcoinj.secp.SecpPrivKey;
import org.bitcoinj.secp.SecpXOnlyPubKey;

import java.security.SecureRandom;
import java.util.HexFormat;

public class NostrKeys {
    private final SecpPrivKey privateKey;
    private final SecpXOnlyPubKey publicKey;
    private final Secp256k1 secp;

    private NostrKeys(SecpPrivKey privateKey, SecpXOnlyPubKey publicKey, Secp256k1 secp) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.secp = secp;
    }

    public static NostrKeys generate() {
        Secp256k1 secp = Secp256k1.get();
        SecpKeyPair keyPair = secp.ecKeyPairCreate();
        return new NostrKeys(keyPair.privateKey(), keyPair.publicKey().xOnly(), secp);
    }

    public static NostrKeys fromPrivateKey(byte[] privateKeyBytes) {
        Secp256k1 secp = Secp256k1.get();
        SecpPrivKey privKey = SecpPrivKey.of(privateKeyBytes);
        SecpKeyPair keyPair = secp.ecKeyPairCreate(privKey);
        return new NostrKeys(privKey, keyPair.publicKey().xOnly(), secp);
    }

    public static NostrKeys fromPrivateKeyHex(String privateKeyHex) {
        byte[] privateKeyBytes = HexFormat.of().parseHex(privateKeyHex);
        return fromPrivateKey(privateKeyBytes);
    }

    public byte[] getPrivateKeyBytes() {
        return privateKey.getEncoded();
    }

    public String getPrivateKeyHex() {
        return HexFormat.of().formatHex(getPrivateKeyBytes());
    }

    public String getPrivateKeyBech32() {
        return Bech32Util.encodeBech32("nsec", getPrivateKeyBytes());
    }

    public byte[] getPublicKeyBytes() {
        return publicKey.serialize();
    }

    public String getPublicKeyHex() {
        return HexFormat.of().formatHex(getPublicKeyBytes());
    }

    public String getPublicKeyBech32() {
        return Bech32Util.encodeBech32("npub", getPublicKeyBytes());
    }

    public SecpPrivKey getPrivateKey() {
        return privateKey;
    }

    public SecpXOnlyPubKey getPublicKey() {
        return publicKey;
    }

    Secp256k1 getSecp() {
        return secp;
    }
}
