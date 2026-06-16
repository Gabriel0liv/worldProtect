package dev.sato.worldprotect.minecraft;

import java.util.Objects;

/**
 * Platform-independent representation of a ResourceLocation / Identifier (namespace:path).
 */
public final class ResourceRef {
    private final String namespace;
    private final String path;

    public ResourceRef(String namespace, String path) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        Objects.requireNonNull(path, "path must not be null");
        
        if (namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace must not be empty");
        }
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Path must not be empty");
        }
        
        // Validate namespace rules: lowercase letters, digits, underscore, dash, dot
        for (int i = 0; i < namespace.length(); i++) {
            char c = namespace.charAt(i);
            if (!isValidNamespaceChar(c)) {
                throw new IllegalArgumentException("Invalid character '" + c + "' in namespace: " + namespace);
            }
        }
        
        // Validate path rules: lowercase letters, digits, underscore, dash, dot, slash
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (!isValidPathChar(c)) {
                throw new IllegalArgumentException("Invalid character '" + c + "' in path: " + path);
            }
        }
        
        this.namespace = namespace;
        this.path = path;
    }

    private static boolean isValidNamespaceChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.';
    }

    private static boolean isValidPathChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/';
    }

    public static ResourceRef of(String namespace, String path) {
        return new ResourceRef(namespace, path);
    }

    public static ResourceRef parse(String input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input must not be empty");
        }
        int firstColon = input.indexOf(':');
        if (firstColon == -1) {
            return new ResourceRef("minecraft", input);
        }
        
        // Reject multiple colons
        if (input.indexOf(':', firstColon + 1) != -1) {
            throw new IllegalArgumentException("Resource ID must not contain multiple colons: " + input);
        }
        
        String namespace = input.substring(0, firstColon);
        String path = input.substring(firstColon + 1);
        
        return new ResourceRef(namespace, path);
    }

    public String namespace() {
        return namespace;
    }

    public String path() {
        return path;
    }

    public String asString() {
        return toString();
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
