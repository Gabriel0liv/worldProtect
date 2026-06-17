package dev.sato.worldprotect.protection.permission;

import java.util.Objects;

/**
 * Immutable representation of a permission key.
 */
public final class PermissionKey {
    private final String value;

    private PermissionKey(String value) {
        Objects.requireNonNull(value, "value must not be null");
        if (value.length() < 1 || value.length() > 128) {
            throw new IllegalArgumentException("Permission key length must be between 1 and 128 characters");
        }
        if (!value.equals(value.toLowerCase())) {
            throw new IllegalArgumentException("Permission key must be lowercase: " + value);
        }
        if (value.startsWith(".") || value.endsWith(".") || value.contains("..")) {
            throw new IllegalArgumentException("Permission key cannot contain empty segments");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.')) {
                throw new IllegalArgumentException("Invalid character in permission key: " + c);
            }
        }
        this.value = value;
    }

    public static PermissionKey of(String value) {
        return new PermissionKey(value);
    }

    public String value() {
        return value;
    }

    public boolean startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix must not be null");
        return this.value.equals(prefix) || this.value.startsWith(prefix + ".");
    }

    public boolean startsWith(PermissionKey prefix) {
        Objects.requireNonNull(prefix, "prefix must not be null");
        return startsWith(prefix.value());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionKey that = (PermissionKey) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
