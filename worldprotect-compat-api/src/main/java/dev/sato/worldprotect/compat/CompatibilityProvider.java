package dev.sato.worldprotect.compat;

/**
 * Service interface for registering mod compatibility providers.
 */
public interface CompatibilityProvider {

    /**
     * Unique identifier for the provider (e.g. "ae2", "refinedstorage").
     */
    String getProviderName();

    /**
     * Initializes the compatibility module.
     */
    void initialize();
}
