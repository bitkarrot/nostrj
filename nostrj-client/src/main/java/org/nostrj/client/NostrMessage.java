package org.nostrj.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nostrj.core.NostrEvent;

import java.util.ArrayList;
import java.util.List;

public class NostrMessage {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public enum Type {
        EVENT, REQ, CLOSE, EOSE, OK, NOTICE, AUTH, COUNT
    }

    private final Type type;
    private final JsonNode rawMessage;

    private NostrMessage(Type type, JsonNode rawMessage) {
        this.type = type;
        this.rawMessage = rawMessage;
    }

    public static NostrMessage parse(String json) throws JsonProcessingException {
        JsonNode node = MAPPER.readTree(json);
        if (!node.isArray() || node.size() < 1) {
            throw new IllegalArgumentException("Invalid Nostr message format");
        }

        String typeStr = node.get(0).asText();
        Type type = Type.valueOf(typeStr);
        return new NostrMessage(type, node);
    }

    public static String createEventMessage(NostrEvent event) throws JsonProcessingException {
        Object[] message = new Object[]{"EVENT", event};
        return MAPPER.writeValueAsString(message);
    }

    public static String createReqMessage(String subscriptionId, NostrFilter... filters) throws JsonProcessingException {
        List<Object> message = new ArrayList<>();
        message.add("REQ");
        message.add(subscriptionId);
        for (NostrFilter filter : filters) {
            message.add(filter);
        }
        return MAPPER.writeValueAsString(message);
    }

    public static String createCloseMessage(String subscriptionId) throws JsonProcessingException {
        Object[] message = new Object[]{"CLOSE", subscriptionId};
        return MAPPER.writeValueAsString(message);
    }

    public Type getType() {
        return type;
    }

    public String getSubscriptionId() {
        if (rawMessage.size() > 1) {
            return rawMessage.get(1).asText();
        }
        return null;
    }

    public NostrEvent getEvent() throws JsonProcessingException {
        if (type == Type.EVENT && rawMessage.size() > 2) {
            return MAPPER.treeToValue(rawMessage.get(2), NostrEvent.class);
        }
        return null;
    }

    public String getNotice() {
        if (type == Type.NOTICE && rawMessage.size() > 1) {
            return rawMessage.get(1).asText();
        }
        return null;
    }

    public boolean isOk() {
        if (type == Type.OK && rawMessage.size() > 2) {
            return rawMessage.get(2).asBoolean();
        }
        return false;
    }

    public String getOkMessage() {
        if (type == Type.OK && rawMessage.size() > 3) {
            return rawMessage.get(3).asText();
        }
        return null;
    }

    public JsonNode getRawMessage() {
        return rawMessage;
    }

    @Override
    public String toString() {
        return rawMessage.toString();
    }
}
