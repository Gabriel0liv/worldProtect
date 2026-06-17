package dev.sato.worldprotect.protection.permission;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class BuiltInPermissionsTest {

    @Test
    public void testConstants() {
        assertEquals("worldprotect.bypass", BuiltInPermissions.GLOBAL_BYPASS.value());
        assertEquals("worldprotect.region.admin", BuiltInPermissions.REGION_ADMIN.value());
    }

    @Test
    public void testGenerators() {
        assertEquals("worldprotect.bypass.flag.build", BuiltInPermissions.flagBypass(FlagKey.of("build")).value());
        assertEquals("worldprotect.region.spawn.owner", BuiltInPermissions.regionOwnerBypass(RegionId.of("spawn")).value());
        assertEquals("worldprotect.region.spawn.member", BuiltInPermissions.regionMemberBypass(RegionId.of("spawn")).value());
    }
}
