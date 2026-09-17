package com.riverside.permits.domain;

public enum Purpose {
    COMMUNITY_EVENT("Community Event"),
    RELIGIOUS_SERVICE("Religious Service"),
    PRIVATE_FUNCTION("Private Function"),
    COMMERCIAL_USE("Commercial Use"),
    COUNCIL_USE("Council Use");

    private final String displayName;
    Purpose(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
