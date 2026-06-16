package dev.sato.worldprotect.core.result;

/**
 * Result of a protection query evaluation.
 */
public enum Decision {
    /**
     * The action is explicitly allowed.
     */
    ALLOW,
    
    /**
     * The action is explicitly denied.
     */
    DENY,
    
    /**
     * The evaluation is neutral (neither allowing nor denying).
     */
    ABSTAIN
}
