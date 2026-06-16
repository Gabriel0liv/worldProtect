package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.FlagState;
import java.util.Objects;

/**
 * Immutable rule representing either a simple FlagState or a conditional rule containing allow/deny selectors.
 */
public final class FlagRule {
    private final FlagState defaultState;
    private final ResourceSelectorSet allowSelectors;
    private final ResourceSelectorSet denySelectors;

    public FlagRule(FlagState defaultState, ResourceSelectorSet allowSelectors, ResourceSelectorSet denySelectors) {
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState must not be null");
        this.allowSelectors = Objects.requireNonNull(allowSelectors, "allowSelectors must not be null");
        this.denySelectors = Objects.requireNonNull(denySelectors, "denySelectors must not be null");
    }

    public static FlagRule simple(FlagState state) {
        Objects.requireNonNull(state, "state must not be null");
        return new FlagRule(state, ResourceSelectorSet.empty(), ResourceSelectorSet.empty());
    }

    public static FlagRule conditional(FlagState defaultState, ResourceSelectorSet allow, ResourceSelectorSet deny) {
        return new FlagRule(defaultState, allow, deny);
    }

    public static FlagRule pass() {
        return new FlagRule(FlagState.PASS, ResourceSelectorSet.empty(), ResourceSelectorSet.empty());
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

    /**
     * Resolves the configured FlagState against a specific resource ID.
     */
    public FlagState resolve(ResourceRef resource) {
        if (resource == null) {
            return defaultState;
        }
        if (denySelectors.matches(resource)) {
            return FlagState.DENY;
        }
        if (allowSelectors.matches(resource)) {
            return FlagState.ALLOW;
        }
        return defaultState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagRule flagRule = (FlagRule) o;
        return defaultState == flagRule.defaultState &&
               allowSelectors.equals(flagRule.allowSelectors) &&
               denySelectors.equals(flagRule.denySelectors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultState, allowSelectors, denySelectors);
    }

    @Override
    public String toString() {
        if (isSimple()) {
            return "FlagRule{simple=" + defaultState + "}";
        }
        return "FlagRule{default=" + defaultState + ", allow=" + allowSelectors + ", deny=" + denySelectors + "}";
    }
}
