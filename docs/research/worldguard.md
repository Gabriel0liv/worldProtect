# WorldGuard Research Brief

This document outlines key components of WorldGuard that should be studied to guide the implementation of the `worldProtect` protection system.

## Crucial Components to Analyze

1. **Spatial Region Database & Indexing**:
   - Inspect how cuboid, polygonal, and global regions are modeled.
   - Analyze how regions are stored, parsed (YAML/JSON), and queried efficiently in 3D space.
2. **Flag System**:
   - Analyze the flag registry mechanism and how flag values (state, string, integer, set, etc.) are declared.
   - Look at the priority resolution rules: how flags are evaluated when regions overlap.
3. **Domain Memberships**:
   - Study how `owners` and `members` are mapped to regions using UUIDs, player names, or permission groups.
4. **Bukkit/Spigot Event Adaptation**:
   - Trace how WorldGuard intercepts block physics, explosions, player movements, and inventory interactions.
   - Note the platform-specific boundary where events are converted into region queries.

## Research Guidelines
- This is purely for studying architecture and design patterns.
- Do **not** copy or adapt any lines of code directly into `worldProtect`.
