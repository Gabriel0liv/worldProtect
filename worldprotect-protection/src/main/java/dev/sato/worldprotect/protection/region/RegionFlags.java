package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable container representing configured flag states on a region.
 */
public final class RegionFlags {
    private static final RegionFlags EMPTY = new RegionFlags(Map.of());

    private final Map<FlagKey, FlagState> flags;

    private RegionFlags(Map<FlagKey, FlagState> flags) {
        this.flags = Map.copyOf(flags);
    }

    public static RegionFlags empty() {
        return EMPTY;
    }

    public static RegionFlags of(Map<FlagKey, FlagState> flags) {
        Objects.requireNonNull(flags, "flags map must not be null");
        return new RegionFlags(flags);
    }

    public Optional<FlagState> get(FlagKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(flags.get(key));
    }

    public boolean contains(FlagKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return flags.containsKey(key);
    }

    public Map<FlagKey, FlagState> asMap() {
        return flags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionFlags that = (RegionFlags) o;
        return flags.equals(that.flags);
    }

    @Override
    public int hashCode() {
        return flags.hashCode();
    }

    @Override
    public String toString() {
        return "RegionFlags{flags=" + flags + "}";
    }
}
