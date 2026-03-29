package org.nostrj.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitcoinj.secp.SchnorrSignature;
import org.bitcoinj.secp.Secp256k1;
import org.bitcoinj.secp.SecpKeyPair;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class NostrSigner {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static NostrEvent sign(NostrEvent event, NostrKeys keys) {
        try {
            String serialized = serializeForId(event);
            byte[] hash = sha256(serialized.getBytes(StandardCharsets.UTF_8));
            event.setId(HexFormat.of().formatHex(hash));
            
            Secp256k1 secp = keys.getSecp();
            SecpKeyPair keyPair = secp.ecKeyPairCreate(keys.getPrivateKey());
            SchnorrSignature signature = secp.schnorrSigSign32(hash, keyPair);
            event.setSig(HexFormat.of().formatHex(signature.serialize()));
            
            return event;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign event", e);
        }
    }

    public static boolean verify(NostrEvent event) {
        try {
            String serialized = serializeForId(event);
            byte[] hash = sha256(serialized.getBytes(StandardCharsets.UTF_8));
            String computedId = HexFormat.of().formatHex(hash);
            
            if (!computedId.equals(event.getId())) {
                return false;
            }
            
            byte[] pubkeyBytes = HexFormat.of().parseHex(event.getPubkey());
            byte[] sigBytes = HexFormat.of().parseHex(event.getSig());
            
            try (Secp256k1 secp = Secp256k1.get()) {
                SchnorrSignature signature = SchnorrSignature.of(sigBytes);
                var pubkey = org.bitcoinj.secp.SecpXOnlyPubKey.of(pubkeyBytes);
                var result = secp.schnorrSigVerify(signature, hash, pubkey);
                return result.isSuccess() && result.get();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static String serializeForId(NostrEvent event) throws JsonProcessingException {
        Object[] array = new Object[]{
            0,
            event.getPubkey(),
            event.getCreatedAt(),
            event.getKind(),
            event.getTags(),
            event.getContent()
        };
        return MAPPER.writeValueAsString(array);
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
