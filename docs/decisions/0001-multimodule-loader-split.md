# ADR 0001: Multi-Module Loader Split

## Context
A major challenge in modern Minecraft mod development is supporting multiple mod loaders (specifically Fabric and NeoForge) and keeping core business logic decoupled from the game's internal events. Monolithic structures often end up blending loader-specific events, database transactions, and region calculations together. This makes the codebase difficult to test, maintain, and port across versions.

## Decision
We will structure `worldProtect` as a multi-module Gradle project, strictly separating loader-independent core domains from platform-specific adapters:

1. **`worldprotect-core`**: Defines pure domain logic (Actor models, decisions, action permissions) and has no dependency on Minecraft or any modloader APIs.
2. **`worldprotect-minecraft`**: Loader-independent Minecraft wrappers (Dimension, ResourceKey, NBT structures).
3. **`worldprotect-protection`**: Manages region definitions, overlapping regions, flags, and priorities.
4. **`worldprotect-audit`**: Defines transaction logging, database write queues, lookup queries, and rollback planning.
5. **`worldprotect-compat-api`**: Exposes extension points for custom external mod integrations.
6. **`worldprotect-worldedit-bridge`**: Optional bridge module for future WorldEdit selection import and operation logging.
7. **`worldprotect-fabric`**: Adapter containing Fabric bootstrap entrypoints, Fabric events, and Mixins.
8. **`worldprotect-neoforge`**: Adapter containing NeoForge bootstrap entrypoints, event listeners, and NeoForge capabilities.
9. **`verification`**: Holds end-to-end integration and architectural tests.

## Consequences
- **Pros**:
  - Domain, protection, and audit logic can be tested locally using lightning-fast JUnit tests without starting a heavy Minecraft server context.
  - Porting to new Minecraft versions or new loaders (e.g. Sponge) requires changes only in the adapter modules, leaving the core protection and database logic untouched.
  - Strict compile-time safety prevents leaking platform-specific code into the core logic.
- **Cons**:
  - Requires maintaining multiple submodules and managing cross-module dependency flows in Gradle.
  - Requires writing adapter classes to translate vanilla/loader types to core types.
