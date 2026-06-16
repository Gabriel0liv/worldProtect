package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable selector used to match a ResourceRef.
 * Supports exact, namespace wildcard, global wildcard, and tag matching patterns.
 */
public final class ResourceSelector {
    private final ResourceSelectorKind kind;
    private final ResourceRef id;
    private final String namespace;

    private ResourceSelector(ResourceSelectorKind kind, ResourceRef id, String namespace) {
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.id = id;
        this.namespace = namespace;
    }

    public static ResourceSelector exact(ResourceRef id) {
        Objects.requireNonNull(id, "id must not be null");
        return new ResourceSelector(ResourceSelectorKind.EXACT, id, null);
    }

    public static ResourceSelector namespaceWildcard(String namespace) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        if (namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace wildcard selector must specify a namespace");
        }
        for (int i = 0; i < namespace.length(); i++) {
            char c = namespace.charAt(i);
            if (!isValidNamespaceChar(c)) {
                throw new IllegalArgumentException("Invalid character '" + c + "' in namespace wildcard: " + namespace);
            }
        }
        return new ResourceSelector(ResourceSelectorKind.NAMESPACE_WILDCARD, null, namespace);
    }

    public static ResourceSelector globalWildcard() {
        return new ResourceSelector(ResourceSelectorKind.GLOBAL_WILDCARD, null, null);
    }

    public static ResourceSelector tag(ResourceRef tagId) {
        Objects.requireNonNull(tagId, "tagId must not be null");
        return new ResourceSelector(ResourceSelectorKind.TAG, tagId, null);
    }

    public static ResourceSelector parse(String input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Selector input must not be empty");
        }

        if (input.equals("*")) {
            return globalWildcard();
        }

        if (input.startsWith("#")) {
            String tagIdStr = input.substring(1);
            if (tagIdStr.isEmpty()) {
                throw new IllegalArgumentException("Tag selector cannot be empty after '#'");
            }
            ResourceRef tagId = ResourceRef.parse(tagIdStr);
            return tag(tagId);
        }

        if (input.endsWith(":*")) {
            String ns = input.substring(0, input.length() - 2);
            return namespaceWildcard(ns);
        }

        // Exact pattern
        ResourceRef ref = ResourceRef.parse(input);
        return exact(ref);
    }

    private static boolean isValidNamespaceChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.';
    }

    public ResourceSelectorKind kind() {
        return kind;
    }

    public Optional<ResourceRef> id() {
        return Optional.ofNullable(id);
    }

    public Optional<String> namespace() {
        return Optional.ofNullable(namespace);
    }

    public ResourceSelectorKind getKind() {
        return kind;
    }

    public ResourceRef getId() {
        return id;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * Evaluates if this selector matches the given resource reference.
     */
    public boolean matches(ResourceRef resource) {
        if (resource == null) {
            return false;
        }
        switch (kind) {
            case EXACT:
                return resource.equals(id);
            case NAMESPACE_WILDCARD:
                return resource.namespace().equals(namespace);
            case GLOBAL_WILDCARD:
                return true;
            case TAG:
                // Tag selection matches a list/set of resources, which requires a tag registry lookup.
                // Since this task does not implement a real tag registry lookup yet, tag matching
                // always returns false for raw ResourceRef matching.
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceSelector that = (ResourceSelector) o;
        return kind == that.kind &&
               Objects.equals(id, that.id) &&
               Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, id, namespace);
    }

    @Override
    public String toString() {
        switch (kind) {
            case EXACT:
                return id.toString();
            case NAMESPACE_WILDCARD:
                return namespace + ":*";
            case GLOBAL_WILDCARD:
                return "*";
            case TAG:
                return "#" + id.toString();
            default:
                return "unknown";
        }
    }
}
