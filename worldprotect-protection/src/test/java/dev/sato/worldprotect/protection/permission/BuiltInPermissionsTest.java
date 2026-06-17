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
        assertEquals("worldprotect.command.region.create", BuiltInPermissions.COMMAND_REGION_CREATE.value());
        assertEquals("worldprotect.command.region.delete", BuiltInPermissions.COMMAND_REGION_DELETE.value());
        assertEquals("worldprotect.command.region.flag", BuiltInPermissions.COMMAND_REGION_FLAG.value());
        assertEquals("worldprotect.command.region.parent", BuiltInPermissions.COMMAND_REGION_PARENT.value());
        assertEquals("worldprotect.command.region.subject", BuiltInPermissions.COMMAND_REGION_SUBJECT.value());
        assertEquals("worldprotect.command.region.info", BuiltInPermissions.COMMAND_REGION_INFO.value());
        assertEquals("worldprotect.command.region.list", BuiltInPermissions.COMMAND_REGION_LIST.value());
        assertEquals("worldprotect.command.region.admin", BuiltInPermissions.COMMAND_REGION_ADMIN.value());
    }

    @Test
    public void testGenerators() {
        assertEquals("worldprotect.bypass.flag.build", BuiltInPermissions.flagBypass(FlagKey.of("build")).value());
        assertEquals("worldprotect.region.spawn.owner", BuiltInPermissions.regionOwnerBypass(RegionId.of("spawn")).value());
        assertEquals("worldprotect.region.spawn.member", BuiltInPermissions.regionMemberBypass(RegionId.of("spawn")).value());
    }
}
