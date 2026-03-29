package org.nostrj.core;

public final class NostrKind {
    public static final int METADATA = 0;
    public static final int TEXT_NOTE = 1;
    public static final int RECOMMEND_RELAY = 2;
    public static final int CONTACTS = 3;
    public static final int ENCRYPTED_DIRECT_MESSAGE = 4;
    public static final int EVENT_DELETION = 5;
    public static final int REPOST = 6;
    public static final int REACTION = 7;
    public static final int CHANNEL_CREATE = 40;
    public static final int CHANNEL_METADATA = 41;
    public static final int CHANNEL_MESSAGE = 42;
    public static final int CHANNEL_HIDE_MESSAGE = 43;
    public static final int CHANNEL_MUTE_USER = 44;
    public static final int REPORTING = 1984;
    public static final int ZAP_REQUEST = 9734;
    public static final int ZAP = 9735;
    public static final int RELAY_LIST_METADATA = 10002;
    public static final int CLIENT_AUTHENTICATION = 22242;
    public static final int NOSTR_CONNECT = 24133;
    public static final int CATEGORIZED_PEOPLE_LIST = 30000;
    public static final int CATEGORIZED_BOOKMARK_LIST = 30001;
    public static final int PROFILE_BADGES = 30008;
    public static final int BADGE_DEFINITION = 30009;
    public static final int LONG_FORM_CONTENT = 30023;
    public static final int APPLICATION_SPECIFIC_DATA = 30078;

    private NostrKind() {
    }
}
