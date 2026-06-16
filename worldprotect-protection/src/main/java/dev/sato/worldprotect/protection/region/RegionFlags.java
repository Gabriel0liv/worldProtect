package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.rule.FlagRule;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable container representing configured flag rules on a region.
 */
public final class RegionFlags {
    private static final RegionFlags EMPTY = new RegionFlags(Map.of());

    private final Map<FlagKey, FlagRule> rules;

    private RegionFlags(Map<FlagKey, FlagRule> rules) {
        this.rules = Map.copyOf(rules);
    }

    public static RegionFlags empty() {
        return EMPTY;
    }

    public static RegionFlags of(Map<FlagKey, FlagState> states) {
        Objects.requireNonNull(states, "states map must not be null");
        Map<FlagKey, FlagRule> converted = new HashMap<>();
        states.forEach((key, state) -> {
            Objects.requireNonNull(key, "flag key must not be null");
            Objects.requireNonNull(state, "flag state must not be null");
            converted.put(key, FlagRule.simple(state));
        });
        return new RegionFlags(converted);
    }

    public static RegionFlags ofRules(Map<FlagKey, FlagRule> rules) {
        Objects.requireNonNull(rules, "rules map must not be null");
        rules.forEach((key, rule) -> {
            Objects.requireNonNull(key, "flag key must not be null");
            Objects.requireNonNull(rule, "flag rule must not be null");
        });
        return new RegionFlags(rules);
    }

    public Optional<FlagRule> rule(FlagKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(rules.get(key));
    }

    public Optional<FlagState> get(FlagKey key) {
        return rule(key).map(FlagRule::defaultState);
    }

    public boolean contains(FlagKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return rules.containsKey(key);
    }

    public Map<FlagKey, FlagRule> asRuleMap() {
        return rules;
    }

    public Map<FlagKey, FlagState> asMap() {
        Map<FlagKey, FlagState> simpleMap = new HashMap<>();
        rules.forEach((key, rule) -> simpleMap.put(key, rule.defaultState()));
        return Collections.unmodifiableMap(simpleMap);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionFlags that = (RegionFlags) o;
        return rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }

    @Override
    public String toString() {
        return "RegionFlags{rules=" + rules + "}";
    }
}
