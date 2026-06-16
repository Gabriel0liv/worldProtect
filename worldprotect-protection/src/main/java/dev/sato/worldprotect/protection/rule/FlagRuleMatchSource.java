package dev.sato.worldprotect.protection.rule;

/**
 * Indicates the source or component of a FlagRule that determined the outcome.
 */
public enum FlagRuleMatchSource {
    /**
     * The decision was resolved from the default fallback state of the rule.
     */
    DEFAULT,

    /**
     * The decision was determined by a matching allow selector.
     */
    ALLOW_SELECTOR,

    /**
     * The decision was determined by a matching deny selector.
     */
    DENY_SELECTOR
}
