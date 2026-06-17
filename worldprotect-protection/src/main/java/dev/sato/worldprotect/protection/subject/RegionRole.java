package dev.sato.worldprotect.protection.subject;

import java.util.Objects;

/**
 * Defines the roles inside a region.
 */
public enum RegionRole {
    NONE,
    MEMBER,
    OWNER;

    /**
     * Checks if this role is at least equal to or higher than the other role.
     */
    public boolean atLeast(RegionRole other) {
        Objects.requireNonNull(other, "other role must not be null");
        return this.ordinal() >= other.ordinal();
    }
}
