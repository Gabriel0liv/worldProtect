package dev.sato.worldprotect.protection.flag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry mapping flag keys to their definition details.
 */
public final class FlagRegistry {
    private final Map<FlagKey, FlagDefinition> definitions = new HashMap<>();

    public FlagRegistry() {}

    public synchronized void register(FlagDefinition definition) {
        Objects.requireNonNull(definition, "definition must not be null");
        FlagKey key = definition.key();
        if (definitions.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate flag registration: " + key.getValue());
        }
        definitions.put(key, definition);
    }

    public synchronized Optional<FlagDefinition> get(FlagKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return Optional.ofNullable(definitions.get(key));
    }

    public synchronized boolean exists(FlagKey key) {
        Objects.requireNonNull(key, "key must not be null");
        return definitions.containsKey(key);
    }

    public synchronized Collection<FlagDefinition> definitions() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public static FlagRegistry withBuiltIns() {
        FlagRegistry registry = new FlagRegistry();
        for (FlagDefinition definition : BuiltInFlags.ALL) {
            registry.register(definition);
        }
        return registry;
    }
}
