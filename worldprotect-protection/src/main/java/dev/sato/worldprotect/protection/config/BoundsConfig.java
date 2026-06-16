package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration representation of a region's spatial boundaries.
 */
public final class BoundsConfig {
    private final BoundsType type;
    private final BlockPosRef min;
    private final BlockPosRef max;

    private BoundsConfig(BoundsType type, BlockPosRef min, BlockPosRef max) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.min = Objects.requireNonNull(min, "min must not be null");
        this.max = Objects.requireNonNull(max, "max must not be null");
    }

    public static BoundsConfig cuboid(BlockPosRef min, BlockPosRef max) {
        return new BoundsConfig(BoundsType.CUBOID, min, max);
    }

    public BoundsType type() {
        return type;
    }

    public BlockPosRef min() {
        return min;
    }

    public BlockPosRef max() {
        return max;
    }

    public BoundsType getType() {
        return type;
    }

    public BlockPosRef getMin() {
        return min;
    }

    public BlockPosRef getMax() {
        return max;
    }

    public ConfigValidationResult validate() {
        return validate("bounds");
    }

    public ConfigValidationResult validate(String path) {
        Objects.requireNonNull(path, "path must not be null");
        List<ConfigValidationMessage> msgs = new ArrayList<>();
        if (min.x() > max.x()) {
            msgs.add(ConfigValidationMessage.error(path + ".min", "min.x (" + min.x() + ") must be <= max.x (" + max.x() + ")"));
        }
        if (min.y() > max.y()) {
            msgs.add(ConfigValidationMessage.error(path + ".min", "min.y (" + min.y() + ") must be <= max.y (" + max.y() + ")"));
        }
        if (min.z() > max.z()) {
            msgs.add(ConfigValidationMessage.error(path + ".min", "min.z (" + min.z() + ") must be <= max.z (" + max.z() + ")"));
        }
        return ConfigValidationResult.of(msgs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundsConfig that = (BoundsConfig) o;
        return type == that.type &&
               min.equals(that.min) &&
               max.equals(that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, min, max);
    }

    @Override
    public String toString() {
        return "BoundsConfig{type=" + type + ", min=" + min + ", max=" + max + "}";
    }
}
