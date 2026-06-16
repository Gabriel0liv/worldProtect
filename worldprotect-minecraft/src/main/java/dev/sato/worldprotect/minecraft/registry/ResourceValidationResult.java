package dev.sato.worldprotect.minecraft.registry;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.List;
import java.util.Objects;

/**
 * Immutable validation result for a ResourceRef.
 */
public final class ResourceValidationResult {
    private final ResourceRef id;
    private final ResourceKind kind;
    private final boolean valid;
    private final List<String> messages;

    public ResourceValidationResult(ResourceRef id, ResourceKind kind, boolean valid, List<String> messages) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.valid = valid;
        this.messages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
    }

    public ResourceRef id() {
        return id;
    }

    public ResourceKind kind() {
        return kind;
    }

    public boolean valid() {
        return valid;
    }

    public List<String> messages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceValidationResult that = (ResourceValidationResult) o;
        return valid == that.valid &&
                id.equals(that.id) &&
                kind == that.kind &&
                messages.equals(that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, kind, valid, messages);
    }

    @Override
    public String toString() {
        return "ResourceValidationResult{id=" + id + ", kind=" + kind + ", valid=" + valid + ", messages=" + messages + "}";
    }
}
