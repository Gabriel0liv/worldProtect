package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.rule.FlagRule;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionFlagsTest {

    @Test
    public void testEmptyFlags() {
        RegionFlags empty = RegionFlags.empty();
        assertTrue(empty.asMap().isEmpty());
        assertTrue(empty.asRuleMap().isEmpty());
        assertFalse(empty.contains(FlagKey.of("build")));
        assertTrue(empty.get(FlagKey.of("build")).isEmpty());
        assertTrue(empty.rule(FlagKey.of("build")).isEmpty());
    }

    @Test
    public void testSimpleStateMapWorks() {
        Map<FlagKey, FlagState> simpleMap = Map.of(
            FlagKey.of("build"), FlagState.ALLOW,
            FlagKey.of("break-block"), FlagState.DENY
        );

        RegionFlags flags = RegionFlags.of(simpleMap);

        assertTrue(flags.contains(FlagKey.of("build")));
        assertTrue(flags.contains(FlagKey.of("break-block")));
        assertEquals(FlagState.ALLOW, flags.get(FlagKey.of("build")).orElse(null));
        assertEquals(FlagState.DENY, flags.get(FlagKey.of("break-block")).orElse(null));

        FlagRule buildRule = flags.rule(FlagKey.of("build")).orElse(null);
        assertNotNull(buildRule);
        assertTrue(buildRule.isSimple());
        assertEquals(FlagState.ALLOW, buildRule.defaultState());
    }

    @Test
    public void testRuleMapWorks() {
        FlagRule rule = FlagRule.simple(FlagState.DENY);
        Map<FlagKey, FlagRule> ruleMap = Map.of(FlagKey.of("build"), rule);

        RegionFlags flags = RegionFlags.ofRules(ruleMap);

        assertTrue(flags.contains(FlagKey.of("build")));
        assertEquals(rule, flags.rule(FlagKey.of("build")).orElse(null));
        assertEquals(FlagState.DENY, flags.get(FlagKey.of("build")).orElse(null));
    }

    @Test
    public void testDefensiveCopy() {
        // Test defensive copying for simple state constructor
        Map<FlagKey, FlagState> stateMap = new HashMap<>();
        FlagKey key = FlagKey.of("build");
        stateMap.put(key, FlagState.ALLOW);

        RegionFlags flagsFromStates = RegionFlags.of(stateMap);
        stateMap.put(key, FlagState.DENY);
        assertEquals(FlagState.ALLOW, flagsFromStates.get(key).orElse(null));

        // Test defensive copying for rule constructor
        Map<FlagKey, FlagRule> ruleMap = new HashMap<>();
        ruleMap.put(key, FlagRule.simple(FlagState.ALLOW));

        RegionFlags flagsFromRules = RegionFlags.ofRules(ruleMap);
        ruleMap.put(key, FlagRule.simple(FlagState.DENY));
        assertEquals(FlagState.ALLOW, flagsFromRules.get(key).orElse(null));
    }

    @Test
    public void testImmutabilityOfReturnedMaps() {
        RegionFlags flags = RegionFlags.of(Map.of(FlagKey.of("build"), FlagState.ALLOW));

        assertThrows(UnsupportedOperationException.class, () -> 
            flags.asMap().put(FlagKey.of("break-block"), FlagState.DENY)
        );

        assertThrows(UnsupportedOperationException.class, () -> 
            flags.asRuleMap().put(FlagKey.of("break-block"), FlagRule.simple(FlagState.DENY))
        );
    }

    @Test
    public void testNullChecks() {
        assertThrows(NullPointerException.class, () -> RegionFlags.of(null));
        assertThrows(NullPointerException.class, () -> RegionFlags.ofRules(null));

        // Map with null key
        Map<FlagKey, FlagState> stateMapWithNullKey = new HashMap<>();
        stateMapWithNullKey.put(null, FlagState.ALLOW);
        assertThrows(NullPointerException.class, () -> RegionFlags.of(stateMapWithNullKey));

        // Map with null value
        Map<FlagKey, FlagState> stateMapWithNullVal = new HashMap<>();
        stateMapWithNullVal.put(FlagKey.of("build"), null);
        assertThrows(NullPointerException.class, () -> RegionFlags.of(stateMapWithNullVal));

        // Rule map with null key
        Map<FlagKey, FlagRule> ruleMapWithNullKey = new HashMap<>();
        ruleMapWithNullKey.put(null, FlagRule.simple(FlagState.ALLOW));
        assertThrows(NullPointerException.class, () -> RegionFlags.ofRules(ruleMapWithNullKey));

        // Rule map with null value
        Map<FlagKey, FlagRule> ruleMapWithNullVal = new HashMap<>();
        ruleMapWithNullVal.put(FlagKey.of("build"), null);
        assertThrows(NullPointerException.class, () -> RegionFlags.ofRules(ruleMapWithNullVal));
    }
}
