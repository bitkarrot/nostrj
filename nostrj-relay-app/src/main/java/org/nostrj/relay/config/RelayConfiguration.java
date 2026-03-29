package org.nostrj.relay.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("relay")
public class RelayConfiguration {
    private String name = "NostrJ Relay";
    private String description = "A Nostr relay powered by NostrJ";
    private String contact = "";
    private String dbPath = "nostr-relay.db";
    private boolean enableNip11 = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public boolean isEnableNip11() {
        return enableNip11;
    }

    public void setEnableNip11(boolean enableNip11) {
        this.enableNip11 = enableNip11;
    }
}
