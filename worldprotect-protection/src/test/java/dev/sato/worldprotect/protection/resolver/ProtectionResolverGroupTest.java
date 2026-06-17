package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RegionGroup-scoped flag rules resolved through ProtectionResolver.
 * Uses non-build-related actions (CONTAINER_OPEN -> open-container) to isolate
 * group scoping behavior from build/passthrough semantics.
 */
public final class ProtectionResolverGroupTest {

    private DimensionRef overworld;
    private BlockPosRef pos;
    private BlockPosRef min;
    private BlockPosRef max;
    private Actor actor;
    private CauseChain causeChain;
    private UUID playerUuid;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        pos = new BlockPosRef(5, 5, 5);
        min = new BlockPosRef(0, 0, 0);
        max = new BlockPosRef(10, 10, 10);
        playerUuid = UUID.randomUUID();
        actor = new Actor(playerUuid.toString(), ActorType.PLAYER);
        causeChain = CauseChain.of(ProtectionCause.player());
    }

    private ProtectionSubjectContext createContext(UUID uuid) {
        ActorSubjects actorSubjects = ActorSubjects.player(actor, uuid, List.of());
        return ProtectionSubjectContext.withoutPermissions(actorSubjects);
    }

    @Test
    public void testOwnersGroupDeniesNonOwners() {
        // OWNERS only -> deny open-container (non-build action to isolate group scoping)
        FlagRule rule = FlagRule.simple(FlagState.DENY, RegionGroup.OWNERS);
        RegionSubjects subjects = RegionSubjects.of(Set.of(SubjectRef.player(playerUuid)), Set.of()); // owner is playerUuid
        
        ProtectionResolver resolver = new ProtectionResolver();

        // With ownerBypass = false so the DENY flag rule is evaluated for the owner
        Region rNoBypass = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                min,
                max,
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.OPEN_CONTAINER_KEY, rule)),
                subjects,
                RegionAccessPolicy.of(false, false, Set.of(), Set.of()),
                Optional.empty()
        );
        RegionSet setNoBypass = RegionSet.of(List.of(rNoBypass));

        // When actor is owner, flag matches because they are OWNER, so it is DENIED
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        assertTrue(resolver.resolve(query, setNoBypass, createContext(playerUuid)).isDenied());

        // Scenario 2: Actor is not owner (other player uuid)
        UUID otherUuid = UUID.randomUUID();
        Actor otherActor = new Actor(otherUuid.toString(), ActorType.PLAYER);
        ProtectionQuery otherQuery = new ProtectionQuery(otherActor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        ActorSubjects otherSubjects = ActorSubjects.player(otherActor, otherUuid, List.of());
        ProtectionSubjectContext otherContext = ProtectionSubjectContext.withoutPermissions(otherSubjects);

        // When actor is not owner, flag rule does not match, resolver gets PASS
        assertTrue(resolver.resolve(otherQuery, setNoBypass, otherContext).isPass());
    }

    @Test
    public void testMembersGroupMatchesOwnersAndMembers() {
        // MEMBERS group -> deny open-container
        FlagRule rule = FlagRule.simple(FlagState.DENY, RegionGroup.MEMBERS);
        
        // playerUuid is member, otherUuid is owner
        UUID ownerUuid = UUID.randomUUID();
        RegionSubjects subjects = RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of(SubjectRef.player(playerUuid)));

        Region r = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                min,
                max,
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.OPEN_CONTAINER_KEY, rule)),
                subjects,
                RegionAccessPolicy.of(false, false, Set.of(), Set.of()),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionResolver resolver = new ProtectionResolver();
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);

        // playerUuid is member -> matches MEMBERS -> DENIED
        assertTrue(resolver.resolve(query, set, createContext(playerUuid)).isDenied());

        // otherUuid is owner -> matches MEMBERS -> DENIED
        Actor ownerActor = new Actor(ownerUuid.toString(), ActorType.PLAYER);
        ProtectionQuery ownerQuery = new ProtectionQuery(ownerActor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        ActorSubjects ownerSubjects = ActorSubjects.player(ownerActor, ownerUuid, List.of());
        ProtectionSubjectContext ownerContext = ProtectionSubjectContext.withoutPermissions(ownerSubjects);
        assertTrue(resolver.resolve(ownerQuery, set, ownerContext).isDenied());

        // non-member -> does not match MEMBERS -> PASS
        UUID nonUuid = UUID.randomUUID();
        Actor nonActor = new Actor(nonUuid.toString(), ActorType.PLAYER);
        ProtectionQuery nonQuery = new ProtectionQuery(nonActor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        ActorSubjects nonSubjects = ActorSubjects.player(nonActor, nonUuid, List.of());
        ProtectionSubjectContext nonContext = ProtectionSubjectContext.withoutPermissions(nonSubjects);
        assertTrue(resolver.resolve(nonQuery, set, nonContext).isPass());
    }

    @Test
    public void testNonMembersGroupOnlyMatchesNone() {
        // NONMEMBERS group -> deny open-container
        FlagRule rule = FlagRule.simple(FlagState.DENY, RegionGroup.NONMEMBERS);
        RegionSubjects subjects = RegionSubjects.of(Set.of(), Set.of(SubjectRef.player(playerUuid))); // playerUuid is member

        Region r = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                min,
                max,
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.OPEN_CONTAINER_KEY, rule)),
                subjects,
                RegionAccessPolicy.of(false, false, Set.of(), Set.of()),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionResolver resolver = new ProtectionResolver();
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);

        // playerUuid is member -> does not match NONMEMBERS -> PASS
        assertTrue(resolver.resolve(query, set, createContext(playerUuid)).isPass());

        // non-member UUID -> matches NONMEMBERS -> DENIED
        UUID nonUuid = UUID.randomUUID();
        Actor nonActor = new Actor(nonUuid.toString(), ActorType.PLAYER);
        ProtectionQuery nonQuery = new ProtectionQuery(nonActor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        ActorSubjects nonSubjects = ActorSubjects.player(nonActor, nonUuid, List.of());
        ProtectionSubjectContext nonContext = ProtectionSubjectContext.withoutPermissions(nonSubjects);
        assertTrue(resolver.resolve(nonQuery, set, nonContext).isDenied());
    }
}
