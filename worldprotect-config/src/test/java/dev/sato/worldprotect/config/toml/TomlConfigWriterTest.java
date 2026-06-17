package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.RegionSubjectsConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class TomlConfigWriterTest {

    private TomlConfigWriter writer;
    private TomlConfigParser parser;
    private DimensionRef overworld;

    @BeforeEach
    public void setUp() {
        writer = new TomlConfigWriter();
        parser = new TomlConfigParser();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
    }

    @Test
    public void testWritesCuboidRegionAndParsesBack() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(
                        RegionId.of("spawn"),
                        overworld,
                        100,
                        BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10)),
                        Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY))
                )
        ));

        String content = writer.write(config).content().orElseThrow();

        assertTrue(content.contains("[regions.spawn]"));
        assertTrue(content.contains("type = \"cuboid\""));
        assertTrue(parser.parseString(content).isSuccess());
    }

    @Test
    public void testWritesGlobalRegionParentSubjectsAccessAndFlags() {
        RegionConfig region = RegionConfig.of(
                RegionId.of("global_overworld"),
                overworld,
                -100,
                BoundsConfig.global(),
                Map.of(
                        BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.DENY),
                        BuiltInFlags.INTERACT_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY, RegionGroup.NONMEMBERS),
                        BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("minecraft:oak_door"), List.of("create:wrench"), RegionGroup.MEMBERS),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRuleConfig.simple(FlagState.ALLOW)
                ),
                RegionSubjectsConfig.of(
                        List.of(SubjectRefConfig.of("player:00000000-0000-0000-0000-000000000000")),
                        List.of(SubjectRefConfig.of("group:trusted"))
                ),
                RegionAccessPolicyConfig.of(false, true, List.of("build"), List.of("interact-block")),
                Optional.of(RegionId.of("parent"))
        );

        String content = writer.write(WorldProtectConfig.of(List.of(region))).content().orElseThrow();

        assertTrue(content.contains("type = \"global\""));
        assertTrue(content.contains("parent = \"parent\""));
        assertTrue(content.contains("owners = [\"player:00000000-0000-0000-0000-000000000000\"]"));
        assertTrue(content.contains("members = [\"group:trusted\"]"));
        assertTrue(content.contains("owners-bypass = false"));
        assertTrue(content.contains("[regions.global_overworld.flags.interact-block]"));
        assertTrue(content.contains("group = \"nonmembers\""));
        assertTrue(content.contains("[regions.global_overworld.flags.use-item-on-block]"));
        assertTrue(parser.parseString(content).isSuccess());
    }

    @Test
    public void testEscapesQuotesAndBackslashes() {
        RegionConfig region = RegionConfig.of(
                RegionId.of("spawn"),
                overworld,
                0,
                BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(1, 1, 1)),
                Map.of(BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("mod:quoted\"item", "mod:path\\item"), List.of()))
        );

        String content = writer.write(WorldProtectConfig.of(List.of(region))).content().orElseThrow();

        assertTrue(content.contains("mod:quoted\\\"item"));
        assertTrue(content.contains("mod:path\\\\item"));
    }

    @Test
    public void testDeterministicOutput() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(
                        RegionId.of("spawn"),
                        overworld,
                        100,
                        BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10)),
                        Map.of(
                                BuiltInFlags.PLACE_BLOCK_KEY, FlagRuleConfig.simple(FlagState.ALLOW),
                                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
                        ),
                        RegionSubjectsConfig.of(
                                List.of(SubjectRefConfig.of("group:zeta"), SubjectRefConfig.of("group:alpha")),
                                List.of(SubjectRefConfig.of("group:member_b"), SubjectRefConfig.of("group:member_a"))
                        ),
                        RegionAccessPolicyConfig.defaults()
                )
        ));

        String first = writer.write(config).content().orElseThrow();
        String second = writer.write(config).content().orElseThrow();

        assertEquals(first, second);
        assertTrue(first.indexOf("break-block") < first.indexOf("place-block"));
        assertTrue(first.indexOf("group:alpha") < first.indexOf("group:zeta"));
    }

    @Test
    public void testWritesEmptyRegionsTableWhenNoRegions() {
        String content = writer.write(WorldProtectConfig.of(List.of())).content().orElseThrow();

        assertEquals("[regions]\n", content);
    }
}
