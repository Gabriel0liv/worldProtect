package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.permission.PermissionSet;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class ProtectionResolverSubjectTest {

    private DimensionRef overworld;
    private BlockPosRef pos;
    private Actor actor;
    private CauseChain causeChain;
    private ProtectionTarget target;
    private ProtectionResolver resolver;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        pos = new BlockPosRef(5, 5, 5);
        actor = new Actor("player1", ActorType.PLAYER);
        causeChain = CauseChain.of(ProtectionCause.player());
        target = ProtectionTarget.unknown();
        resolver = new ProtectionResolver();
    }

    @Test
    public void testOldResolveApiBehavesUnchanged() {
        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );
        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isDenied());
        assertTrue(decision.reason().contains("denied by flag"));
    }

    @Test
    public void testGlobalBypassAllowsDeniedRegion() {
        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );
        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ActorSubjects actorSubjects = ActorSubjects.console(actor);
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.bypass"))
        );

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("global bypass"));
    }

    @Test
    public void testFlagBypassAllowsDeniedFlag() {
        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );
        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ActorSubjects actorSubjects = ActorSubjects.console(actor);
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.bypass.flag.break-block"))
        );

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("flag bypass"));
    }

    @Test
    public void testOwnerBypassesDeniedFlagByDefault() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);

        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty().withOwner(SubjectRef.player(uuid)),
                RegionAccessPolicy.defaults() // Owners bypass everything by default
        );

        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("region owner bypass"));
    }

    @Test
    public void testMemberDoesNotBypassByDefault() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);

        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty().withMember(SubjectRef.player(uuid)),
                RegionAccessPolicy.defaults() // Members do not bypass by default
        );

        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isDenied());
    }

    @Test
    public void testMemberBypassesWithCustomPolicy() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);

        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty().withMember(SubjectRef.player(uuid)),
                RegionAccessPolicy.of(true, false, Set.of(), Set.of(BuiltInFlags.BREAK_BLOCK_KEY))
        );

        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("region member bypass"));
    }

    @Test
    public void testHigherPriorityDenyBeatsLowerPriorityOwnerBypass() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);

        // Region A at priority 100: Denies break block, player has no roles
        CuboidRegion regionA = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        // Region B at priority 50: Denies break block, player is owner (and owner bypass is active)
        CuboidRegion regionB = new CuboidRegion(
                RegionId.of("spawn-lower"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                50,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty().withOwner(SubjectRef.player(uuid)),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(regionA, regionB));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        // Higher priority deny should take precedence and immediately return DENY
        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isDenied());
        assertEquals("spawn", decision.regionId().get().getValue());
    }

    @Test
    public void testSamePriorityDenyBeatsRoleBypass() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);

        // Region A at priority 100: Denies break block, player has no roles
        CuboidRegion regionA = new CuboidRegion(
                RegionId.of("spawn-a"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        // Region B at priority 100: Denies break block, player is owner
        CuboidRegion regionB = new CuboidRegion(
                RegionId.of("spawn-b"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty().withOwner(SubjectRef.player(uuid)),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(regionA, regionB));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        // Same priority: DENY from spawn-a beats OWNER bypass from spawn-b
        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isDenied());
        assertEquals("spawn-a", decision.regionId().get().getValue());
    }

    @Test
    public void testRegionOwnerBypassPermissionAllowsDeniedFlagInThatRegion() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());
        
        // Context grants region owner bypass permission for "spawn"
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.spawn.owner"))
        );

        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(), // Player is not actually role owner
                RegionAccessPolicy.defaults() // Owner bypass active by default
        );

        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("region owner bypass permission"));
    }

    @Test
    public void testRegionMemberBypassPermissionAllowsDeniedFlagOnlyWhenPolicyAllows() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());

        // Context grants region member bypass permission for "spawn"
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.spawn.member"))
        );

        // Access policy does NOT allow member bypass for BREAK_BLOCK by default
        CuboidRegion regionNoBypass = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet setNoBypass = RegionSet.of(List.of(regionNoBypass));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decisionNoBypass = resolver.resolve(query, setNoBypass, context);
        assertTrue(decisionNoBypass.isDenied());

        // Now with custom policy allowing member bypass for BREAK_BLOCK
        CuboidRegion regionWithBypass = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.of(true, false, Set.of(), Set.of(BuiltInFlags.BREAK_BLOCK_KEY))
        );

        RegionSet setWithBypass = RegionSet.of(List.of(regionWithBypass));
        ProtectionDecision decisionWithBypass = resolver.resolve(query, setWithBypass, context);
        assertTrue(decisionWithBypass.isAllowed());
        assertTrue(decisionWithBypass.reason().contains("region member bypass permission"));
    }

    @Test
    public void testRegionOwnerBypassPermissionForSpawnLowerDoesNotBypassSpawn() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());

        // Context grants region owner bypass permission for "spawn-lower" only
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.spawn-lower.owner"))
        );

        // Evaluated region is "spawn"
        CuboidRegion region = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isDenied());
        assertEquals("spawn", decision.regionId().get().getValue());
    }

    @Test
    public void testHigherPriorityDenyBeatsLowerPriorityRegionOwnerPermissionBypass() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());

        // Context grants region owner bypass permission for lower priority region "spawn-lower"
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.spawn-lower.owner"))
        );

        // Region A at priority 100: Denies break block, player has no roles or permission bypasses
        CuboidRegion regionA = new CuboidRegion(
                RegionId.of("spawn"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        // Region B at priority 50: Denies break block, player has owner bypass permission for this region
        CuboidRegion regionB = new CuboidRegion(
                RegionId.of("spawn-lower"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                50,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(regionA, regionB));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        // Higher priority deny on "spawn" must override the owner permission bypass on "spawn-lower"
        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isDenied());
        assertEquals("spawn", decision.regionId().get().getValue());
    }

    @Test
    public void testSamePriorityDenyBeatsRegionOwnerPermissionBypassFromAnotherRegion() {
        UUID uuid = UUID.randomUUID();
        Actor playerActor = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, uuid, List.of());

        // Context grants region owner bypass permission for "spawn-b"
        ProtectionSubjectContext context = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.spawn-b.owner"))
        );

        // Region A at priority 100: Denies break block, player has no permissions
        CuboidRegion regionA = new CuboidRegion(
                RegionId.of("spawn-a"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        // Region B at priority 100: Denies break block, player has owner bypass permission
        CuboidRegion regionB = new CuboidRegion(
                RegionId.of("spawn-b"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(regionA, regionB));
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        // Same priority: DENY from spawn-a beats OWNER permission bypass from spawn-b
        ProtectionDecision decision = resolver.resolve(query, set, context);
        assertTrue(decision.isDenied());
        assertEquals("spawn-a", decision.regionId().get().getValue());
    }
}
