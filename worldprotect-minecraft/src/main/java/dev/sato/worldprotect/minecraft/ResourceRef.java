package dev.sato.worldprotect.minecraft;

import java.util.Objects;

/**
 * Platform-independent representation of a ResourceLocation / Identifier (namespace:path).
 */
public final class ResourceRef {
    private final String namespace;
    private final String path;

    public ResourceRef(String namespace, String path) {
        this.namespace = Objects.requireNonNull(namespace, "namespace must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    public static ResourceRef parse(String resourceString) {
        Objects.requireNonNull(resourceString, "resourceString must not be null");
        int split = resourceString.indexOf(':');
        if (split == -1) {
            return new ResourceRef("minecraft", resourceString);
        }
        return new ResourceRef(resourceString.substring(0, split), resourceString.substring(split + 1));
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceRef that = (ResourceRef) o;
        return namespace.equals(that.namespace) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
