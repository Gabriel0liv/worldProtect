package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import java.util.Objects;

/**
 * Immutable rule representing either a simple FlagState or a conditional rule containing allow/deny selectors.
 */
public final class FlagRule {
    private final FlagState defaultState;
    private final ResourceSelectorSet allowSelectors;
    private final ResourceSelectorSet denySelectors;
    private final RegionGroup group;

    public FlagRule(FlagState defaultState, ResourceSelectorSet allowSelectors, ResourceSelectorSet denySelectors) {
        this(defaultState, allowSelectors, denySelectors, RegionGroup.ALL);
    }

    public FlagRule(FlagState defaultState, ResourceSelectorSet allowSelectors, ResourceSelectorSet denySelectors, RegionGroup group) {
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState must not be null");
        this.allowSelectors = Objects.requireNonNull(allowSelectors, "allowSelectors must not be null");
        this.denySelectors = Objects.requireNonNull(denySelectors, "denySelectors must not be null");
        this.group = Objects.requireNonNull(group, "group must not be null");
    }

    public static FlagRule simple(FlagState state) {
        Objects.requireNonNull(state, "state must not be null");
        return new FlagRule(state, ResourceSelectorSet.empty(), ResourceSelectorSet.empty(), RegionGroup.ALL);
    }

    public static FlagRule simple(FlagState state, RegionGroup group) {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(group, "group must not be null");
        return new FlagRule(state, ResourceSelectorSet.empty(), ResourceSelectorSet.empty(), group);
    }

    public static FlagRule conditional(FlagState defaultState, ResourceSelectorSet allow, ResourceSelectorSet deny) {
        return new FlagRule(defaultState, allow, deny, RegionGroup.ALL);
    }

    public static FlagRule conditional(FlagState defaultState, ResourceSelectorSet allow, ResourceSelectorSet deny, RegionGroup group) {
        return new FlagRule(defaultState, allow, deny, group);
    }

    public static FlagRule pass() {
        return new FlagRule(FlagState.PASS, ResourceSelectorSet.empty(), ResourceSelectorSet.empty(), RegionGroup.ALL);
    }

    public FlagState defaultState() {
        return defaultState;
    }

    public ResourceSelectorSet allowSelectors() {
        return allowSelectors;
    }

    public ResourceSelectorSet denySelectors() {
        return denySelectors;
    }

    public FlagState getDefaultState() {
        return defaultState;
    }

    public ResourceSelectorSet getAllowSelectors() {
        return allowSelectors;
    }

    public ResourceSelectorSet getDenySelectors() {
        return denySelectors;
    }

    public boolean isSimple() {
        return allowSelectors.isEmpty() && denySelectors.isEmpty();
    }

    public RegionGroup group() {
        return group;
    }

    public RegionGroup getGroup() {
        return group;
    }

    /**
     * Evaluates the rule against a resource reference, returning detail on match source and matched selector.
     */
    public FlagRuleEvaluation evaluate(ResourceRef resource) {
        if (resource == null) {
            return new FlagRuleEvaluation(defaultState, FlagRuleMatchSource.DEFAULT, null);
        }
        for (ResourceSelector selector : denySelectors.selectors()) {
            if (selector.matches(resource)) {
                return new FlagRuleEvaluation(FlagState.DENY, FlagRuleMatchSource.DENY_SELECTOR, selector);
            }
        }
        for (ResourceSelector selector : allowSelectors.selectors()) {
            if (selector.matches(resource)) {
                return new FlagRuleEvaluation(FlagState.ALLOW, FlagRuleMatchSource.ALLOW_SELECTOR, selector);
            }
        }
        return new FlagRuleEvaluation(defaultState, FlagRuleMatchSource.DEFAULT, null);
    }

    /**
     * Resolves the configured FlagState against a specific resource ID.
     */
    public FlagState resolve(ResourceRef resource) {
        return evaluate(resource).state();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagRule flagRule = (FlagRule) o;
        return defaultState == flagRule.defaultState &&
               allowSelectors.equals(flagRule.allowSelectors) &&
               denySelectors.equals(flagRule.denySelectors) &&
               group == flagRule.group;
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultState, allowSelectors, denySelectors, group);
    }

    @Override
    public String toString() {
        if (isSimple()) {
            return "FlagRule{simple=" + defaultState + ", group=" + group + "}";
        }
        return "FlagRule{default=" + defaultState + ", allow=" + allowSelectors + ", deny=" + denySelectors + ", group=" + group + "}";
    }
}
