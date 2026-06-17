package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for identifying build-related actions, their specific flags,
 * and determining whether an action falls back to the generic build flag.
 *
 * <p>Build-related actions are those that modify the world or act directly on blocks in a way
 * that should inherit build fallback semantics. Non-build actions (e.g., container open,
 * item use on entities) are never resolved through build fallback or implicit membership
 * protection.</p>
 */
public final class BuildSemantics {

    /**
     * Actions that are considered build-related: they may fall back to the generic build flag
     * and participate in implicit membership-based build protection.
     */
    private static final Set<ProtectionAction> BUILD_RELATED = EnumSet.of(
            ProtectionAction.BUILD,
            ProtectionAction.BLOCK_BREAK,
            ProtectionAction.BLOCK_PLACE,
            ProtectionAction.BLOCK_MODIFY,
            ProtectionAction.BLOCK_INTERACT,
            ProtectionAction.ITEM_USE_ON_BLOCK,
            ProtectionAction.PISTON_MOVE,
            ProtectionAction.FLUID_SPREAD,
            ProtectionAction.EXPLOSION_BLOCK_DAMAGE
    );

    private BuildSemantics() {}

    /**
     * Returns true if the given action is build-related and eligible for build fallback
     * and implicit membership protection.
     */
    public static boolean isBuildRelated(ProtectionAction action) {
        Objects.requireNonNull(action, "action must not be null");
        return BUILD_RELATED.contains(action);
    }

    /**
     * Returns the action-specific flag keys for a build-related action.
     * For example, BLOCK_BREAK -> [break-block], BLOCK_PLACE -> [place-block].
     * Returns an empty list for the generic BUILD action (no specific flag, only fallback).
     *
     * @throws IllegalArgumentException if the action is not build-related
     */
    public static List<FlagKey> specificFlagsFor(ProtectionAction action) {
        Objects.requireNonNull(action, "action must not be null");
        if (!isBuildRelated(action)) {
            throw new IllegalArgumentException("Action is not build-related: " + action);
        }
        switch (action) {
            case BLOCK_BREAK:
                return List.of(BuiltInFlags.BREAK_BLOCK_KEY);
            case BLOCK_PLACE:
                return List.of(BuiltInFlags.PLACE_BLOCK_KEY);
            case BLOCK_MODIFY:
                return List.of(BuiltInFlags.MODIFY_BLOCK_KEY);
            case BLOCK_INTERACT:
                return List.of(BuiltInFlags.INTERACT_BLOCK_KEY);
            case ITEM_USE_ON_BLOCK:
                return List.of(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, BuiltInFlags.USE_ITEM_KEY);
            case PISTON_MOVE:
                return List.of(BuiltInFlags.PISTON_MOVE_KEY);
            case FLUID_SPREAD:
                return List.of(BuiltInFlags.FLUID_SPREAD_KEY);
            case EXPLOSION_BLOCK_DAMAGE:
                return List.of(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY);
            case BUILD:
                return List.of();
            default:
                return List.of();
        }
    }

    /**
     * Returns true if the given build-related action uses the generic build flag as a fallback.
     * All build-related actions use build fallback.
     *
     * @throws IllegalArgumentException if the action is not build-related
     */
    public static boolean usesBuildFallback(ProtectionAction action) {
        Objects.requireNonNull(action, "action must not be null");
        if (!isBuildRelated(action)) {
            throw new IllegalArgumentException("Action is not build-related: " + action);
        }
        return true;
    }

    /**
     * Returns all specific build-related flag keys (excluding the generic build flag itself).
     */
    public static List<FlagKey> allSpecificFlags() {
        return List.of(
                BuiltInFlags.BREAK_BLOCK_KEY,
                BuiltInFlags.PLACE_BLOCK_KEY,
                BuiltInFlags.MODIFY_BLOCK_KEY,
                BuiltInFlags.INTERACT_BLOCK_KEY,
                BuiltInFlags.USE_ITEM_ON_BLOCK_KEY,
                BuiltInFlags.USE_ITEM_KEY,
                BuiltInFlags.PISTON_MOVE_KEY,
                BuiltInFlags.FLUID_SPREAD_KEY,
                BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY
        );
    }
}
