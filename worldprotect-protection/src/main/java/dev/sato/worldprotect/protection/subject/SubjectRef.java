package dev.sato.worldprotect.protection.subject;

import java.util.Objects;
import java.util.UUID;

/**
 * An immutable reference to a subject type and ID.
 */
public final class SubjectRef {
    private final SubjectType type;
    private final String id;

    private SubjectRef(SubjectType type, String id) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(id, "id must not be null");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        // Apply type-specific validation rules
        switch (type) {
            case PLAYER:
                // Parse to validate UUID format
                try {
                    UUID.fromString(id);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid UUID format for player subject ID: " + id, e);
                }
                this.id = id.toLowerCase();
                break;
            case GROUP:
                if (id.length() < 1 || id.length() > 64) {
                    throw new IllegalArgumentException("Group ID length must be between 1 and 64 characters");
                }
                if (!id.equals(id.toLowerCase())) {
                    throw new IllegalArgumentException("Group ID must be lowercase: " + id);
                }
                for (int i = 0; i < id.length(); i++) {
                    char c = id.charAt(i);
                    if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '.' || c == '-')) {
                        throw new IllegalArgumentException("Invalid character in group ID: " + c);
                    }
                }
                this.id = id;
                break;
            case CONSOLE:
                if (!"console".equals(id)) {
                    throw new IllegalArgumentException("Console subject ID must be 'console'");
                }
                this.id = id;
                break;
            case SYSTEM:
                if (!"system".equals(id)) {
                    throw new IllegalArgumentException("System subject ID must be 'system'");
                }
                this.id = id;
                break;
            default:
                this.id = id;
                break;
        }
    }

    public static SubjectRef of(SubjectType type, String id) {
        return new SubjectRef(type, id);
    }

    public static SubjectRef player(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid must not be null");
        return new SubjectRef(SubjectType.PLAYER, uuid.toString().toLowerCase());
    }

    public static SubjectRef group(String name) {
        return new SubjectRef(SubjectType.GROUP, name);
    }

    public static SubjectRef console() {
        return new SubjectRef(SubjectType.CONSOLE, "console");
    }

    public static SubjectRef system() {
        return new SubjectRef(SubjectType.SYSTEM, "system");
    }

    public SubjectType type() {
        return type;
    }

    public String id() {
        return id;
    }

    public boolean isPlayer() {
        return type == SubjectType.PLAYER;
    }

    public boolean isGroup() {
        return type == SubjectType.GROUP;
    }

    public boolean isConsole() {
        return type == SubjectType.CONSOLE;
    }

    public boolean isSystem() {
        return type == SubjectType.SYSTEM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectRef that = (SubjectRef) o;
        return type == that.type && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return type + ":" + id;
    }
}
