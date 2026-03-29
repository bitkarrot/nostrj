package org.nostrj.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NostrEventBuilder {
    private String pubkey;
    private long createdAt;
    private int kind;
    private List<List<String>> tags;
    private String content;

    public NostrEventBuilder() {
        this.tags = new ArrayList<>();
        this.createdAt = Instant.now().getEpochSecond();
    }

    public NostrEventBuilder pubkey(String pubkey) {
        this.pubkey = pubkey;
        return this;
    }

    public NostrEventBuilder pubkey(NostrKeys keys) {
        this.pubkey = keys.getPublicKeyHex();
        return this;
    }

    public NostrEventBuilder createdAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NostrEventBuilder kind(int kind) {
        this.kind = kind;
        return this;
    }

    public NostrEventBuilder content(String content) {
        this.content = content;
        return this;
    }

    public NostrEventBuilder tag(String... tagValues) {
        this.tags.add(Arrays.asList(tagValues));
        return this;
    }

    public NostrEventBuilder tags(List<List<String>> tags) {
        this.tags = new ArrayList<>(tags);
        return this;
    }

    public NostrEventBuilder addTag(List<String> tag) {
        this.tags.add(new ArrayList<>(tag));
        return this;
    }

    public NostrEvent build() {
        return new NostrEvent(pubkey, createdAt, kind, tags, content);
    }

    public NostrEvent buildAndSign(NostrKeys keys) {
        NostrEvent event = build();
        event.setPubkey(keys.getPublicKeyHex());
        return NostrSigner.sign(event, keys);
    }
}
