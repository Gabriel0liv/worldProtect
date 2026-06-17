package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.query.ProtectionCauseType;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTargetKind;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class to extract the primary resource of interest from a ProtectionQuery.
 */
public final class QueryResourceExtractor {

    public static Optional<ResourceRef> primaryResource(ProtectionQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        switch (query.getAction()) {
            case BLOCK_BREAK:
            case BLOCK_PLACE:
            case EXPLOSION_BLOCK_DAMAGE:
            case PISTON_MOVE:
                if (query.getTarget().kind() == ProtectionTargetKind.BLOCK) {
                    return query.getTarget().id();
                }
                break;

            case BLOCK_MODIFY:
                if (query.getTarget().kind() == ProtectionTargetKind.BLOCK ||
                    query.getTarget().kind() == ProtectionTargetKind.BLOCK_ENTITY) {
                    return query.getTarget().id();
                }
                break;

            case BLOCK_INTERACT:
                if (query.getTarget().kind() == ProtectionTargetKind.BLOCK ||
                    query.getTarget().kind() == ProtectionTargetKind.CONTAINER) {
                    return query.getTarget().id();
                }
                break;

            case ITEM_USE:
                if (query.getCauseChain().direct().type() == ProtectionCauseType.ITEM) {
                    return query.getCauseChain().direct().sourceId();
                }
                if (query.getTarget().kind() == ProtectionTargetKind.ITEM) {
                    return query.getTarget().id();
                }
                break;

            case ITEM_USE_ON_BLOCK:
                if (query.getCauseChain().direct().type() == ProtectionCauseType.ITEM) {
                    Optional<ResourceRef> item = query.getCauseChain().direct().sourceId();
                    if (item.isPresent()) {
                        return item;
                    }
                }
                if (query.getTarget().kind() == ProtectionTargetKind.BLOCK) {
                    return query.getTarget().id();
                }
                break;

            case ITEM_USE_ON_ENTITY:
                if (query.getCauseChain().direct().type() == ProtectionCauseType.ITEM) {
                    Optional<ResourceRef> item = query.getCauseChain().direct().sourceId();
                    if (item.isPresent()) {
                        return item;
                    }
                }
                if (query.getTarget().kind() == ProtectionTargetKind.ENTITY) {
                    return query.getTarget().id();
                }
                break;

            case CONTAINER_OPEN:
                if (query.getTarget().kind() == ProtectionTargetKind.CONTAINER ||
                    query.getTarget().kind() == ProtectionTargetKind.BLOCK) {
                    return query.getTarget().id();
                }
                break;

            case INVENTORY_INSERT:
            case INVENTORY_EXTRACT:
                if (query.getTarget().kind() == ProtectionTargetKind.CONTAINER ||
                    query.getTarget().kind() == ProtectionTargetKind.INVENTORY) {
                    return query.getTarget().id();
                }
                break;

            case ENTITY_DAMAGE:
            case ENTITY_SPAWN:
            case ENTITY_INTERACT:
            case EXPLOSION_ENTITY_DAMAGE:
                if (query.getTarget().kind() == ProtectionTargetKind.ENTITY) {
                    return query.getTarget().id();
                }
                break;

            case EXPLOSION_ITEM_DROP:
            case BLOCK_DROP:
            case ENTITY_DROP:
                if (query.getTarget().kind() == ProtectionTargetKind.DROP ||
                    query.getTarget().kind() == ProtectionTargetKind.ITEM) {
                    return query.getTarget().id();
                }
                break;

            case FLUID_SPREAD:
                if (query.getTarget().kind() == ProtectionTargetKind.FLUID) {
                    return query.getTarget().id();
                }
                break;

            case BUILD:
                return Optional.empty();

            case WORLD_MODIFY:
                if (query.getTarget().id().isPresent()) {
                    return query.getTarget().id();
                }
                if (query.getCauseChain().direct().sourceId().isPresent()) {
                    return query.getCauseChain().direct().sourceId();
                }
                break;
        }

        return Optional.empty();
    }

    private QueryResourceExtractor() {}
}
