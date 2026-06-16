# Architecture

`worldProtect` uses a highly decoupled multi-module architecture designed to split the domain logic, region protection, audit logs, and modloader-specific integration layers.

## High-Level Design: Core vs. Platform (WorldEdit Inspiration)

Following the proven architecture of WorldEdit, `worldProtect` separates its design into core business logic and loader-specific platform adapters:
- **Core Modules (`worldprotect-core`, `worldprotect-protection`, `worldprotect-audit`)**: House Pure Java code, domain models, and logic rules. They do not depend on loader APIs or Minecraft classes.
- **Platform Adapters (`worldprotect-fabric`, `worldprotect-neoforge`)**: Bind the core logic to the game loop. They intercept events (e.g. block placement, right-clicks) and forward them to the core services.

This ensures that the business logic can be tested entirely using lightweight JUnit tests without spinning up a heavy Minecraft server instance.

```mermaid
graph TD
    subgraph Core Logic
        core[worldprotect-core]
        protection[worldprotect-protection] --> core
        audit[worldprotect-audit] --> core
        compat[worldprotect-compat-api] --> core
    end
    
    subgraph Minecraft Abstraction
        minecraft[worldprotect-minecraft] --> core
        protection --> minecraft
        audit --> minecraft
        compat --> minecraft
    end

    subgraph Platform Adapters
        neoforge[worldprotect-neoforge] --> protection
        neoforge --> audit
        neoforge --> compat
        
        fabric[worldprotect-fabric] --> protection
        fabric --> audit
        fabric --> compat
    end
```

## Generic Inventory Strategy

To support arbitrary modded inventories without manually coding integrations for thousands of mods, we employ a layered inventory logging and access strategy:

1. **Generic Inventory Access**: First, access containers using standard Minecraft container and inventory classes (`Container` in vanilla/loader terms).
2. **NeoForge Capabilities / Fabric Transfer API**: Later, wrap capability-based (`IItemHandler`) and Fabric Transfer API (`Storage<ItemVariant>`) interfaces to track transactions on advanced pipes, chests, and machines.
3. **Snapshot and Diff Fallback**: If an inventory does not expose a standard API, we take periodic snapshots and calculate the diff to determine item changes.
4. **Compatibility Modules**: Added only as a last resort for virtual, remote, or networked storage systems (such as AE2 or Refined Storage) where item storage is not physically local or represented by a standard inventory.

## Threading Rules

To ensure server performance and database integrity, we adhere to the following concurrency guidelines:

- **Asynchronous Database Log Queue**: All audit logging, database writes, and lookup queries must run asynchronously off the main server thread. Database operations must never block the main game loop.
- **Synchronous World Modifications**: Any operations modifying blocks, entities, or inventories (such as executing a rollback) MUST occur on the Minecraft server main thread to prevent race conditions and chunk corruption.
- **Rollback Planning Separation**: When performing a rollback, the system must generate a read-only `RollbackPlan` first (asynchronously). Once finalized, the plan is scheduled to apply its operations on the main thread incrementally.

## Loader Boundary Rules

- **Platform Modules are Adapters Only**: `worldprotect-fabric` and `worldprotect-neoforge` are adapters that translate platform-specific events (e.g., Fabric events or NeoForge event bus events) to `worldProtect` core API calls.
- **No Domain Logic in Loader Modules**: Code inside loader modules must only handle bootstrap, event registration, injection (mixins), configuration mapping, and command registration. Actual logic remains in `worldprotect-core`, `worldprotect-protection`, and `worldprotect-audit`.
