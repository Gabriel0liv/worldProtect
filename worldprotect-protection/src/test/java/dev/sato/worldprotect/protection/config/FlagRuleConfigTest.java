package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.flag.FlagState;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class FlagRuleConfigTest {

    @Test
    public void testSimpleRuleValidates() {
        FlagRuleConfig config = FlagRuleConfig.simple(FlagState.ALLOW);
        assertTrue(config.isSimple());
        assertEquals(FlagState.ALLOW, config.defaultState());
        assertTrue(config.allowSelectors().isEmpty());
        assertTrue(config.denySelectors().isEmpty());

        ConfigValidationResult result = config.validate("flags.break-block");
        assertTrue(result.isValid());
    }

    @Test
    public void testConditionalRuleValidatesExactSelectors() {
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("create:wrench", "minecraft:iron_pickaxe"),
                List.of()
        );
        assertFalse(config.isSimple());
        
        ConfigValidationResult result = config.validate("flags.use-item");
        assertTrue(result.isValid());
    }

    @Test
    public void testConditionalRuleValidatesNamespaceWildcard() {
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("create:*"),
                List.of("minecraft:*")
        );
        ConfigValidationResult result = config.validate("flags.use-item");
        assertTrue(result.isValid());
    }

    @Test
    public void testConditionalRuleValidatesGlobalWildcard() {
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("*"),
                List.of()
        );
        ConfigValidationResult result = config.validate("flags.use-item");
        assertTrue(result.isValid());
    }

    @Test
    public void testConditionalRuleValidatesTagSyntax() {
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("#forge:ores"),
                List.of()
        );
        ConfigValidationResult result = config.validate("flags.use-item");
        assertTrue(result.isValid());
    }

    @Test
    public void testInvalidSelectorCreatesValidationErrorNotException() {
        // Syntax parsing checks are in validate(), not the factory (assuming valid strings structurally)
        FlagRuleConfig config = FlagRuleConfig.conditional(
                FlagState.DENY,
                List.of("invalid space selector", "create:wrench"),
                List.of("botania:twig_wand", "invalid::colons")
        );

        ConfigValidationResult result = config.validate("flags.use-item");
        assertFalse(result.isValid());
        assertEquals(2, result.errors().size());
        
        assertEquals("flags.use-item.allow[0]", result.errors().get(0).path());
        assertEquals("flags.use-item.deny[1]", result.errors().get(1).path());
    }

    @Test
    public void testNullOrBlankSelectorsRejectedImmediately() {
        // Factories must reject null/blank values on creation
        assertThrows(NullPointerException.class, () -> {
            FlagRuleConfig.conditional(FlagState.DENY, null, List.of("botania:twig_wand"));
        });

        assertThrows(NullPointerException.class, () -> {
            List<String> allow = new ArrayList<>();
            allow.add(null);
            FlagRuleConfig.conditional(FlagState.DENY, allow, List.of("botania:twig_wand"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            FlagRuleConfig.conditional(FlagState.DENY, List.of("   "), List.of("botania:twig_wand"));
        });
    }
}
