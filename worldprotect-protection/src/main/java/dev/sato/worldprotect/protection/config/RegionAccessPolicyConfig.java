package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable configuration representation of bypass rules for owners/members.
 */
public final class RegionAccessPolicyConfig {
    private static final RegionAccessPolicyConfig DEFAULTS = new RegionAccessPolicyConfig(null, null, List.of(), List.of());

    private final Boolean ownersBypass;
    private final Boolean membersBypass;
    private final List<String> ownerBypassFlags;
    private final List<String> memberBypassFlags;

    private RegionAccessPolicyConfig(
            Boolean ownersBypass,
            Boolean membersBypass,
            Collection<String> ownerBypassFlags,
            Collection<String> memberBypassFlags
    ) {
        this.ownersBypass = ownersBypass;
        this.membersBypass = membersBypass;
        this.ownerBypassFlags = ownerBypassFlags != null ? List.copyOf(ownerBypassFlags) : List.of();
        this.memberBypassFlags = memberBypassFlags != null ? List.copyOf(memberBypassFlags) : List.of();

        for (String f : this.ownerBypassFlags) {
            Objects.requireNonNull(f, "owner bypass flag element must not be null");
        }
        for (String f : this.memberBypassFlags) {
            Objects.requireNonNull(f, "member bypass flag element must not be null");
        }
    }

    public static RegionAccessPolicyConfig defaults() {
        return DEFAULTS;
    }

    public static RegionAccessPolicyConfig of(
            Boolean ownersBypass,
            Boolean membersBypass,
            Collection<String> ownerBypassFlags,
            Collection<String> memberBypassFlags
    ) {
        return new RegionAccessPolicyConfig(ownersBypass, membersBypass, ownerBypassFlags, memberBypassFlags);
    }

    public Boolean ownersBypass() {
        return ownersBypass;
    }

    public Boolean membersBypass() {
        return membersBypass;
    }

    public List<String> ownerBypassFlags() {
        return ownerBypassFlags;
    }

    public List<String> memberBypassFlags() {
        return memberBypassFlags;
    }

    public ConfigValidationResult validate(String path, FlagRegistry flagRegistry) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
        ConfigValidationResult result = ConfigValidationResult.ok();

        Set<FlagKey> seenOwnerFlags = new HashSet<>();
        Set<FlagKey> duplicateOwnerFlags = new HashSet<>();
        for (int i = 0; i < ownerBypassFlags.size(); i++) {
            String rawFlag = ownerBypassFlags.get(i);
            String p = path + ".owner-bypass-flags[" + i + "]";
            FlagKey key = null;
            try {
                key = FlagKey.of(rawFlag);
            } catch (Exception e) {
                result = result.add(ConfigValidationMessage.error(p, "Invalid flag syntax: " + rawFlag + ". " + e.getMessage()));
                continue;
            }

            if (!flagRegistry.exists(key)) {
                result = result.add(ConfigValidationMessage.error(p, "Unknown flag key: " + key.getValue()));
            }

            if (!seenOwnerFlags.add(key)) {
                duplicateOwnerFlags.add(key);
            }
        }

        for (FlagKey dup : duplicateOwnerFlags) {
            result = result.add(ConfigValidationMessage.warning(path + ".owner-bypass-flags", "Duplicate flag configured: " + dup.getValue()));
        }

        Set<FlagKey> seenMemberFlags = new HashSet<>();
        Set<FlagKey> duplicateMemberFlags = new HashSet<>();
        for (int i = 0; i < memberBypassFlags.size(); i++) {
            String rawFlag = memberBypassFlags.get(i);
            String p = path + ".member-bypass-flags[" + i + "]";
            FlagKey key = null;
            try {
                key = FlagKey.of(rawFlag);
            } catch (Exception e) {
                result = result.add(ConfigValidationMessage.error(p, "Invalid flag syntax: " + rawFlag + ". " + e.getMessage()));
                continue;
            }

            if (!flagRegistry.exists(key)) {
                result = result.add(ConfigValidationMessage.error(p, "Unknown flag key: " + key.getValue()));
            }

            if (!seenMemberFlags.add(key)) {
                duplicateMemberFlags.add(key);
            }
        }

        for (FlagKey dup : duplicateMemberFlags) {
            result = result.add(ConfigValidationMessage.warning(path + ".member-bypass-flags", "Duplicate flag configured: " + dup.getValue()));
        }

        return result;
    }

    public RegionAccessPolicy toDomain() {
        boolean ownersBypassDomain = ownersBypass != null ? ownersBypass : true;
        boolean membersBypassDomain = membersBypass != null ? membersBypass : false;
        Set<FlagKey> ownerFlags = ownerBypassFlags.stream()
                .map(FlagKey::of)
                .collect(Collectors.toUnmodifiableSet());
        Set<FlagKey> memberFlags = memberBypassFlags.stream()
                .map(FlagKey::of)
                .collect(Collectors.toUnmodifiableSet());
        return RegionAccessPolicy.of(ownersBypassDomain, membersBypassDomain, ownerFlags, memberFlags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionAccessPolicyConfig that = (RegionAccessPolicyConfig) o;
        return Objects.equals(ownersBypass, that.ownersBypass) &&
               Objects.equals(membersBypass, that.membersBypass) &&
               ownerBypassFlags.equals(that.ownerBypassFlags) &&
               memberBypassFlags.equals(that.memberBypassFlags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownersBypass, membersBypass, ownerBypassFlags, memberBypassFlags);
    }

    @Override
    public String toString() {
        return "RegionAccessPolicyConfig{ownersBypass=" + ownersBypass +
               ", membersBypass=" + membersBypass +
               ", ownerBypassFlags=" + ownerBypassFlags +
               ", memberBypassFlags=" + memberBypassFlags +
               '}';
    }
}
