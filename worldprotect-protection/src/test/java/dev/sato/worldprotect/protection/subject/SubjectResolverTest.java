package dev.sato.worldprotect.protection.subject;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.permission.PermissionSet;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class SubjectResolverTest {

    @Test
    public void testRoleInRegionResolution() {
        UUID uuid = UUID.randomUUID();
        Actor actor = new Actor("player1", ActorType.PLAYER);
        ActorSubjects subjects = ActorSubjects.player(actor, uuid, List.of("vip"));
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(subjects);

        RegionSubjects regionSubjects = RegionSubjects.empty()
                .withOwner(SubjectRef.player(uuid))
                .withMember(SubjectRef.group("vip"));

        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                new DimensionRef(ResourceRef.of("minecraft", "overworld")),
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                dev.sato.worldprotect.protection.region.RegionFlags.empty(),
                regionSubjects,
                RegionAccessPolicy.defaults()
        );

        // Owner wins over member
        assertEquals(RegionRole.OWNER, SubjectResolver.roleInRegion(context, region));

        // Member only
        ActorSubjects memberSubjects = ActorSubjects.player(actor, UUID.randomUUID(), List.of("vip"));
        ProtectionSubjectContext memberContext = ProtectionSubjectContext.withoutPermissions(memberSubjects);
        assertEquals(RegionRole.MEMBER, SubjectResolver.roleInRegion(memberContext, region));

        // None
        ActorSubjects noneSubjects = ActorSubjects.player(actor, UUID.randomUUID(), List.of("other"));
        ProtectionSubjectContext noneContext = ProtectionSubjectContext.withoutPermissions(noneSubjects);
        assertEquals(RegionRole.NONE, SubjectResolver.roleInRegion(noneContext, region));
    }

    @Test
    public void testBypasses() {
        Actor actor = new Actor("player1", ActorType.PLAYER);
        ActorSubjects subjects = ActorSubjects.console(actor);

        // Global bypass
        ProtectionSubjectContext globalContext = ProtectionSubjectContext.of(
                subjects,
                PermissionSet.ofStrings(List.of("worldprotect.bypass"))
        );
        assertTrue(SubjectResolver.hasGlobalBypass(globalContext));
        assertTrue(SubjectResolver.hasFlagBypass(globalContext, FlagKey.of("build")));

        // Admin bypass
        ProtectionSubjectContext adminContext = ProtectionSubjectContext.of(
                subjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.admin"))
        );
        assertTrue(SubjectResolver.hasGlobalBypass(adminContext));

        // Flag bypass
        ProtectionSubjectContext flagContext = ProtectionSubjectContext.of(
                subjects,
                PermissionSet.ofStrings(List.of("worldprotect.bypass.flag.build"))
        );
        assertFalse(SubjectResolver.hasGlobalBypass(flagContext));
        assertTrue(SubjectResolver.hasFlagBypass(flagContext, FlagKey.of("build")));
        assertFalse(SubjectResolver.hasFlagBypass(flagContext, FlagKey.of("place")));

        // Region bypass
        ProtectionSubjectContext regionContext = ProtectionSubjectContext.of(
                subjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.spawn.owner", "worldprotect.region.wild.member"))
        );
        assertFalse(SubjectResolver.hasGlobalBypass(regionContext));
        assertTrue(SubjectResolver.hasRegionOwnerBypass(regionContext, RegionId.of("spawn")));
        assertFalse(SubjectResolver.hasRegionOwnerBypass(regionContext, RegionId.of("wild")));
        assertTrue(SubjectResolver.hasRegionMemberBypass(regionContext, RegionId.of("wild")));
    }
}
