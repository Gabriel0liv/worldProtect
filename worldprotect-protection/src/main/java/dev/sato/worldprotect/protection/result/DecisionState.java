package dev.sato.worldprotect.protection.result;

/**
 * Represents the final decision of a protection check.
 */
public enum DecisionState {
    /**
     * The operation is explicitly allowed.
     */
    ALLOW,

    /**
     * The operation is explicitly denied.
     */
    DENY,

    /**
     * No applicable rule was found; caller may fall back to default behavior.
     */
    PASS
}
