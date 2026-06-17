package dev.sato.worldprotect.protection.flag;

import dev.sato.worldprotect.protection.query.ProtectionAction;
import java.util.List;
import java.util.Objects;

/**
 * Maps ProtectionAction to one or more relevant FlagKey values, ordered from most specific to most generic.
 *
 * <p>For build-related actions (BLOCK_BREAK, BLOCK_PLACE, BLOCK_MODIFY), only the action-specific
 * flag is returned. The build fallback chain (build flag, passthrough, implicit membership) is
 * handled separately by {@code BuildDecisionResolver}.</p>
 */
public final class ActionFlagMapper {

    public static List<FlagKey> mapAction(ProtectionAction action) {
        Objects.requireNonNull(action, "action must not be null");
        switch (action) {
            case BUILD:
                return List.of(BuiltInFlags.BUILD_KEY);
            case BLOCK_BREAK:
                return List.of(BuiltInFlags.BREAK_BLOCK_KEY);
            case BLOCK_PLACE:
                return List.of(BuiltInFlags.PLACE_BLOCK_KEY);
            case BLOCK_MODIFY:
                return List.of(BuiltInFlags.MODIFY_BLOCK_KEY);
            case BLOCK_INTERACT:
                return List.of(BuiltInFlags.INTERACT_BLOCK_KEY);
            case ITEM_USE:
                return List.of(BuiltInFlags.USE_ITEM_KEY);
            case ITEM_USE_ON_BLOCK:
                return List.of(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, BuiltInFlags.USE_ITEM_KEY);
            case ITEM_USE_ON_ENTITY:
                return List.of(BuiltInFlags.USE_ITEM_ON_ENTITY_KEY, BuiltInFlags.USE_ITEM_KEY);
            case CONTAINER_OPEN:
                return List.of(BuiltInFlags.OPEN_CONTAINER_KEY);
            case INVENTORY_INSERT:
                return List.of(BuiltInFlags.INVENTORY_INSERT_KEY);
            case INVENTORY_EXTRACT:
                return List.of(BuiltInFlags.INVENTORY_EXTRACT_KEY);
            case ENTITY_DAMAGE:
                return List.of(BuiltInFlags.DAMAGE_ENTITY_KEY);
            case ENTITY_SPAWN:
                return List.of(BuiltInFlags.SPAWN_ENTITY_KEY);
            case EXPLOSION_BLOCK_DAMAGE:
                return List.of(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY);
            case EXPLOSION_ENTITY_DAMAGE:
                return List.of(BuiltInFlags.EXPLOSION_DAMAGE_ENTITIES_KEY);
            case EXPLOSION_ITEM_DROP:
                return List.of(BuiltInFlags.EXPLOSION_DROP_ITEMS_KEY);
            case BLOCK_DROP:
                return List.of(BuiltInFlags.BLOCK_DROPS_KEY);
            case ENTITY_DROP:
                return List.of(BuiltInFlags.ENTITY_DROPS_KEY);
            case FLUID_SPREAD:
                return List.of(BuiltInFlags.FLUID_SPREAD_KEY);
            case PISTON_MOVE:
                return List.of(BuiltInFlags.PISTON_MOVE_KEY);
            case WORLD_MODIFY:
                return List.of(BuiltInFlags.WORLD_MODIFY_KEY);
            default:
                return List.of();
        }
    }

    private ActionFlagMapper() {}
}
