package dev.sato.worldprotect.minecraft.registry;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service to validate a ResourceRef against a ResourceRegistryView.
 */
public final class ResourceValidator {
    private final ResourceRegistryView registryView;

    public ResourceValidator(ResourceRegistryView registryView) {
        this.registryView = Objects.requireNonNull(registryView, "registryView must not be null");
    }

    /**
     * Validates semantic existence of a resource identifier.
     * Rejects null arguments, but returns an invalid result if resource or namespace is missing.
     */
    public ResourceValidationResult validate(ResourceKind kind, ResourceRef id) {
        Objects.requireNonNull(kind, "kind must not be null");
        Objects.requireNonNull(id, "id must not be null");

        List<String> messages = new ArrayList<>();
        boolean valid = true;

        String namespace = id.namespace();
        if (!registryView.namespaceLoaded(namespace)) {
            valid = false;
            messages.add("Namespace '" + namespace + "' is not loaded in the current modpack/server runtime.");
        } else if (kind != ResourceKind.UNKNOWN) {
            if (!registryView.exists(kind, id)) {
                valid = false;
                messages.add("Resource ID '" + id.asString() + "' does not exist in registry kind: " + kind);
            }
        }

        return new ResourceValidationResult(id, kind, valid, messages);
    }
}
