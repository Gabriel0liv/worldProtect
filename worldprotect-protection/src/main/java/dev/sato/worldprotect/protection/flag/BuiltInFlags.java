package dev.sato.worldprotect.protection.flag;

import java.util.List;

/**
 * Registry of all built-in protection flags.
 */
public final class BuiltInFlags {
    public static final FlagKey BUILD_KEY = FlagKey.of("build");
    public static final FlagKey BREAK_BLOCK_KEY = FlagKey.of("break-block");
    public static final FlagKey PLACE_BLOCK_KEY = FlagKey.of("place-block");
    public static final FlagKey MODIFY_BLOCK_KEY = FlagKey.of("modify-block");
    public static final FlagKey INTERACT_BLOCK_KEY = FlagKey.of("interact-block");
    public static final FlagKey USE_ITEM_KEY = FlagKey.of("use-item");
    public static final FlagKey USE_ITEM_ON_BLOCK_KEY = FlagKey.of("use-item-on-block");
    public static final FlagKey USE_ITEM_ON_ENTITY_KEY = FlagKey.of("use-item-on-entity");
    public static final FlagKey OPEN_CONTAINER_KEY = FlagKey.of("open-container");
    public static final FlagKey INVENTORY_INSERT_KEY = FlagKey.of("inventory-insert");
    public static final FlagKey INVENTORY_EXTRACT_KEY = FlagKey.of("inventory-extract");
    public static final FlagKey DAMAGE_ENTITY_KEY = FlagKey.of("damage-entity");
    public static final FlagKey SPAWN_ENTITY_KEY = FlagKey.of("spawn-entity");
    public static final FlagKey EXPLOSION_BREAK_BLOCKS_KEY = FlagKey.of("explosion-break-blocks");
    public static final FlagKey EXPLOSION_DAMAGE_ENTITIES_KEY = FlagKey.of("explosion-damage-entities");
    public static final FlagKey EXPLOSION_DROP_ITEMS_KEY = FlagKey.of("explosion-drop-items");
    public static final FlagKey BLOCK_DROPS_KEY = FlagKey.of("block-drops");
    public static final FlagKey ENTITY_DROPS_KEY = FlagKey.of("entity-drops");
    public static final FlagKey FLUID_SPREAD_KEY = FlagKey.of("fluid-spread");
    public static final FlagKey PISTON_MOVE_KEY = FlagKey.of("piston-move");
    public static final FlagKey WORLD_MODIFY_KEY = FlagKey.of("world-modify");

    public static final FlagDefinition BUILD = FlagDefinition.of(BUILD_KEY, "Fallback flag for building or modifying the world", FlagState.PASS);
    public static final FlagDefinition BREAK_BLOCK = FlagDefinition.of(BREAK_BLOCK_KEY, "Permission to break blocks", FlagState.PASS);
    public static final FlagDefinition PLACE_BLOCK = FlagDefinition.of(PLACE_BLOCK_KEY, "Permission to place blocks", FlagState.PASS);
    public static final FlagDefinition MODIFY_BLOCK = FlagDefinition.of(MODIFY_BLOCK_KEY, "Permission to modify blocks (wrenches, etc.)", FlagState.PASS);
    public static final FlagDefinition INTERACT_BLOCK = FlagDefinition.of(INTERACT_BLOCK_KEY, "Permission to interact with blocks (levers, buttons, etc.)", FlagState.PASS);
    public static final FlagDefinition USE_ITEM = FlagDefinition.of(USE_ITEM_KEY, "Permission to use items", FlagState.PASS);
    public static final FlagDefinition USE_ITEM_ON_BLOCK = FlagDefinition.of(USE_ITEM_ON_BLOCK_KEY, "Permission to use items on blocks", FlagState.PASS);
    public static final FlagDefinition USE_ITEM_ON_ENTITY = FlagDefinition.of(USE_ITEM_ON_ENTITY_KEY, "Permission to use items on entities", FlagState.PASS);
    public static final FlagDefinition OPEN_CONTAINER = FlagDefinition.of(OPEN_CONTAINER_KEY, "Permission to open containers (chests, sophisticated storage, etc.)", FlagState.PASS);
    public static final FlagDefinition INVENTORY_INSERT = FlagDefinition.of(INVENTORY_INSERT_KEY, "Permission to insert items into inventories", FlagState.PASS);
    public static final FlagDefinition INVENTORY_EXTRACT = FlagDefinition.of(INVENTORY_EXTRACT_KEY, "Permission to extract items from inventories", FlagState.PASS);
    public static final FlagDefinition DAMAGE_ENTITY = FlagDefinition.of(DAMAGE_ENTITY_KEY, "Permission to damage entities (players, mobs, etc.)", FlagState.PASS);
    public static final FlagDefinition SPAWN_ENTITY = FlagDefinition.of(SPAWN_ENTITY_KEY, "Permission to spawn entities", FlagState.PASS);
    public static final FlagDefinition EXPLOSION_BREAK_BLOCKS = FlagDefinition.of(EXPLOSION_BREAK_BLOCKS_KEY, "Whether explosions can break blocks", FlagState.PASS);
    public static final FlagDefinition EXPLOSION_DAMAGE_ENTITIES = FlagDefinition.of(EXPLOSION_DAMAGE_ENTITIES_KEY, "Whether explosions can damage entities", FlagState.PASS);
    public static final FlagDefinition EXPLOSION_DROP_ITEMS = FlagDefinition.of(EXPLOSION_DROP_ITEMS_KEY, "Whether explosions can drop items", FlagState.PASS);
    public static final FlagDefinition BLOCK_DROPS = FlagDefinition.of(BLOCK_DROPS_KEY, "Whether broken blocks spawn item drops", FlagState.PASS);
    public static final FlagDefinition ENTITY_DROPS = FlagDefinition.of(ENTITY_DROPS_KEY, "Whether killed entities spawn drops", FlagState.PASS);
    public static final FlagDefinition FLUID_SPREAD = FlagDefinition.of(FLUID_SPREAD_KEY, "Whether fluids are allowed to flow/spread", FlagState.PASS);
    public static final FlagDefinition PISTON_MOVE = FlagDefinition.of(PISTON_MOVE_KEY, "Whether pistons/contraptions are allowed to move blocks", FlagState.PASS);
    public static final FlagDefinition WORLD_MODIFY = FlagDefinition.of(WORLD_MODIFY_KEY, "Permission to modify the world generally via machines", FlagState.PASS);

    public static final List<FlagDefinition> ALL = List.of(
        BUILD, BREAK_BLOCK, PLACE_BLOCK, MODIFY_BLOCK, INTERACT_BLOCK,
        USE_ITEM, USE_ITEM_ON_BLOCK, USE_ITEM_ON_ENTITY, OPEN_CONTAINER,
        INVENTORY_INSERT, INVENTORY_EXTRACT, DAMAGE_ENTITY, SPAWN_ENTITY,
        EXPLOSION_BREAK_BLOCKS, EXPLOSION_DAMAGE_ENTITIES, EXPLOSION_DROP_ITEMS,
        BLOCK_DROPS, ENTITY_DROPS, FLUID_SPREAD, PISTON_MOVE, WORLD_MODIFY
    );

    private BuiltInFlags() {}
}
