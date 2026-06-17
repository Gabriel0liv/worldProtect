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
    public static final PermissionKey COMMAND_REGION_CREATE = PermissionKey.of("worldprotect.command.region.create");
    public static final PermissionKey COMMAND_REGION_DELETE = PermissionKey.of("worldprotect.command.region.delete");
    public static final PermissionKey COMMAND_REGION_FLAG = PermissionKey.of("worldprotect.command.region.flag");
    public static final PermissionKey COMMAND_REGION_PARENT = PermissionKey.of("worldprotect.command.region.parent");
    public static final PermissionKey COMMAND_REGION_SUBJECT = PermissionKey.of("worldprotect.command.region.subject");
    public static final PermissionKey COMMAND_REGION_INFO = PermissionKey.of("worldprotect.command.region.info");
    public static final PermissionKey COMMAND_REGION_LIST = PermissionKey.of("worldprotect.command.region.list");
    public static final PermissionKey COMMAND_REGION_ADMIN = PermissionKey.of("worldprotect.command.region.admin");

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
