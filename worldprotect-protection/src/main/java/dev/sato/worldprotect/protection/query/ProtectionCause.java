package dev.sato.worldprotect.protection.query;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable representation of a cause or trigger in the protection query.
 */
public final class ProtectionCause {
    private final ProtectionCauseType type;
    private final ResourceRef sourceId;
    private final String description;

    public ProtectionCause(ProtectionCauseType type, ResourceRef sourceId, String description) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.sourceId = sourceId;
        this.description = description;
    }

    public ProtectionCauseType type() {
        return type;
    }

    public Optional<ResourceRef> sourceId() {
        return Optional.ofNullable(sourceId);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public ProtectionCauseType getType() {
        return type;
    }

    public ResourceRef getSourceId() {
        return sourceId;
    }

    public String getDescription() {
        return description;
    }

    public static ProtectionCause player() {
        return new ProtectionCause(ProtectionCauseType.PLAYER, null, null);
    }

    public static ProtectionCause item(ResourceRef itemId) {
        return new ProtectionCause(ProtectionCauseType.ITEM, Objects.requireNonNull(itemId, "itemId must not be null"), null);
    }

    public static ProtectionCause explosion(ResourceRef sourceId) {
        return new ProtectionCause(ProtectionCauseType.EXPLOSION, sourceId, null);
    }

    public static ProtectionCause blockEntity(ResourceRef blockEntityId) {
        return new ProtectionCause(ProtectionCauseType.BLOCK_ENTITY, Objects.requireNonNull(blockEntityId, "blockEntityId must not be null"), null);
    }

    public static ProtectionCause unknown() {
        return new ProtectionCause(ProtectionCauseType.UNKNOWN, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtectionCause that = (ProtectionCause) o;
        return type == that.type &&
               Objects.equals(sourceId, that.sourceId) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sourceId, description);
    }

    @Override
    public String toString() {
        return "ProtectionCause{type=" + type + ", sourceId=" + sourceId + ", description='" + description + "'}";
    }
}
