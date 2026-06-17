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

    @Test
    public void testEvaluateOutputs() {
        ResourceSelector allowSel = ResourceSelector.parse("create:*");
        ResourceSelector denySel = ResourceSelector.parse("create:creative_motor");
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(allowSel));
        ResourceSelectorSet deny = ResourceSelectorSet.of(List.of(denySel));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, deny);

        // evaluate() returns DENY_SELECTOR when deny selector matches
        FlagRuleEvaluation evalDeny = rule.evaluate(ResourceRef.of("create", "creative_motor"));
        assertEquals(FlagState.DENY, evalDeny.state());
        assertEquals(FlagRuleMatchSource.DENY_SELECTOR, evalDeny.source());
        assertTrue(evalDeny.matchedSelector().isPresent());
        assertEquals(denySel, evalDeny.matchedSelector().get());

        // evaluate() returns ALLOW_SELECTOR when allow selector matches
        FlagRuleEvaluation evalAllow = rule.evaluate(ResourceRef.of("create", "wrench"));
        assertEquals(FlagState.ALLOW, evalAllow.state());
        assertEquals(FlagRuleMatchSource.ALLOW_SELECTOR, evalAllow.source());
        assertTrue(evalAllow.matchedSelector().isPresent());
        assertEquals(allowSel, evalAllow.matchedSelector().get());

        // evaluate() returns DEFAULT when no selector matches
        FlagRuleEvaluation evalDefault = rule.evaluate(ResourceRef.of("minecraft", "stone"));
        assertEquals(FlagState.DENY, evalDefault.state());
        assertEquals(FlagRuleMatchSource.DEFAULT, evalDefault.source());
        assertFalse(evalDefault.matchedSelector().isPresent());

        // evaluate() returns DEFAULT when resource is null
        FlagRuleEvaluation evalNull = rule.evaluate(null);
        assertEquals(FlagState.DENY, evalNull.state());
        assertEquals(FlagRuleMatchSource.DEFAULT, evalNull.source());
        assertFalse(evalNull.matchedSelector().isPresent());
    }

    @Test
    public void testRegionGroupSupport() {
        // Defaults to ALL
        FlagRule r1 = FlagRule.simple(FlagState.ALLOW);
        assertEquals(dev.sato.worldprotect.protection.subject.RegionGroup.ALL, r1.group());

        // Custom simple
        FlagRule r2 = FlagRule.simple(FlagState.DENY, dev.sato.worldprotect.protection.subject.RegionGroup.OWNERS);
        assertEquals(dev.sato.worldprotect.protection.subject.RegionGroup.OWNERS, r2.group());

        // Custom conditional
        FlagRule r3 = FlagRule.conditional(
                FlagState.DENY,
                ResourceSelectorSet.empty(),
                ResourceSelectorSet.empty(),
                dev.sato.worldprotect.protection.subject.RegionGroup.MEMBERS
        );
        assertEquals(dev.sato.worldprotect.protection.subject.RegionGroup.MEMBERS, r3.group());

        // Equals, hashCode, toString
        FlagRule r3Copy = FlagRule.conditional(
                FlagState.DENY,
                ResourceSelectorSet.empty(),
                ResourceSelectorSet.empty(),
                dev.sato.worldprotect.protection.subject.RegionGroup.MEMBERS
        );
        assertEquals(r3, r3Copy);
        assertEquals(r3.hashCode(), r3Copy.hashCode());
        assertTrue(r3.toString().contains("group=MEMBERS"));
    }
}
