package dev.sato.worldprotect.protection.region;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Unique identifier for a protected region.
 */
public final class RegionId {
    private static final Pattern VALID_PATTERN = Pattern.compile("^[a-z0-9_\\-.]{1,64}$");
    
    private final String value;

    public RegionId(String value) {
        Objects.requireNonNull(value, "value must not be null");
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Region ID must be lowercase alphanumeric, dashes, dots, or underscores, and between 1 and 64 characters. Got: " + value);
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RegionId of(String value) {
        return new RegionId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionId regionId = (RegionId) o;
        return value.equals(regionId.value);
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
