package dev.sato.worldprotect.minecraft;

import java.util.Objects;

/**
 * References a specific dimension/world (e.g., minecraft:overworld).
 */
public final class DimensionRef {
    private final ResourceRef key;

    public DimensionRef(ResourceRef key) {
        this.key = Objects.requireNonNull(key, "key must not be null");
    }

    public ResourceRef key() {
        return key;
    }

    public String asString() {
        return key.asString();
    }

    public ResourceRef getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionRef that = (DimensionRef) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "DimensionRef{" + key + "}";
    }
}
