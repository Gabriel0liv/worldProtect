package dev.sato.worldprotect.minecraft.registry;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Set;

/**
 * Read-only abstraction over the currently loaded modpack/server registries.
 */
public interface ResourceRegistryView {

    /**
     * Checks if the namespace is loaded in the current modpack/server runtime.
     */
    boolean namespaceLoaded(String namespace);

    /**
     * Checks if a resource of the specified kind exists in the registry.
     */
    boolean exists(ResourceKind kind, ResourceRef id);

    /**
     * Gets all namespaces currently loaded.
     * Implementations must return an immutable set or a defensive copy.
     */
    Set<String> loadedNamespaces();

    /**
     * Gets all resource IDs currently registered under a specific kind.
     * Implementations must return an immutable set or a defensive copy.
     */
    Set<ResourceRef> ids(ResourceKind kind);
}
