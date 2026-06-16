package dev.sato.worldprotect.protection.flag;

/**
 * Represents the state or configured value of a protection flag.
 */
public enum FlagState {
    /**
     * Explicitly allows the action.
     */
    ALLOW,

    /**
     * Explicitly denies the action.
     */
    DENY,

    /**
     * Passes the decision to lower priority regions or fallback checks.
     */
    PASS
}
