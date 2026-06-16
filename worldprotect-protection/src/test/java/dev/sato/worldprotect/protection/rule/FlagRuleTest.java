package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.FlagState;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class FlagRuleTest {

    @Test
    public void testSimpleAllowResolvesAllow() {
        FlagRule rule = FlagRule.simple(FlagState.ALLOW);
        assertTrue(rule.isSimple());
        assertEquals(FlagState.ALLOW, rule.resolve(ResourceRef.of("minecraft", "stone")));
        assertEquals(FlagState.ALLOW, rule.resolve(null));
    }

    @Test
    public void testSimpleDenyResolvesDeny() {
        FlagRule rule = FlagRule.simple(FlagState.DENY);
        assertTrue(rule.isSimple());
        assertEquals(FlagState.DENY, rule.resolve(ResourceRef.of("minecraft", "stone")));
        assertEquals(FlagState.DENY, rule.resolve(null));
    }

    @Test
    public void testDefaultDenyAllowSpecific() {
        ResourceSelector selector = ResourceSelector.parse("create:wrench");
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(selector));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        assertFalse(rule.isSimple());
        assertEquals(FlagState.ALLOW, rule.resolve(ResourceRef.of("create", "wrench")));
        assertEquals(FlagState.DENY, rule.resolve(ResourceRef.of("minecraft", "stone")));
    }

    @Test
    public void testDefaultDenyAllowNamespaceWildcard() {
        ResourceSelector selector = ResourceSelector.parse("create:*");
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(selector));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        assertEquals(FlagState.ALLOW, rule.resolve(ResourceRef.of("create", "wrench")));
        assertEquals(FlagState.ALLOW, rule.resolve(ResourceRef.of("create", "mechanical_press")));
        assertEquals(FlagState.DENY, rule.resolve(ResourceRef.of("minecraft", "stone")));
    }

    @Test
    public void testDefaultAllowDenySpecific() {
        ResourceSelector selector = ResourceSelector.parse("create:creative_motor");
        ResourceSelectorSet deny = ResourceSelectorSet.of(List.of(selector));
        FlagRule rule = FlagRule.conditional(FlagState.ALLOW, ResourceSelectorSet.empty(), deny);

        assertEquals(FlagState.DENY, rule.resolve(ResourceRef.of("create", "creative_motor")));
        assertEquals(FlagState.ALLOW, rule.resolve(ResourceRef.of("create", "wrench")));
    }

    @Test
    public void testDenySelectorBeatsAllowSelector() {
        ResourceSelector allowSel = ResourceSelector.parse("create:*");
        ResourceSelector denySel = ResourceSelector.parse("create:creative_motor");
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(allowSel));
        ResourceSelectorSet deny = ResourceSelectorSet.of(List.of(denySel));

        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, deny);

        // Allowed generally in create:*
        assertEquals(FlagState.ALLOW, rule.resolve(ResourceRef.of("create", "wrench")));
        // Denied specifically because deny beats allow
        assertEquals(FlagState.DENY, rule.resolve(ResourceRef.of("create", "creative_motor")));
        // Other mod denied by default state
        assertEquals(FlagState.DENY, rule.resolve(ResourceRef.of("minecraft", "stick")));
    }

    @Test
    public void testNullResourceReturnsDefaultState() {
        FlagRule rule = FlagRule.conditional(
            FlagState.ALLOW,
            ResourceSelectorSet.of(List.of(ResourceSelector.parse("*"))),
            ResourceSelectorSet.of(List.of(ResourceSelector.parse("minecraft:stone")))
        );
        assertEquals(FlagState.ALLOW, rule.resolve(null));
    }
}
