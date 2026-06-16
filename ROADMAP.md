# Roadmap

This roadmap details the sequential phases of development for the `worldProtect` mod.

## Phase 1: Repository Scaffold and Docs
- [x] Set up multi-module Gradle structure.
- [x] Configure Gradle wrapper.
- [x] Create core documentation, ADR, and research briefs.
- [x] Implement minimal Java placeholder APIs and basic tests.

## Phase 2: Core Domain Models
- [ ] Implement robust `Actor`, `ActorType`, `Decision`, and `DecisionReason` models.
- [ ] Define standard registry systems for core events.

## Phase 3: Region and Flag System
- [ ] Implement region storage, spatial indexing (e.g. 2D/3D trees or flat mappings).
- [ ] Build flag registries, flag types, and override mechanics.
- [ ] Set up priority resolution and inheritance rules.

## Phase 4: Basic Protection Queries
- [ ] Create query engine matching an `Actor`, a location, and a flag type to determine build/use permissions.
- [ ] Write unit tests for flag priority resolution.

## Phase 5: Minecraft Abstractions
- [ ] Implement loader-independent wrappers for Minecraft core components (`BlockPosRef`, `DimensionRef`, `ResourceRef`).
- [ ] Create snapshot containers (`BlockSnapshot`, `ItemStackSnapshot`, `NbtSnapshot`).

## Phase 6: NeoForge Adapter MVP
- [ ] Introduce NeoForge project plugins (UserDev, Loom, etc.).
- [ ] Bind basic NeoForge events (block breaks, player interactions, chest openings) to core protection queries.

## Phase 7: Fabric Adapter MVP
- [ ] Introduce Fabric project plugins (Loom, etc.).
- [ ] Bind basic Fabric events (player block place/break, block/entity interactions) to core protection queries.

## Phase 8: Audit Event Model
- [ ] Define audit log event types, structural details, and JSON serialization.

## Phase 9: Generic Inventory Snapshots
- [ ] Implement the generic inventory inspection utility.
- [ ] Add snapshot diff engines to detect player and automation item inputs/outputs.

## Phase 10: Database Queue
- [ ] Design the asynchronous database write queue.
- [ ] Implement SQLite and H2/MySQL database connections.

## Phase 11: Lookup Commands
- [ ] Build ingame lookup queries (`/wp lookup`).
- [ ] Implement inspector tool (e.g. inspector wand tool) to inspect block history.

## Phase 12: Rollback Planning
- [ ] Write the rollback engine to generate a transaction `RollbackPlan`.
- [ ] Safely apply the rollback plan on the main server thread.

## Phase 13: Optional WorldEdit Bridge
- [ ] Implement selection import from WorldEdit.
- [ ] Listen to WorldEdit operations to log block modifications inside WorldEdit selections.

## Phase 14: Compatibility Modules (Only when needed)
- [ ] Introduce optional compatibility modules for mods with complex custom inventory storage (e.g. Applied Energistics 2, Refined Storage).
