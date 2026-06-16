# CoreProtect Research Brief

This document outlines key components of CoreProtect that should be studied to guide the implementation of the `worldProtect` audit system.

## Crucial Components to Analyze

1. **Event Listeners**:
   - Trace how block placement, block destruction, entity damage, container transactions, and chat events are captured.
   - Note which events are processed synchronously vs asynchronously.
2. **Consumer/Queue System**:
   - CoreProtect utilizes a double-buffered queue or block consumer thread (`Consumer.java`) to handle database writes asynchronously.
   - Examine how data is batch-committed to avoid database locks and server performance degradation.
3. **Database Logger**:
   - Inspect the database schema: how coordinates, block states, and item data (including NBT metadata) are serialized and indexed.
4. **Lookup Interface**:
   - Understand the lookup command builder and result formatting.
   - Note the spatial and temporal query indices (e.g. searching changes in a radius, coordinates, time range, or by specific users).
5. **Rollback & Restore Engine**:
   - Trace how rollbacks build a block change sequence.
   - Check how container transactions are rolled back step-by-step to prevent duplications or voiding items.
6. **Container Logging**:
   - Analyze how container open/close snapshots and item transaction logs are generated, specifically dealing with transaction differences.

## Research Guidelines
- This is purely for studying architecture and design patterns.
- Do **not** copy or adapt any lines of code directly into `worldProtect`.
