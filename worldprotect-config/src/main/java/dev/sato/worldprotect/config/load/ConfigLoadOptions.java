package dev.sato.worldprotect.config.load;

/**
 * Immutable configuration parameters controlling the configuration load behavior.
 */
public final class ConfigLoadOptions {
    private final boolean validateResources;
    private final boolean failOnWarnings;

    private ConfigLoadOptions(boolean validateResources, boolean failOnWarnings) {
        this.validateResources = validateResources;
        this.failOnWarnings = failOnWarnings;
    }

    public static ConfigLoadOptions defaults() {
        return new ConfigLoadOptions(false, false);
    }

    public static ConfigLoadOptions validatingResources() {
        return new ConfigLoadOptions(true, false);
    }

    public static ConfigLoadOptions strict() {
        return new ConfigLoadOptions(true, true);
    }

    public boolean validateResources() {
        return validateResources;
    }

    public boolean failOnWarnings() {
        return failOnWarnings;
    }

    public ConfigLoadOptions withValidateResources(boolean validateResources) {
        return new ConfigLoadOptions(validateResources, this.failOnWarnings);
    }

    public ConfigLoadOptions withFailOnWarnings(boolean failOnWarnings) {
        return new ConfigLoadOptions(this.validateResources, failOnWarnings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigLoadOptions that = (ConfigLoadOptions) o;
        return validateResources == that.validateResources &&
               failOnWarnings == that.failOnWarnings;
    }

    @Override
    public int hashCode() {
        int result = (validateResources ? 1 : 0);
        result = 31 * result + (failOnWarnings ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigLoadOptions{validateResources=" + validateResources +
               ", failOnWarnings=" + failOnWarnings + "}";
    }
}
