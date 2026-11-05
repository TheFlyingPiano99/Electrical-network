# Multi-Terminal Component Architecture

## Overview

The simulator has been refactored to support components with **3 or more poles (terminals)**, while maintaining backward compatibility with existing 2-terminal components (resistors, capacitors, inductors, etc.).

## Architecture

### Component Terminal Structure

**2-Terminal Components (Legacy)**
- Use `getInput()` and `getOutput()` methods
- No change to existing code
- Examples: Resistor, Capacitor, Inductor, Wire, Voltage/Current Sources

**Multi-Terminal Components (3+ Poles)**
- Use `getTerminals()` to access all terminal nodes
- Use `getTerminal(index)` for indexed access
- Use `getPoleCount()` to query number of terminals
- Examples: BJT Transistor (3 terminals), MOSFET (4 terminals), Thyristor (3 terminals)

### Component.java - New API

#### New Fields
```java
protected ArrayList<ComponentNode> terminals;
```
Stores terminal nodes for multi-terminal components. Empty for 2-terminal components.

#### New Methods

**`getPoleCount() : int`**
- Returns number of poles
- 2-terminal components return 2
- Multi-terminal components return terminals.size()

**`getTerminal(index : int) : ComponentNode`**
- Returns terminal node by index
- For 2-terminal: 0=input, 1=output (backward compatible)
- For multi-terminal: accesses terminals list

**`generateMultiTerminalNodes(numberOfTerminals : int) : void`**
- Creates N terminal nodes
- Registers with parent network
- Assigns input/output roles to first and last terminals

**`removeMultiTerminalNodes() : void`**
- Cleans up all terminal nodes
- Removes from component nodes list
- Handles edge disconnections

### Terminal Node Indexing Convention

For consistency across different component types:

**BJT Transistor (3 terminals)**
- Index 0: Emitter (input role)
- Index 1: Base (middle)
- Index 2: Collector (output role)

**MOSFET (4 terminals, future)**
- Index 0: Source (input role)
- Index 1: Gate
- Index 2: Body (bulk)
- Index 3: Drain (output role)

**Thyristor (3 terminals, future)**
- Index 0: Cathode (input role)
- Index 1: Gate
- Index 2: Anode (output role)

## Implementation: BJT Transistor Example

### build() Method
```java
@Override
public void build() {
    // Generate 3 terminal nodes
    generateMultiTerminalNodes(3);
    
    // Access by index
    emitterNode = getTerminal(0);
    baseNode = getTerminal(1);
    collectorNode = getTerminal(2);
    
    // ... rest of initialization
}
```

### destroy() Method
```java
@Override
public void destroy() {
    // Clean up all terminals
    removeMultiTerminalNodes();
    // ... remove edges
}
```

### Terminal Mapping
```java
@Override
public TerminalMapping getTerminalMapping() {
    return new BJTTerminalMapping(baseNode, collectorNode, emitterNode, 
                                  getParent());
}
```

## Harmonic Balance Integration

The `TerminalMapping` interface maps reduced terminal coordinates to global node voltages:

```
u = B * V_nodes

where:
  u   = reduced terminal voltage vector (device-specific dimension)
  B   = mapping matrix (terminalCount x numNodes)
  V   = global node voltage vector
```

### Example: BJT Mapping

For BJT with 3 terminal nodes (Emitter, Base, Collector):

**Reduced coordinates**: u = [v_B - v_E, v_B - v_C]^T (2D)

**Mapping matrix B** (2 x numNodes):
```
       | v_E | v_B | v_C | ... |
B[0] = | -1  |  1  |  0  | ... |  (v_B - v_E)
B[1] = |  0  |  1  | -1  | ... |  (v_B - v_C)
```

## Multi-Component Positioning

The simulator's drag-and-drop mechanism automatically handles multi-terminal components:

1. **Canvas Drop Detection**: MainController detects component type
2. **Multi-Terminal Recognition**: Component framework queries getPoleCount()
3. **Terminal Positioning**: Each terminal gets initial position based on component bounds
4. **Network Registration**: All terminals auto-registered via generateMultiTerminalNodes()

## Future Component Types

The framework is ready for:

### N-Channel MOSFET (4 terminals)
```java
public class MOSFETTransistor extends Component implements NonlinearDevice {
    @Override
    public void build() {
        generateMultiTerminalNodes(4);  // Source, Gate, Body, Drain
        sourceNode = getTerminal(0);
        gateNode = getTerminal(1);
        bodyNode = getTerminal(2);
        drainNode = getTerminal(3);
    }
}
```

### Silicon-Controlled Rectifier / Thyristor (3 terminals)
```java
public class Thyristor extends Component implements NonlinearDevice {
    @Override
    public void build() {
        generateMultiTerminalNodes(3);  // Cathode, Gate, Anode
        cathodeNode = getTerminal(0);
        gateNode = getTerminal(1);
        anodeNode = getTerminal(2);
    }
}
```

### Operational Amplifier (5+ terminals, future)
```java
public class OpAmp extends Component implements NonlinearDevice {
    @Override
    public void build() {
        generateMultiTerminalNodes(5);  // V+, V-, Vin+, Vin-, Vout
        // Could represent as subcircuit in harmonic balance
    }
}
```

## Backward Compatibility

All existing 2-terminal components continue to work unchanged:

```java
// Legacy code still works
Resistance r = new Resistance(1000);
r.build();
ComponentNode input = r.getInput();      // Still works
ComponentNode output = r.getOutput();    // Still works
```

The empty `terminals` list ensures legacy behavior:
- `getPoleCount()` returns 2 for components with empty terminals
- `getTerminal(0)` delegates to `getInput()`
- `getTerminal(1)` delegates to `getOutput()`

## Visualization and Manipulation

### Multi-Terminal Component Visualization

Drawing multi-terminal components requires custom `draw()` method:

```java
@Override
public void draw(GraphicsContext ctx) {
    // Access all terminal positions
    for (int i = 0; i < getPoleCount(); ++i) {
        ComponentNode terminal = getTerminal(i);
        Coordinate pos = terminal.getPos();
        // Draw terminal symbol at pos
    }
    
    // Draw component body/symbol
    // Connect terminals visually
}
```

### Interactive Manipulation

Multi-terminal components are manipulated like 2-terminal components:

1. **Grab/Move**: All terminals move together (component is rigid)
2. **Connection**: Network connects terminals to nodes/wires independently
3. **Selection**: Entire component selected, all terminals highlighted

## Network Simulation

### Linear Component Assembly

For linear 2-terminal components:
```java
// Admittance stamp (2x2)
Y[a,a] += y;
Y[a,b] -= y;
Y[b,a] -= y;
Y[b,b] += y;
```

### Nonlinear Component Assembly

Multi-terminal devices integrate through harmonic balance:

1. Extract terminal voltages via TerminalMapping
2. Evaluate device model → reduced space currents f(u)
3. Transform back to node currents: i_node = B^T * f(u)
4. Assemble into global admittance matrix/Jacobian

## Testing Multi-Terminal Components

### Geometric Tests
```java
@Test
void testBJTTerminalPositioning() {
    BJTTransistor bjt = new BJTTransistor();
    bjt.build();
    
    assertEquals(3, bjt.getPoleCount());
    assertNotNull(bjt.getTerminal(0));  // Emitter
    assertNotNull(bjt.getTerminal(1));  // Base
    assertNotNull(bjt.getTerminal(2));  // Collector
}
```

### Functional Tests
```java
@Test
void testBJTEvaluation() {
    BJTTransistor bjt = new BJTTransistor();
    bjt.build();
    
    TerminalMapping mapping = bjt.getTerminalMapping();
    assertEquals(2, mapping.getTerminalDimension());  // Reduced space
    assertEquals(3, bjt.getPoleCount());             // Physical space
}
```

## Performance Considerations

### Memory
- 2-terminal: 1 empty ArrayList overhead (negligible)
- 3-terminal: 3 ComponentNode references + 1 ArrayList
- N-terminal: N ComponentNode references + 1 ArrayList

### Computational Complexity
- Terminal access: O(1) via ArrayList.get()
- Position updates: O(N) for N terminals (parallel possible)
- Network assembly: O(N * M^2) where M = reduced terminal dimension

## Implementation Checklist for New Components

To add a new multi-terminal component type:

1. ✅ Extend `Component` class
2. ✅ Implement `NonlinearDevice` interface
3. ✅ Override `build()` with `generateMultiTerminalNodes(N)`
4. ✅ Implement `getTerminalMapping()` returning TerminalMapping
5. ✅ Override `evaluateTimeDomain()` using terminal mapping
6. ✅ Override `destroy()` with `removeMultiTerminalNodes()`
7. ✅ Override `draw()` for custom multi-terminal visualization
8. ✅ Add to MainController.java drag-and-drop handler
9. ✅ Add language strings for component name
10. ✅ Write unit tests for model accuracy
11. ✅ Add save/load serialization

## See Also

- [Nonlinear Transistor Implementation](NONLINEAR_IMPLEMENTATION.md)
- LaTeX specification: `docs/nonlinear-transistor-implementation.tex`
- Component base class: `network/Component.java`
- Example implementation: `network/BJTTransistor.java`
