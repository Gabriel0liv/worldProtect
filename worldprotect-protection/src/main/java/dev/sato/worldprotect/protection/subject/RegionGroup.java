package dev.sato.worldprotect.protection.subject;

import java.util.Objects;

/**
 * Represents a group of actors based on their membership role inside a region.
 */
public enum RegionGroup {
    ALL("all"),
    OWNERS("owners"),
    MEMBERS("members"),
    NONOWNERS("nonowners"),
    NONMEMBERS("nonmembers");

    private final String key;

    RegionGroup(String key) {
        this.key = key;
    }

    public String configKey() {
        return key;
    }

    public static RegionGroup parse(String raw) {
        Objects.requireNonNull(raw, "raw must not be null");
        String normalized = raw.trim().toLowerCase();
        for (RegionGroup group : values()) {
            if (group.key.equals(normalized)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Unknown region group: " + raw);
    }

    public boolean matches(RegionRole role) {
        Objects.requireNonNull(role, "role must not be null");
        switch (this) {
            case ALL:
                return true;
            case OWNERS:
                return role == RegionRole.OWNER;
            case MEMBERS:
                return role == RegionRole.OWNER || role == RegionRole.MEMBER;
            case NONOWNERS:
                return role != RegionRole.OWNER;
            case NONMEMBERS:
                return role != RegionRole.OWNER && role != RegionRole.MEMBER;
            default:
                throw new AssertionError("Unhandled group: " + this);
        }
    }
}
