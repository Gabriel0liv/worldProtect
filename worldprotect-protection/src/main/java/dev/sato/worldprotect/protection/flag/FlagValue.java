package dev.sato.worldprotect.protection.flag;

import java.util.Objects;

/**
 * Value container for a protection flag.
 */
public final class FlagValue<T> {
    private final T value;

    private FlagValue(T value) {
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    public static <V> FlagValue<V> of(V value) {
        return new FlagValue<>(value);
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagValue<?> flagValue = (FlagValue<?>) o;
        return value.equals(flagValue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
