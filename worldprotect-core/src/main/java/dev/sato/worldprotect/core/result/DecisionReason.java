package dev.sato.worldprotect.core.result;

import java.util.Objects;

/**
 * Contextual explanation for why a protection decision was made.
 */
public final class DecisionReason {
    public static final DecisionReason DEFAULT = new DecisionReason("DEFAULT", "No specific rule applied.");

    private final String code;
    private final String description;

    public DecisionReason(String code, String description) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DecisionReason{code='" + code + "', description='" + description + "'}";
    }
}
