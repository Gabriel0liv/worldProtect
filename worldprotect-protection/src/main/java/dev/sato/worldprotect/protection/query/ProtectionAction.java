package dev.sato.worldprotect.protection.query;

/**
 * Represents logical protection actions that can be checked by worldProtect.
 * These are high-level abstractions, not direct platform-specific loader events.
 * Platform adapters must map physical game events (e.g. block breaking, entity interaction)
 * to these logical actions.
 */
public enum ProtectionAction {
    /**
     * General/fallback build action.
     */
    BUILD,

    /**
     * Breaking a block.
     */
    BLOCK_BREAK,

    /**
     * Placing a block.
     */
    BLOCK_PLACE,

    /**
     * Modifying block state or properties (e.g. using a wrench).
     */
    BLOCK_MODIFY,

    /**
     * Interacting with a block (e.g. pressing a button, flipping a lever).
     */
    BLOCK_INTERACT,

    /**
     * Generic item use.
     */
    ITEM_USE,

    /**
     * Using an item on a block.
     */
    ITEM_USE_ON_BLOCK,

    /**
     * Using an item on an entity.
     */
    ITEM_USE_ON_ENTITY,

    /**
     * Opening a container interface.
     */
    CONTAINER_OPEN,

    /**
     * Inserting an item into an inventory.
     */
    INVENTORY_INSERT,

    /**
     * Extracting an item from an inventory.
     */
    INVENTORY_EXTRACT,

    /**
     * Damaging an entity.
     */
    ENTITY_DAMAGE,

    /**
     * Spawning an entity in the world.
     */
    ENTITY_SPAWN,

    /**
     * Interacting with an entity.
     */
    ENTITY_INTERACT,

    /**
     * Block damage caused by an explosion.
     */
    EXPLOSION_BLOCK_DAMAGE,

    /**
     * Entity damage caused by an explosion.
     */
    EXPLOSION_ENTITY_DAMAGE,

    /**
     * Item drops caused by an explosion.
     */
    EXPLOSION_ITEM_DROP,

    /**
     * Item drops from block destruction.
     */
    BLOCK_DROP,

    /**
     * Item drops from entity death.
     */
    ENTITY_DROP,

    /**
     * Liquid flowing or spreading.
     */
    FLUID_SPREAD,

    /**
     * Block movement caused by pistons or contraptions.
     */
    PISTON_MOVE,

    /**
     * Generic world modifications not covered by other specific categories.
     */
    WORLD_MODIFY
}
