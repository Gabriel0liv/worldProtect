package dev.sato.worldprotect.protection.flag;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Key representation of a protection setting (e.g. "build", "chest-access", "pvp").
 */
public final class FlagKey {
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9\\-]{1,64}$");
    
    private final String name;

    public FlagKey(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (!VALID_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Flag name must be lowercase alphanumeric and dashes, and between 1 and 64 characters. Got: " + name);
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return name;
    }

    public static FlagKey of(String name) {
        return new FlagKey(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagKey flagKey = (FlagKey) o;
        return name.equals(flagKey.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
