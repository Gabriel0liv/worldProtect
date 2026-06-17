package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.rule.ResourceSelector;
import dev.sato.worldprotect.protection.rule.ResourceSelectorSet;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.resolver.ProtectionResolver;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigToDomainMapperTest {

    private ConfigToDomainMapper mapper;
    private DimensionRef overworld;
    private BoundsConfig bounds;

    @BeforeEach
    public void setUp() {
        mapper = new ConfigToDomainMapper();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
    }

    @Test
    public void testSimpleDenyFlagMapsToFlagRuleSimple() {
        FlagRuleConfig config = FlagRuleConfig.simple(FlagState.DENY);
        FlagRule rule = mapper.toFlagRule(config);
        
        assertTrue(rule.isSimple());
        assertEquals(FlagState.DENY, rule.defaultState());
    }

    @Test
    public void testConditionalRuleMapsSelectorsCorrectly() {
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("create:wrench"),
                List.of("botania:twig_wand")
        );

        FlagRule rule = mapper.toFlagRule(config);
        assertFalse(rule.isSimple());
        assertEquals(FlagState.DENY, rule.defaultState());
        
        ResourceSelectorSet allow = rule.allowSelectors();
        assertEquals(1, allow.selectors().size());
        assertEquals(ResourceSelector.parse("create:wrench"), allow.selectors().get(0));

        ResourceSelectorSet deny = rule.denySelectors();
        assertEquals(1, deny.selectors().size());
        assertEquals(ResourceSelector.parse("botania:twig_wand"), deny.selectors().get(0));
    }

    @Test
    public void testConfigMapsToRegionSetWithCuboidRegion() {
        RegionConfig rConfig = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
        ));
        WorldProtectConfig wpConfig = WorldProtectConfig.of(List.of(rConfig));

        RegionSet regionSet = mapper.toRegionSet(wpConfig);
        assertEquals(1, regionSet.regions().size());
        
        assertTrue(regionSet.regions().get(0) instanceof CuboidRegion);
        CuboidRegion region = (CuboidRegion) regionSet.regions().get(0);
        assertEquals(RegionId.of("spawn"), region.getId());
        assertEquals(overworld, region.getDimension());
        assertEquals(100, region.getPriority());
        assertTrue(region.contains(new BlockPosRef(0, 0, 0)));
        assertTrue(region.contains(new BlockPosRef(10, 10, 10)));
        assertFalse(region.contains(new BlockPosRef(-1, 0, 0)));
        assertFalse(region.contains(new BlockPosRef(0, 0, 11)));
        assertEquals(FlagState.DENY, region.flags().get(BuiltInFlags.BREAK_BLOCK_KEY).orElse(null));
    }

    @Test
    public void testMappedRegionSetCanBeUsedWithProtectionResolver() {
        RegionConfig rConfig = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("create:wrench"), List.of())
        ));
        WorldProtectConfig wpConfig = WorldProtectConfig.of(List.of(rConfig));
        RegionSet regionSet = mapper.toRegionSet(wpConfig);

        ProtectionResolver resolver = new ProtectionResolver();
        Actor actor = new Actor("player1", ActorType.PLAYER);
        BlockPosRef pos = new BlockPosRef(5, 5, 5);
        
        // Query ITEM_USE for create:wrench (allowed)
        CauseChain causeWrench = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "wrench")));
        ProtectionQuery queryWrench = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causeWrench, ProtectionTarget.unknown(), overworld, pos);
        
        assertTrue(resolver.resolve(queryWrench, regionSet).isAllowed());

        // Query ITEM_USE for minecraft:stick (denied by default state)
        CauseChain causeStick = CauseChain.of(ProtectionCause.item(ResourceRef.of("minecraft", "stick")));
        ProtectionQuery queryStick = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causeStick, ProtectionTarget.unknown(), overworld, pos);
        
        assertTrue(resolver.resolve(queryStick, regionSet).isDenied());
    }

    @Test
    public void testInvalidSelectorThrowsDuringMappingIfValidationWasSkipped() {
        // Factory allows creating with selector strings as structural syntax check is only done in validate() or domain parse
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("invalid space"),
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            mapper.toFlagRule(config);
        });
    }

    @Test
    public void testMapsSubjectsAndAccessPolicyIntoCuboidRegion() {
        UUID ownerUuid = UUID.randomUUID();
        RegionSubjectsConfig subjects = RegionSubjectsConfig.of(
                List.of(SubjectRefConfig.of("player:" + ownerUuid)),
                List.of(SubjectRefConfig.of("group:trusted"))
        );
        RegionAccessPolicyConfig access = RegionAccessPolicyConfig.of(
                false,
                true,
                List.of("break-block"),
                List.of("place-block")
        );

        RegionConfig rConfig = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, Map.of(), subjects, access);
        WorldProtectConfig wpConfig = WorldProtectConfig.of(List.of(rConfig));

        RegionSet regionSet = mapper.toRegionSet(wpConfig);
        CuboidRegion region = (CuboidRegion) regionSet.regions().get(0);

        assertTrue(region.subjects().isOwner(SubjectRef.player(ownerUuid)));
        assertTrue(region.subjects().isMember(SubjectRef.group("trusted")));
        assertFalse(region.accessPolicy().ownersBypassFlags());
        assertTrue(region.accessPolicy().membersBypassFlags());
        assertTrue(region.accessPolicy().ownerBypassFlags().contains(BuiltInFlags.BREAK_BLOCK_KEY));
        assertTrue(region.accessPolicy().memberBypassFlags().contains(BuiltInFlags.PLACE_BLOCK_KEY));
    }

    @Test
    public void testOwnerMemberLoadedFromConfigAffectsProtectionResolver() {
        UUID ownerUuid = UUID.randomUUID();
        RegionSubjectsConfig subjects = RegionSubjectsConfig.of(
                List.of(SubjectRefConfig.of("player:" + ownerUuid)),
                List.of()
        );
        // RegionAccessPolicy defaults to ownerBypass = true
        RegionAccessPolicyConfig access = RegionAccessPolicyConfig.defaults();

        // Spawn region denies break block
        RegionConfig rConfig = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
        ), subjects, access);
        WorldProtectConfig wpConfig = WorldProtectConfig.of(List.of(rConfig));
        RegionSet regionSet = mapper.toRegionSet(wpConfig);

        ProtectionResolver resolver = new ProtectionResolver();
        Actor playerActor = new Actor(ownerUuid.toString(), ActorType.PLAYER);
        CauseChain cause = CauseChain.of(ProtectionCause.player());
        BlockPosRef pos = new BlockPosRef(5, 5, 5);
        ProtectionQuery query = new ProtectionQuery(playerActor, ProtectionAction.BLOCK_BREAK, cause, ProtectionTarget.unknown(), overworld, pos);

        // Without subject context, action is denied
        assertTrue(resolver.resolve(query, regionSet).isDenied());

        // With subject context, player is mapped as owner, owner bypasses DENY -> ALLOW
        dev.sato.worldprotect.protection.subject.ActorSubjects actorSubjects = dev.sato.worldprotect.protection.subject.ActorSubjects.player(playerActor, ownerUuid, List.of());
        dev.sato.worldprotect.protection.permission.ProtectionSubjectContext context = dev.sato.worldprotect.protection.permission.ProtectionSubjectContext.withoutPermissions(actorSubjects);
        assertTrue(resolver.resolve(query, regionSet, context).isAllowed());
    }
}
