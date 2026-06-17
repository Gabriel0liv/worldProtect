package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.rule.ResourceSelector;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration representation of a simple or conditional flag rule.
 */
public final class FlagRuleConfig {
    private final FlagState defaultState;
    private final List<String> allowSelectors;
    private final List<String> denySelectors;
    private final boolean simple;
    private final RegionGroup group;

    private FlagRuleConfig(FlagState defaultState, List<String> allowSelectors, List<String> denySelectors, boolean simple, RegionGroup group) {
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState must not be null");
        Objects.requireNonNull(allowSelectors, "allowSelectors must not be null");
        Objects.requireNonNull(denySelectors, "denySelectors must not be null");
        this.group = Objects.requireNonNull(group, "group must not be null");
        
        for (String s : allowSelectors) {
            Objects.requireNonNull(s, "allow selector must not be null");
            if (s.trim().isEmpty()) {
                throw new IllegalArgumentException("allow selector must not be blank");
            }
        }
        for (String s : denySelectors) {
            Objects.requireNonNull(s, "deny selector must not be null");
            if (s.trim().isEmpty()) {
                throw new IllegalArgumentException("deny selector must not be blank");
            }
        }

        this.allowSelectors = List.copyOf(allowSelectors);
        this.denySelectors = List.copyOf(denySelectors);
        this.simple = simple;
    }

    public static FlagRuleConfig simple(FlagState state) {
        Objects.requireNonNull(state, "state must not be null");
        return new FlagRuleConfig(state, List.of(), List.of(), true, RegionGroup.ALL);
    }

    public static FlagRuleConfig simple(FlagState state, RegionGroup group) {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(group, "group must not be null");
        return new FlagRuleConfig(state, List.of(), List.of(), true, group);
    }

    public static FlagRuleConfig conditional(FlagState defaultState, List<String> allowSelectors, List<String> denySelectors) {
        return new FlagRuleConfig(defaultState, allowSelectors, denySelectors, false, RegionGroup.ALL);
    }

    public static FlagRuleConfig conditional(FlagState defaultState, java.util.Collection<String> allowSelectors, java.util.Collection<String> denySelectors, RegionGroup group) {
        Objects.requireNonNull(allowSelectors, "allowSelectors must not be null");
        Objects.requireNonNull(denySelectors, "denySelectors must not be null");
        Objects.requireNonNull(group, "group must not be null");
        return new FlagRuleConfig(defaultState, List.copyOf(allowSelectors), List.copyOf(denySelectors), false, group);
    }

    public FlagState defaultState() {
        return defaultState;
    }

    public List<String> allowSelectors() {
        return allowSelectors;
    }

    public List<String> denySelectors() {
        return denySelectors;
    }

    public boolean isSimple() {
        return simple;
    }

    public RegionGroup group() {
        return group;
    }

    public RegionGroup getGroup() {
        return group;
    }

    public FlagState getDefaultState() {
        return defaultState;
    }

    public List<String> getAllowSelectors() {
        return allowSelectors;
    }

    public List<String> getDenySelectors() {
        return denySelectors;
    }

    public ConfigValidationResult validate(String path) {
        Objects.requireNonNull(path, "path must not be null");
        List<ConfigValidationMessage> msgs = new ArrayList<>();
        
        for (int i = 0; i < allowSelectors.size(); i++) {
            String selector = allowSelectors.get(i);
            try {
                ResourceSelector.parse(selector);
            } catch (Exception e) {
                msgs.add(ConfigValidationMessage.error(path + ".allow[" + i + "]", "Invalid allow selector '" + selector + "': " + e.getMessage()));
            }
        }

        for (int i = 0; i < denySelectors.size(); i++) {
            String selector = denySelectors.get(i);
            try {
                ResourceSelector.parse(selector);
            } catch (Exception e) {
                msgs.add(ConfigValidationMessage.error(path + ".deny[" + i + "]", "Invalid deny selector '" + selector + "': " + e.getMessage()));
            }
        }

        return ConfigValidationResult.of(msgs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagRuleConfig that = (FlagRuleConfig) o;
        return simple == that.simple &&
               defaultState == that.defaultState &&
               allowSelectors.equals(that.allowSelectors) &&
               denySelectors.equals(that.denySelectors) &&
               group == that.group;
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultState, allowSelectors, denySelectors, simple, group);
    }

    @Override
    public String toString() {
        if (simple) {
            return "FlagRuleConfig{simple=" + defaultState + ", group=" + group + "}";
        }
        return "FlagRuleConfig{default=" + defaultState + ", allow=" + allowSelectors + ", deny=" + denySelectors + ", group=" + group + "}";
    }
}
