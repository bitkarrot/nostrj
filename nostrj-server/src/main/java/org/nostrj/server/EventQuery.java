package org.nostrj.server;

import java.util.ArrayList;
import java.util.List;

public class EventQuery {
    private List<String> ids;
    private List<String> authors;
    private List<Integer> kinds;
    private List<String> eventTags;
    private List<String> pubkeyTags;
    private Long since;
    private Long until;
    private Integer limit;

    public EventQuery() {
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
        private final EventQuery query = new EventQuery();

        public Builder ids(List<String> ids) {
            query.ids = new ArrayList<>(ids);
            return this;
        }

        public Builder authors(List<String> authors) {
            query.authors = new ArrayList<>(authors);
            return this;
        }

        public Builder kinds(List<Integer> kinds) {
            query.kinds = new ArrayList<>(kinds);
            return this;
        }

        public Builder eventTags(List<String> eventTags) {
            query.eventTags = new ArrayList<>(eventTags);
            return this;
        }

        public Builder pubkeyTags(List<String> pubkeyTags) {
            query.pubkeyTags = new ArrayList<>(pubkeyTags);
            return this;
        }

        public Builder since(long since) {
            query.since = since;
            return this;
        }

        public Builder until(long until) {
            query.until = until;
            return this;
        }

        public Builder limit(int limit) {
            query.limit = limit;
            return this;
        }

        public EventQuery build() {
            return query;
        }
    }
}
