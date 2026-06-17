package dev.sato.worldprotect.protection.subject;

import dev.sato.worldprotect.protection.flag.FlagKey;
import java.util.Objects;
import java.util.Set;

/**
 * Defines the bypass policies for owner and member roles inside a region.
 */
public final class RegionAccessPolicy {
    private static final RegionAccessPolicy DEFAULTS = new RegionAccessPolicy(true, false, Set.of(), Set.of());

    private final boolean ownersBypassFlags;
    private final boolean membersBypassFlags;
    private final Set<FlagKey> ownerBypassFlags;
    private final Set<FlagKey> memberBypassFlags;

    private RegionAccessPolicy(
            boolean ownersBypassFlags,
            boolean membersBypassFlags,
            Set<FlagKey> ownerBypassFlags,
            Set<FlagKey> memberBypassFlags
    ) {
        Objects.requireNonNull(ownerBypassFlags, "ownerBypassFlags must not be null");
        Objects.requireNonNull(memberBypassFlags, "memberBypassFlags must not be null");

        for (FlagKey f : ownerBypassFlags) {
            Objects.requireNonNull(f, "owner bypass flag element must not be null");
        }
        for (FlagKey f : memberBypassFlags) {
            Objects.requireNonNull(f, "member bypass flag element must not be null");
        }

        this.ownersBypassFlags = ownersBypassFlags;
        this.membersBypassFlags = membersBypassFlags;
        this.ownerBypassFlags = Set.copyOf(ownerBypassFlags);
        this.memberBypassFlags = Set.copyOf(memberBypassFlags);
    }

    public static RegionAccessPolicy defaults() {
        return DEFAULTS;
    }

    public static RegionAccessPolicy ownersBypassAll() {
        return DEFAULTS;
    }

    public static RegionAccessPolicy of(
            boolean ownersBypassFlags,
            boolean membersBypassFlags,
            Set<FlagKey> ownerBypassFlags,
            Set<FlagKey> memberBypassFlags
    ) {
        return new RegionAccessPolicy(ownersBypassFlags, membersBypassFlags, ownerBypassFlags, memberBypassFlags);
    }

    public boolean ownersBypassFlags() {
        return ownersBypassFlags;
    }

    public boolean membersBypassFlags() {
        return membersBypassFlags;
    }

    public Set<FlagKey> ownerBypassFlags() {
        return ownerBypassFlags;
    }

    public Set<FlagKey> memberBypassFlags() {
        return memberBypassFlags;
    }

    public boolean ownerBypasses(FlagKey flag) {
        Objects.requireNonNull(flag, "flag must not be null");
        if (ownersBypassFlags) {
            return true;
        }
        return ownerBypassFlags.contains(flag);
    }

    public boolean memberBypasses(FlagKey flag) {
        Objects.requireNonNull(flag, "flag must not be null");
        if (membersBypassFlags) {
            return true;
        }
        return memberBypassFlags.contains(flag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionAccessPolicy that = (RegionAccessPolicy) o;
        return ownersBypassFlags == that.ownersBypassFlags &&
               membersBypassFlags == that.membersBypassFlags &&
               ownerBypassFlags.equals(that.ownerBypassFlags) &&
               memberBypassFlags.equals(that.memberBypassFlags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownersBypassFlags, membersBypassFlags, ownerBypassFlags, memberBypassFlags);
    }

    @Override
    public String toString() {
        return "RegionAccessPolicy{" +
               "ownersBypassFlags=" + ownersBypassFlags +
               ", membersBypassFlags=" + membersBypassFlags +
               ", ownerBypassFlags=" + ownerBypassFlags +
               ", memberBypassFlags=" + memberBypassFlags +
               '}';
    }
}
