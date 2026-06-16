package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.protection.flag.FlagState;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable result of evaluating a FlagRule against a ResourceRef.
 * Contains the decided FlagState, the match source, and any matched ResourceSelector.
 */
public final class FlagRuleEvaluation {
    private final FlagState state;
    private final FlagRuleMatchSource source;
    private final ResourceSelector matchedSelector;

    public FlagRuleEvaluation(FlagState state, FlagRuleMatchSource source, ResourceSelector matchedSelector) {
        this.state = Objects.requireNonNull(state, "state must not be null");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.matchedSelector = matchedSelector;
    }

    public FlagState state() {
        return state;
    }

    public FlagRuleMatchSource source() {
        return source;
    }

    public Optional<ResourceSelector> matchedSelector() {
        return Optional.ofNullable(matchedSelector);
    }

    public FlagState getState() {
        return state;
    }

    public FlagRuleMatchSource getSource() {
        return source;
    }

    public ResourceSelector getMatchedSelector() {
        return matchedSelector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlagRuleEvaluation that = (FlagRuleEvaluation) o;
        return state == that.state &&
               source == that.source &&
               Objects.equals(matchedSelector, that.matchedSelector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, source, matchedSelector);
    }

    @Override
    public String toString() {
        return "FlagRuleEvaluation{state=" + state + ", source=" + source + ", matchedSelector=" + matchedSelector + "}";
    }
}
