package dev.sato.worldprotect.protection.flag;

import java.util.Objects;

/**
 * Immutable definition of a protection flag.
 */
public final class FlagDefinition {
    private final FlagKey key;
    private final String description;
    private final FlagState defaultState;

    public FlagDefinition(FlagKey key, String description, FlagState defaultState) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState must not be null");
    }

    public FlagKey key() {
        return key;
    }

    public String description() {
        return description;
    }

    public FlagState defaultState() {
        return defaultState;
    }

    public FlagKey getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public FlagState getDefaultState() {
        return defaultState;
    }

    public static FlagDefinition of(FlagKey key, String description, FlagState defaultState) {
        return new FlagDefinition(key, description, defaultState);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagDefinition that = (FlagDefinition) o;
        return key.equals(that.key) &&
               description.equals(that.description) &&
               defaultState == that.defaultState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, description, defaultState);
    }

    @Override
    public String toString() {
        return "FlagDefinition{key=" + key + ", description='" + description + '\'' + ", defaultState=" + defaultState + '}';
    }
}
