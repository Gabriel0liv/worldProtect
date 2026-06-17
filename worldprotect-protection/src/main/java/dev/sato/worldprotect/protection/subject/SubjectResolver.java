package dev.sato.worldprotect.protection.subject;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.permission.BuiltInPermissions;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

/**
 * Resolver for checking context subject details, roles, and bypass permissions.
 */
public final class SubjectResolver {

    private SubjectResolver() {
        // Utility constructor
    }

    /**
     * Resolves the role of the subject context inside the given region.
     */
    public static RegionRole roleInRegion(ProtectionSubjectContext context, Region region) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(region, "region must not be null");

        RegionSubjects subjects = region.subjects();
        RegionRole highestRole = RegionRole.NONE;

        for (SubjectRef subject : context.actorSubjects().subjects()) {
            RegionRole role = subjects.roleOf(subject);
            if (role.ordinal() > highestRole.ordinal()) {
                highestRole = role;
            }
        }
        return highestRole;
    }

    /**
     * Checks if the context has global bypass permissions.
     */
    public static boolean hasGlobalBypass(ProtectionSubjectContext context) {
        Objects.requireNonNull(context, "context must not be null");
        return context.permissions().has(BuiltInPermissions.GLOBAL_BYPASS) ||
               context.permissions().has(BuiltInPermissions.REGION_ADMIN);
    }

    /**
     * Checks if the context has flag-specific bypass permissions.
     */
    public static boolean hasFlagBypass(ProtectionSubjectContext context, FlagKey flagKey) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(flagKey, "flagKey must not be null");
        if (hasGlobalBypass(context)) {
            return true;
        }
        try {
            return context.permissions().has(BuiltInPermissions.flagBypass(flagKey));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the context has region owner bypass permissions.
     */
    public static boolean hasRegionOwnerBypass(ProtectionSubjectContext context, RegionId regionId) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(regionId, "regionId must not be null");
        if (hasGlobalBypass(context)) {
            return true;
        }
        try {
            return context.permissions().has(BuiltInPermissions.regionOwnerBypass(regionId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if the context has region member bypass permissions.
     */
    public static boolean hasRegionMemberBypass(ProtectionSubjectContext context, RegionId regionId) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(regionId, "regionId must not be null");
        if (hasGlobalBypass(context)) {
            return true;
        }
        try {
            return context.permissions().has(BuiltInPermissions.regionMemberBypass(regionId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
