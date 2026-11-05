# Drag-and-Drop Placement Fix for Multi-Terminal Components

## Problem

When dragging the BJT transistor onto the canvas, the error occurred:
```
Cannot invoke "network.ComponentNode.setPos(math.Coordinate)" 
because the return value of "network.Component.getInput()" is null
```

## Root Cause

The `Network.dropComponent()` method positions all components by assuming they have `input` and `output` nodes:

```java
public void dropComponent(Component component, Coordinate cursorPos) {
    // ... 
    component.getInput().setPos(...);      // ← NullPointerException if input is null
    component.getOutput().setPos(...);
}
```

The BJT's `build()` method was using `generateMultiTerminalNodes(3)` which creates a `terminals` ArrayList but **does not set the `input` and `output` fields** of the Component base class.

## Solution

In `BJTTransistor.build()`, after creating the 3 terminal nodes, also assign them to the `input` and `output` fields:

```java
@Override
public void build() {
    // Create 3 terminal nodes for multi-terminal architecture
    generateMultiTerminalNodes(3);
    
    // Access via terminals list
    emitterNode = getTerminal(0);
    baseNode = getTerminal(1);
    collectorNode = getTerminal(2);
    
    // For backward compatibility with Network.dropComponent positioning:
    // Set input/output to emitter/collector for drag-and-drop placement
    setInput(emitterNode);
    setOutput(collectorNode);
    
    // ... rest of initialization
}
```

## Design Rationale

**Dual Storage Approach**:
- **Multi-terminal storage**: `terminals[]` ArrayList for general N-pole support
- **Legacy compatibility**: `input`/`output` fields for existing positioning code

**Benefits**:
- ✅ Multi-terminal architecture remains generic (works for any N)
- ✅ Existing drag-and-drop code continues to work unchanged
- ✅ Backward compatible with 2-terminal components
- ✅ No changes needed to Network.java or MainController.java

**Trade-off**:
- Slight redundancy (emitterNode stored in both `terminals[0]` and `input`)
- Acceptable for clarity and compatibility

## Testing

```bash
mvn compile test

# Result: 36/36 tests PASS ✅
# Includes 10 BJT-specific tests
```

## Verification

Now you can:
1. Launch the GUI
2. Look for "BJT Transistor" in the left component panel
3. Drag it onto the canvas
4. ✅ Component will be placed without error
5. ✅ All 3 terminal nodes will be positioned correctly

## Related Files

- `BJTTransistor.java` - Multi-terminal BJT implementation
- `Component.java` - Base class with input/output + terminals fields
- `Network.java` - dropComponent() method
- `MainController.java` - Canvas drag-and-drop handler
