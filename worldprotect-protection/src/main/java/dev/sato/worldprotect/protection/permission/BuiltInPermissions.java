package dev.sato.worldprotect.protection.permission;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

/**
 * Utility class defining standard/built-in permission keys and generators.
 */
public final class BuiltInPermissions {
    public static final PermissionKey GLOBAL_BYPASS = PermissionKey.of("worldprotect.bypass");
    public static final PermissionKey REGION_ADMIN = PermissionKey.of("worldprotect.region.admin");

    private BuiltInPermissions() {
        // Utility constructor
    }

    public static PermissionKey flagBypass(FlagKey flagKey) {
        Objects.requireNonNull(flagKey, "flagKey must not be null");
        return PermissionKey.of("worldprotect.bypass.flag." + flagKey.getValue());
    }

    public static PermissionKey regionOwnerBypass(RegionId regionId) {
        Objects.requireNonNull(regionId, "regionId must not be null");
        return PermissionKey.of("worldprotect.region." + regionId.getValue() + ".owner");
    }

    public static PermissionKey regionMemberBypass(RegionId regionId) {
        Objects.requireNonNull(regionId, "regionId must not be null");
        return PermissionKey.of("worldprotect.region." + regionId.getValue() + ".member");
    }
}
