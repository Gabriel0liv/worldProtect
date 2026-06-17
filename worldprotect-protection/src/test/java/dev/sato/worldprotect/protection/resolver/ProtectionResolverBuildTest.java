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
import dev.sato.worldprotect.protection.region.GlobalRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
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
 * Tests for build/passthrough semantics resolved through ProtectionResolver
 * and BuildDecisionResolver.
 */
public final class ProtectionResolverBuildTest {

    private DimensionRef overworld;
    private BlockPosRef pos;
    private BlockPosRef min;
    private BlockPosRef max;
    private ProtectionResolver resolver;
    private Actor actor;
    private CauseChain causeChain;
    private UUID playerUuid;
    private UUID ownerUuid;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        pos = new BlockPosRef(5, 5, 5);
        min = new BlockPosRef(0, 0, 0);
        max = new BlockPosRef(10, 10, 10);
        resolver = new ProtectionResolver();
        playerUuid = UUID.randomUUID();
        ownerUuid = UUID.randomUUID();
        actor = new Actor(playerUuid.toString(), ActorType.PLAYER);
        causeChain = CauseChain.of(ProtectionCause.player());
    }

    private ProtectionSubjectContext createContext(UUID uuid) {
        Actor a = new Actor(uuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(a, uuid, List.of());
        return ProtectionSubjectContext.withoutPermissions(actorSubjects);
    }

    private ProtectionQuery query(ProtectionAction action) {
        return new ProtectionQuery(actor, action, causeChain, ProtectionTarget.unknown(), overworld, pos);
    }

    private Region cuboidRegionWithFlags(String id, Map<dev.sato.worldprotect.protection.flag.FlagKey, FlagRule> flags) {
        return new CuboidRegion(
                RegionId.of(id), overworld, min, max, 100,
                RegionFlags.ofRules(flags),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
    }

    // === STEP 1: Specific flags override build fallback ===

    @Test
    public void testSpecificBreakBlockDenyOverridesBuildAllow() {
        // break-block=deny, build=allow -> DENY (specific flag wins)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY),
                        BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.ALLOW)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isDenied(), "Specific break-block=deny should override build=allow");
    }

    @Test
    public void testSpecificBreakBlockAllowOverridesBuildDeny() {
        // break-block=allow, build=deny -> ALLOW (specific flag wins)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW),
                        BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isAllowed(), "Specific break-block=allow should override build=deny");
    }

    @Test
    public void testNoSpecificFlagFallsThroughToBuild() {
        // No break-block flag, build=deny -> DENY (build fallback used)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isDenied(), "Should fall through to build=deny when no specific flag exists");
    }

    // === STEP 2: Passthrough skips build fallback but NOT specific flags ===

    @Test
    public void testPassthroughAllowSkipsBuildFallback() {
        // passthrough=allow, build=deny -> PASS (passthrough skips build fallback)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isPass(), "Passthrough=allow should skip build fallback, resulting in PASS");
    }

    @Test
    public void testPassthroughAllowDoesNotSkipSpecificFlag() {
        // passthrough=allow, break-block=deny -> DENY (passthrough does NOT skip specific flags)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isDenied(), "Passthrough=allow must NOT skip specific break-block=deny");
    }

    @Test
    public void testBuildDenyDeniesBlockInteractWhenSpecificFlagAbsent() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_INTERACT), set);
        assertTrue(d.isDenied(), "build=deny should deny BLOCK_INTERACT when interact-block is absent");
    }

    @Test
    public void testInteractBlockAllowOverridesBuildDeny() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.INTERACT_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW),
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_INTERACT), set);
        assertTrue(d.isAllowed(), "interact-block=allow should override build=deny");
    }

    @Test
    public void testBuildDenyDeniesItemUseOnBlockWhenSpecificFlagsAbsent() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.ITEM_USE_ON_BLOCK), set);
        assertTrue(d.isDenied(), "build=deny should deny ITEM_USE_ON_BLOCK when use-item-on-block/use-item are absent");
    }

    @Test
    public void testUseItemDenyOverridesBuildAllowForItemUseOnBlock() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRule.simple(FlagState.DENY),
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.ALLOW)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.ITEM_USE_ON_BLOCK), set);
        assertTrue(d.isDenied(), "use-item=deny should override build=allow for ITEM_USE_ON_BLOCK");
    }

    @Test
    public void testBuildDenyDeniesPistonMoveWhenSpecificFlagAbsent() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.PISTON_MOVE), set);
        assertTrue(d.isDenied(), "build=deny should deny PISTON_MOVE when piston-move is absent");
    }

    @Test
    public void testBuildDenyDeniesFluidSpreadWhenSpecificFlagAbsent() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.FLUID_SPREAD), set);
        assertTrue(d.isDenied(), "build=deny should deny FLUID_SPREAD when fluid-spread is absent");
    }

    @Test
    public void testBuildDenyDeniesExplosionBlockDamageWhenSpecificFlagAbsent() {
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY)
        ))));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.EXPLOSION_BLOCK_DAMAGE), set);
        assertTrue(d.isDenied(), "build=deny should deny EXPLOSION_BLOCK_DAMAGE when explosion-break-blocks is absent");
    }

    @Test
    public void testPassthroughAllowSkipsBuildFallbackForExpandedBuildActions() {
        List<ProtectionAction> actions = List.of(
                ProtectionAction.BLOCK_INTERACT,
                ProtectionAction.ITEM_USE_ON_BLOCK,
                ProtectionAction.PISTON_MOVE,
                ProtectionAction.FLUID_SPREAD,
                ProtectionAction.EXPLOSION_BLOCK_DAMAGE
        );
        RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY),
                BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
        ))));

        for (ProtectionAction action : actions) {
            ProtectionDecision d = resolver.resolve(query(action), set);
            assertTrue(d.isPass(), "passthrough=allow should skip build fallback for " + action);
        }
    }

    @Test
    public void testPassthroughAllowDoesNotSkipExpandedSpecificFlags() {
        record Case(ProtectionAction action, dev.sato.worldprotect.protection.flag.FlagKey flagKey) {}

        List<Case> cases = List.of(
                new Case(ProtectionAction.BLOCK_INTERACT, BuiltInFlags.INTERACT_BLOCK_KEY),
                new Case(ProtectionAction.ITEM_USE_ON_BLOCK, BuiltInFlags.USE_ITEM_ON_BLOCK_KEY),
                new Case(ProtectionAction.PISTON_MOVE, BuiltInFlags.PISTON_MOVE_KEY),
                new Case(ProtectionAction.FLUID_SPREAD, BuiltInFlags.FLUID_SPREAD_KEY),
                new Case(ProtectionAction.EXPLOSION_BLOCK_DAMAGE, BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY)
        );

        for (Case testCase : cases) {
            RegionSet set = RegionSet.of(List.of(cuboidRegionWithFlags("spawn", Map.of(
                    testCase.flagKey(), FlagRule.simple(FlagState.DENY),
                    BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
            ))));

            ProtectionDecision d = resolver.resolve(query(testCase.action()), set);
            assertTrue(d.isDenied(), "passthrough=allow must not skip specific flag for " + testCase.action());
        }
    }

    @Test
    public void testPassthroughDenyDoesNotSkipBuildFallback() {
        // passthrough=deny, build=deny -> DENY (passthrough deny doesn't skip build)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.DENY)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isDenied(), "Passthrough=deny should not affect build fallback, still DENY");
    }

    // === STEP 3: Implicit membership protection ===

    @Test
    public void testImplicitMembershipDeniesNonMember() {
        // Region with subjects but no explicit build/break flags -> implicit membership build protection
        // Non-member -> implicit DENY
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.of(false, false, Set.of(), Set.of()),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(playerUuid));
        assertTrue(d.isDenied(), "Non-member should be denied by implicit membership build protection");
    }

    @Test
    public void testImplicitMembershipAllowsOwner() {
        // Region with subjects but no explicit build/break flags -> implicit membership build protection
        // Owner -> implicit ALLOW
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(ownerUuid));
        assertTrue(d.isAllowed(), "Owner should be allowed by implicit membership build protection");
    }

    @Test
    public void testImplicitMembershipAllowsMember() {
        // Region with subjects but no explicit build/break flags -> implicit membership build protection
        // Member -> implicit ALLOW
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of(SubjectRef.player(playerUuid))),
                RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(playerUuid));
        assertTrue(d.isAllowed(), "Member should be allowed by implicit membership build protection");
    }

    @Test
    public void testImplicitMembershipSkippedByPassthroughAllow() {
        // passthrough=allow + subjects -> implicit membership is skipped -> PASS
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
                )),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(playerUuid));
        assertTrue(d.isPass(), "Passthrough=allow should skip implicit membership protection -> PASS");
    }

    @Test
    public void testImplicitMembershipNotActiveForEmptySubjects() {
        // Region with no subjects -> implicit membership does not activate -> PASS
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(playerUuid));
        assertTrue(d.isPass(), "Region with no subjects should not activate implicit membership build");
    }

    // === Global region implicit membership exclusion ===

    @Test
    public void testGlobalRegionDoesNotActivateImplicitMembership() {
        // Global region with subjects but no explicit build flags -> PASS (global excluded from implicit)
        Region r = new GlobalRegion(
                RegionId.of("global_ow"), overworld, 0,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.defaults()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(playerUuid));
        assertTrue(d.isPass(), "Global region must NOT activate implicit membership build protection");
    }

    @Test
    public void testGlobalRegionWithExplicitBuildDenyStillDenies() {
        // Global region with build=deny -> should deny via build fallback (step 2)
        Region r = new GlobalRegion(
                RegionId.of("global_ow"), overworld, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isDenied(), "Global region with explicit build=deny should still deny");
    }

    @Test
    public void testEmptyGlobalRegionDoesNotProtect() {
        // Global region with no flags, no subjects -> PASS
        Region r = new GlobalRegion(
                RegionId.of("global_ow"), overworld, 0,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isPass(), "Empty global region should not protect the world");
    }

    // === Access policy bypass on implicit membership deny ===

    @Test
    public void testImplicitMembershipDenyBypassedByOwnerAccessPolicy() {
        // Non-member, but ownersBypassFlags=true + region owner permission bypass
        // Actually let's test: actor has owner role bypass for the region
        // Since we're a non-member, the role-based bypass doesn't apply.
        // But access policy bypass means owners can bypass build flag.
        // For implicit membership, NONE with owner bypass permission should work.
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.defaults(), // ownersBypassFlags=true
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        // Non-member without permissions -> should be denied
        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set, createContext(playerUuid));
        assertTrue(d.isDenied(), "Non-member without bypass should be denied");
    }

    // === Priority interaction ===

    @Test
    public void testHigherPrioritySpecificFlagOverridesLowerPriorityBuildDeny() {
        // High-priority: break-block=allow
        // Low-priority: build=deny
        // -> ALLOW (higher priority wins, specific flag resolves first)
        Region highPri = new CuboidRegion(
                RegionId.of("vip"), overworld, min, max, 200,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        Region lowPri = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(highPri, lowPri));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isAllowed(), "Higher-priority specific flag should override lower-priority build deny");
    }

    @Test
    public void testSamePrioritySpecificDenyBeatsBuildAllow() {
        // Same priority: break-block=deny in one region, build=allow in another
        // Step 1 evaluates specific flags first -> DENY wins
        Region r1 = new CuboidRegion(
                RegionId.of("r1"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        Region r2 = new CuboidRegion(
                RegionId.of("r2"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.ALLOW))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r1, r2));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isDenied(), "Specific deny should take precedence over build allow at same priority (step 1 before step 2)");
    }

    // === Non-build actions remain unaffected ===

    @Test
    public void testNonBuildActionNotAffectedByPassthrough() {
        // passthrough + build should not affect container_open
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.OPEN_CONTAINER_KEY, FlagRule.simple(FlagState.DENY),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionQuery q = new ProtectionQuery(actor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        ProtectionDecision d = resolver.resolve(q, set);
        assertTrue(d.isDenied(), "Non-build action (CONTAINER_OPEN) should not be affected by passthrough");
    }

    @Test
    public void testNonBuildActionNotAffectedByImplicitMembership() {
        // Region with subjects but no container open flag -> PASS (implicit membership is build-only)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionQuery q = new ProtectionQuery(actor, ProtectionAction.CONTAINER_OPEN, causeChain, ProtectionTarget.unknown(), overworld, pos);
        ProtectionDecision d = resolver.resolve(q, set, createContext(playerUuid));
        assertTrue(d.isPass(), "Non-build action should not trigger implicit membership protection");
    }

    // === Generic BUILD action ===

    @Test
    public void testGenericBuildActionUsesOnlyBuildFallback() {
        // BUILD action has no specific flags, only uses build fallback
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BUILD), set);
        assertTrue(d.isDenied(), "Generic BUILD action should use build fallback");
    }

    @Test
    public void testGenericBuildActionPassthroughAllowSkipsFallback() {
        // BUILD + passthrough=allow -> PASS
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BUILD_KEY, FlagRule.simple(FlagState.DENY),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.ALLOW)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BUILD), set);
        assertTrue(d.isPass(), "Generic BUILD action with passthrough=allow should skip fallback -> PASS");
    }

    // === Passthrough does not directly deny/allow unrelated actions ===

    @Test
    public void testPassthroughDoesNotDenyByItself() {
        // Only passthrough=deny, no other flags -> PASS for build actions
        // (passthrough controls participation, it doesn't deny by itself)
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRule.simple(FlagState.DENY)
                )),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isPass(), "Passthrough=deny alone should not deny; it controls participation, not protection");
    }

    // === Build action with no context -> no implicit membership ===

    @Test
    public void testBuildWithoutSubjectContextSkipsImplicitMembership() {
        // Region with subjects, no explicit flags, no subject context
        Region r = new CuboidRegion(
                RegionId.of("spawn"), overworld, min, max, 100,
                RegionFlags.empty(),
                RegionSubjects.of(Set.of(SubjectRef.player(ownerUuid)), Set.of()),
                RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        RegionSet set = RegionSet.of(List.of(r));

        // No subject context -> implicit membership check is skipped -> PASS
        ProtectionDecision d = resolver.resolve(query(ProtectionAction.BLOCK_BREAK), set);
        assertTrue(d.isPass(), "Without subject context, implicit membership check should not activate");
    }
}
