package org.nostrj.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NostrFilter {
    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("authors")
    private List<String> authors;

    @JsonProperty("kinds")
    private List<Integer> kinds;

    @JsonProperty("#e")
    private List<String> eventTags;

    @JsonProperty("#p")
    private List<String> pubkeyTags;

    @JsonProperty("since")
    private Long since;

    @JsonProperty("until")
    private Long until;

    @JsonProperty("limit")
    private Integer limit;

    public NostrFilter() {
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public List<Integer> getKinds() {
        return kinds;
    }

    public void setKinds(List<Integer> kinds) {
        this.kinds = kinds;
    }

    public List<String> getEventTags() {
        return eventTags;
    }

    public void setEventTags(List<String> eventTags) {
        this.eventTags = eventTags;
    }

    public List<String> getPubkeyTags() {
        return pubkeyTags;
    }

    public void setPubkeyTags(List<String> pubkeyTags) {
        this.pubkeyTags = pubkeyTags;
    }

    public Long getSince() {
        return since;
    }

    public void setSince(Long since) {
        this.since = since;
    }

    public Long getUntil() {
        return until;
    }

    public void setUntil(Long until) {
        this.until = until;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NostrFilter filter = new NostrFilter();

        public Builder ids(String... ids) {
            filter.ids = List.of(ids);
            return this;
        }

        public Builder authors(String... authors) {
            filter.authors = List.of(authors);
            return this;
        }

        public Builder kinds(Integer... kinds) {
            filter.kinds = List.of(kinds);
            return this;
        }

        public Builder eventTags(String... eventTags) {
            filter.eventTags = List.of(eventTags);
            return this;
        }

        public Builder pubkeyTags(String... pubkeyTags) {
            filter.pubkeyTags = List.of(pubkeyTags);
            return this;
        }

        public Builder since(long since) {
            filter.since = since;
            return this;
        }

        public Builder until(long until) {
            filter.until = until;
            return this;
        }

        public Builder limit(int limit) {
            filter.limit = limit;
            return this;
        }

        public NostrFilter build() {
            return filter;
        }
    }
}
