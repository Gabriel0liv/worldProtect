package dev.sato.worldprotect.protection.query;

/**
 * Represents the type of trigger or initiator for a protection query.
 */
public enum ProtectionCauseType {
    PLAYER,
    COMMAND,
    ITEM,
    BLOCK,
    BLOCK_ENTITY,
    ENTITY,
    PROJECTILE,
    EXPLOSION,
    FLUID,
    PISTON,
    MODDED_MACHINE,
    WORLDGEN,
    UNKNOWN
}
