package dev.sato.worldprotect.protection.permission;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable collection of permission keys.
 */
public final class PermissionSet {
    private static final PermissionSet EMPTY = new PermissionSet(Set.of());

    private final Set<PermissionKey> permissions;

    private PermissionSet(Set<PermissionKey> permissions) {
        Objects.requireNonNull(permissions, "permissions must not be null");
        for (PermissionKey key : permissions) {
            Objects.requireNonNull(key, "permission key element must not be null");
        }
        this.permissions = Set.copyOf(permissions);
    }

    public static PermissionSet empty() {
        return EMPTY;
    }

    public static PermissionSet of(Collection<PermissionKey> permissions) {
        if (permissions == null) {
            return EMPTY;
        }
        return new PermissionSet(new HashSet<>(permissions));
    }

    public static PermissionSet ofStrings(Collection<String> permissions) {
        if (permissions == null) {
            return EMPTY;
        }
        Set<PermissionKey> keys = permissions.stream()
                .map(PermissionKey::of)
                .collect(Collectors.toSet());
        return new PermissionSet(keys);
    }

    public Set<PermissionKey> permissions() {
        return permissions;
    }

    public boolean has(PermissionKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return permissions.contains(key);
    }

    public boolean has(String key) {
        Objects.requireNonNull(key, "key must not be null");
        try {
            return has(PermissionKey.of(key));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasAny(Collection<PermissionKey> keys) {
        Objects.requireNonNull(keys, "keys must not be null");
        for (PermissionKey key : keys) {
            if (has(key)) {
                return true;
            }
        }
        return false;
    }

    public PermissionSet with(PermissionKey key) {
        Objects.requireNonNull(key, "key must not be null");
        if (has(key)) {
            return this;
        }
        Set<PermissionKey> newPerms = new HashSet<>(permissions);
        newPerms.add(key);
        return new PermissionSet(newPerms);
    }

    public PermissionSet without(PermissionKey key) {
        Objects.requireNonNull(key, "key must not be null");
        if (!has(key)) {
            return this;
        }
        Set<PermissionKey> newPerms = new HashSet<>(permissions);
        newPerms.remove(key);
        return new PermissionSet(newPerms);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionSet that = (PermissionSet) o;
        return permissions.equals(that.permissions);
    }

    @Override
    public int hashCode() {
        return permissions.hashCode();
    }

    @Override
    public String toString() {
        return permissions.toString();
    }
}
