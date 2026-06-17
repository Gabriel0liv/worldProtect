package dev.sato.worldprotect.config.load;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigLoadOptionsTest {

    @Test
    public void testFactoryDefaults() {
        ConfigLoadOptions opts = ConfigLoadOptions.defaults();
        assertFalse(opts.validateResources());
        assertFalse(opts.failOnWarnings());
    }

    @Test
    public void testFactoryValidatingResources() {
        ConfigLoadOptions opts = ConfigLoadOptions.validatingResources();
        assertTrue(opts.validateResources());
        assertFalse(opts.failOnWarnings());
    }

    @Test
    public void testFactoryStrict() {
        ConfigLoadOptions opts = ConfigLoadOptions.strict();
        assertTrue(opts.validateResources());
        assertTrue(opts.failOnWarnings());
    }

    @Test
    public void testModifiers() {
        ConfigLoadOptions opts = ConfigLoadOptions.defaults()
                .withValidateResources(true)
                .withFailOnWarnings(true);

        assertTrue(opts.validateResources());
        assertTrue(opts.failOnWarnings());

        ConfigLoadOptions modified = opts.withValidateResources(false);
        assertFalse(modified.validateResources());
        assertTrue(modified.failOnWarnings());

        ConfigLoadOptions modified2 = opts.withFailOnWarnings(false);
        assertTrue(modified2.validateResources());
        assertFalse(modified2.failOnWarnings());
    }

    @Test
    public void testEqualsAndHashCode() {
        ConfigLoadOptions opts1 = ConfigLoadOptions.defaults();
        ConfigLoadOptions opts2 = ConfigLoadOptions.defaults();
        ConfigLoadOptions opts3 = ConfigLoadOptions.strict();

        assertEquals(opts1, opts2);
        assertNotEquals(opts1, opts3);
        assertEquals(opts1.hashCode(), opts2.hashCode());
        assertNotEquals(opts1.hashCode(), opts3.hashCode());
    }
}
