package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
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
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class ProtectionResolverGlobalRegionTest {

    private DimensionRef overworld;
    private DimensionRef nether;
    private DimensionRef end;
    private Actor actor;
    private CauseChain causeChain;
    private ProtectionTarget target;
    private ProtectionResolver resolver;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        nether = new DimensionRef(ResourceRef.of("minecraft", "the_nether"));
        end = new DimensionRef(ResourceRef.of("minecraft", "the_end"));
        actor = new Actor("player1", ActorType.PLAYER);
        causeChain = CauseChain.of(ProtectionCause.player());
        target = ProtectionTarget.unknown();
        resolver = new ProtectionResolver();
    }

    @Test
    public void testGlobalDenyPriorityOverride() {
        // global deny priority -1000000
        Region globalRegion = new GlobalRegion(
                RegionId.of("global_overworld"),
                overworld,
                -1000000,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        // cuboid allow priority 100
        Region cuboidRegion = new CuboidRegion(
                RegionId.of("spawn_cuboid"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                100,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW))
        );

        RegionSet set = RegionSet.of(List.of(globalRegion, cuboidRegion));

        // Posição dentro do cuboid => ALLOW
        BlockPosRef inside = new BlockPosRef(5, 5, 5);
        ProtectionQuery queryInside = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, inside);
        ProtectionDecision decisionInside = resolver.resolve(queryInside, set);
        assertTrue(decisionInside.isAllowed());

        // Posição fora do cuboid => DENY
        BlockPosRef outside = new BlockPosRef(20, 20, 20);
        ProtectionQuery queryOutside = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, outside);
        ProtectionDecision decisionOutside = resolver.resolve(queryOutside, set);
        assertTrue(decisionOutside.isDenied());
    }

    @Test
    public void testGlobalDimensionIsolation() {
        Region globalOverworld = new GlobalRegion(
                RegionId.of("global_overworld"),
                overworld,
                10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        Region globalNether = new GlobalRegion(
                RegionId.of("global_nether"),
                nether,
                20,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW)),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(globalOverworld, globalNether));
        BlockPosRef pos = new BlockPosRef(5, 5, 5);

        // Query in Overworld => DENY (from globalOverworld)
        ProtectionQuery queryOverworld = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);
        assertTrue(resolver.resolve(queryOverworld, set).isDenied());

        // Query in Nether => ALLOW (from globalNether)
        ProtectionQuery queryNether = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, nether, pos);
        assertTrue(resolver.resolve(queryNether, set).isAllowed());

        // Query in End => PASS (dimension doesn't match either)
        ProtectionQuery queryEnd = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, end, pos);
        assertTrue(resolver.resolve(queryEnd, set).isPass());
    }

    @Test
    public void testEqualPriorityDenyBeatsAllow() {
        Region globalDeny = new GlobalRegion(
                RegionId.of("global_deny"),
                overworld,
                10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        Region globalAllow = new GlobalRegion(
                RegionId.of("global_allow"),
                overworld,
                10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW)),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(globalDeny, globalAllow));
        BlockPosRef pos = new BlockPosRef(5, 5, 5);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        // Equal priority: DENY beats ALLOW
        assertTrue(resolver.resolve(query, set).isDenied());
    }

    @Test
    public void testSubjectBypassOnGlobalRegion() {
        UUID ownerUuid = UUID.randomUUID();
        RegionSubjects subjects = RegionSubjects.empty()
                .withOwner(SubjectRef.player(ownerUuid));

        Region globalRegion = new GlobalRegion(
                RegionId.of("global_overworld"),
                overworld,
                0,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)),
                subjects,
                RegionAccessPolicy.defaults() // ownerBypass = true by default
        );

        RegionSet set = RegionSet.of(List.of(globalRegion));
        BlockPosRef pos = new BlockPosRef(5, 5, 5);
        Actor ownerActor = new Actor(ownerUuid.toString(), ActorType.PLAYER);
        ProtectionQuery query = new ProtectionQuery(ownerActor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, pos);

        // 1. Without context: DENY
        assertTrue(resolver.resolve(query, set).isDenied());

        // 2. With context: ALLOW due to owner bypass on global region
        ActorSubjects actorSubjects = ActorSubjects.player(ownerActor, ownerUuid, List.of());
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);
        assertTrue(resolver.resolve(query, set, context).isAllowed());
    }
}
