package dev.sato.worldprotect.protection.query;

/**
 * Represents the kind of target being acted upon.
 */
public enum ProtectionTargetKind {
    BLOCK,
    ITEM,
    ENTITY,
    BLOCK_ENTITY,
    CONTAINER,
    INVENTORY,
    DIMENSION,
    POSITION,
    EXPLOSION,
    DROP,
    FLUID,
    UNKNOWN
}
