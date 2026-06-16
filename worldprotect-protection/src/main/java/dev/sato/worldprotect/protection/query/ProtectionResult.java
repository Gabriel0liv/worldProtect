package dev.sato.worldprotect.protection.query;

import dev.sato.worldprotect.core.result.Decision;
import dev.sato.worldprotect.core.result.DecisionReason;
import java.util.Objects;

/**
 * Result of evaluating a ProtectionQuery, containing the decision and the reason.
 */
public final class ProtectionResult {
    private final Decision decision;
    private final DecisionReason reason;

    public ProtectionResult(Decision decision, DecisionReason reason) {
        this.decision = Objects.requireNonNull(decision, "decision must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
    }

    public Decision getDecision() {
        return decision;
    }

    public DecisionReason getReason() {
        return reason;
    }

    public boolean isAllowed() {
        return decision == Decision.ALLOW;
    }

    public boolean isDenied() {
        return decision == Decision.DENY;
    }

    @Override
    public String toString() {
        return "ProtectionResult{decision=" + decision + ", reason=" + reason + "}";
    }
}
