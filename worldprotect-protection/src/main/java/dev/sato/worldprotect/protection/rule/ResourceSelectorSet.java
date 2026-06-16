package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Immutable, defensively-copied set of ResourceSelectors.
 */
public final class ResourceSelectorSet {
    private static final ResourceSelectorSet EMPTY = new ResourceSelectorSet(List.of());

    private final List<ResourceSelector> selectors;

    private ResourceSelectorSet(List<ResourceSelector> selectors) {
        this.selectors = List.copyOf(selectors);
    }

    public static ResourceSelectorSet empty() {
        return EMPTY;
    }

    public static ResourceSelectorSet of(Collection<ResourceSelector> selectors) {
        Objects.requireNonNull(selectors, "selectors must not be null");
        for (ResourceSelector selector : selectors) {
            Objects.requireNonNull(selector, "selector element must not be null");
        }
        return new ResourceSelectorSet(List.copyOf(selectors));
    }

    public List<ResourceSelector> selectors() {
        return selectors;
    }

    public List<ResourceSelector> getSelectors() {
        return selectors;
    }

    public boolean isEmpty() {
        return selectors.isEmpty();
    }

    /**
     * Checks if any selector in this set matches the given resource.
     */
    public boolean matches(ResourceRef resource) {
        if (resource == null) {
            return false;
        }
        for (ResourceSelector selector : selectors) {
            if (selector.matches(resource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceSelectorSet that = (ResourceSelectorSet) o;
        return selectors.equals(that.selectors);
    }

    @Override
    public int hashCode() {
        return selectors.hashCode();
    }

    @Override
    public String toString() {
        return selectors.toString();
    }
}
