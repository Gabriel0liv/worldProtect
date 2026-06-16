package dev.sato.worldprotect.minecraft.registry;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fake test-only implementation of ResourceRegistryView.
 * Prevents mutation of internal state through exposed getters.
 */
public final class FakeResourceRegistryView implements ResourceRegistryView {
    private final Set<String> loadedNamespaces = new HashSet<>();
    private final Map<ResourceKind, Set<ResourceRef>> registeredIds = new HashMap<>();

    public void addNamespace(String namespace) {
        if (namespace != null && !namespace.isEmpty()) {
            loadedNamespaces.add(namespace);
        }
    }

    public void registerId(ResourceKind kind, ResourceRef id) {
        if (kind != null && id != null) {
            registeredIds.computeIfAbsent(kind, k -> new HashSet<>()).add(id);
            loadedNamespaces.add(id.namespace());
        }
    }

    @Override
    public boolean namespaceLoaded(String namespace) {
        return loadedNamespaces.contains(namespace);
    }

    @Override
    public boolean exists(ResourceKind kind, ResourceRef id) {
        Set<ResourceRef> ids = registeredIds.get(kind);
        return ids != null && ids.contains(id);
    }

    @Override
    public Set<String> loadedNamespaces() {
        return Set.copyOf(loadedNamespaces);
    }

    @Override
    public Set<ResourceRef> ids(ResourceKind kind) {
        Set<ResourceRef> ids = registeredIds.get(kind);
        return ids == null ? Set.of() : Set.copyOf(ids);
    }
}
