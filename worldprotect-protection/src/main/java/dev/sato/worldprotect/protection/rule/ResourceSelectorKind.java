package dev.sato.worldprotect.protection.rule;

/**
 * Defines the kinds of resource selectors supported by region flag rules.
 */
public enum ResourceSelectorKind {
    /**
     * Exact match for a resource ID (e.g. "minecraft:stone").
     */
    EXACT,

    /**
     * Wildcard matching any resource ID in a specific namespace (e.g. "create:*").
     */
    NAMESPACE_WILDCARD,

    /**
     * Wildcard matching any resource ID (represented as "*").
     */
    GLOBAL_WILDCARD,

    /**
     * Tag matching (e.g., "#forge:chests" or "#c:tools").
     */
    TAG
}
