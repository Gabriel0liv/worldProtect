package dev.sato.worldprotect.audit;

/**
 * Type of action captured by the audit system.
 */
public enum AuditAction {
    BLOCK_BREAK,
    BLOCK_PLACE,
    CONTAINER_TAKE,
    CONTAINER_PUT,
    ENTITY_KILL,
    ENTITY_SPAWN
}
