package org.nostrj.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({"id", "pubkey", "created_at", "kind", "tags", "content", "sig"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NostrEvent {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("pubkey")
    private String pubkey;
    
    @JsonProperty("created_at")
    private long createdAt;
    
    @JsonProperty("kind")
    private int kind;
    
    @JsonProperty("tags")
    private List<List<String>> tags;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("sig")
    private String sig;

    public NostrEvent() {
        this.tags = new ArrayList<>();
    }

    public NostrEvent(String pubkey, long createdAt, int kind, List<List<String>> tags, String content) {
        this.pubkey = pubkey;
        this.createdAt = createdAt;
        this.kind = kind;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public List<List<String>> getTags() {
        return tags;
    }

    public void setTags(List<List<String>> tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NostrEvent that = (NostrEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NostrEvent{" +
                "id='" + id + '\'' +
                ", pubkey='" + pubkey + '\'' +
                ", createdAt=" + createdAt +
                ", kind=" + kind +
                ", tags=" + tags +
                ", content='" + content + '\'' +
                ", sig='" + sig + '\'' +
                '}';
    }
}
