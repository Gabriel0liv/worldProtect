# WorldEdit Research Brief

This document outlines key components of WorldEdit that should be studied to guide the multi-module architecture of `worldProtect`.

## Crucial Components to Analyze

1. **Multi-Module Platform Split**:
   - Study how WorldEdit splits its project into a platform-agnostic `worldedit-core` and platform-specific implementations (e.g. `worldedit-fabric`, `worldedit-bukkit`, etc.).
   - Note the boundaries where player commands, world access, and configuration files are abstracted out of core logic.
2. **Actor & Session Abstraction**:
   - Inspect the `Actor`, `Player`, and `Session` abstractions. How are console operations, fake players, and real players normalized?
3. **Selection & Region Math**:
   - Analyze how vector points, cuboids, and polygons are modeled without using direct Minecraft block classes.
4. **Operation & History Logs**:
   - Study how WorldEdit schedules block modifications via `EditSession` and logs history for undo/redo commands.

## Research Guidelines
- This is purely for studying architecture and design patterns.
- Do **not** copy or adapt any lines of code directly into `worldProtect`.
