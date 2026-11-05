# Architecture Overview: Multi-Terminal Nonlinear Components

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    GUI Layer (JavaFX)                       │
│                  MainController.java                        │
│     ┌──────────────────────────────────────────────┐        │
│     │  Component List & Drag-Drop Handler          │        │
│     │  - "Resistor", "Capacitor", "BJT Transistor" │        │
│     └──────────────────────────────────────────────┘        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               Component Architecture (Java)                 │
│                  Component.java (Base)                      │
│     ┌──────────────────────────────────────────────┐        │
│     │  2-Terminal (Legacy)        Multi-Terminal   │        │
│     │  ─────────────────         ──────────────    │        │
│     │  • input/output            • terminals[]    │        │
│     │  • getPoleCount()=2         • getPoleCount()=N│        │
│     │  • getTerminal(i)           • getTerminal(i) │        │
│     │                             • generateMulti… │        │
│     │  Examples:                  • removeMulti…   │        │
│     │  - Resistance               │                │        │
│     │  - Capacitor                Examples:        │        │
│     │  - Inductor                 - BJTTransistor  │        │
│     │  - Wire                     - MOSFET (future)│        │
│     │                             - Thyristor      │        │
│     └──────────────────────────────────────────────┘        │
└──────────────────────────┬──────────────────────────────────┘
                           │
        ┌──────────────────┴──────────────────┐
        ▼                                      ▼
┌──────────────────────────┐  ┌──────────────────────────────┐
│  Nonlinear Devices       │  │  Linear Components           │
│  (Java Interface Layer)  │  │  (Existing Solver)           │
└──────────────────────────┘  └──────────────────────────────┘
    │                                      │
    ├─ NonlinearDevice.java                ├─ Resistance.java
    │  • evaluateTimeDomain()              ├─ Capacitor.java
    │  • evaluateDerivatives()             ├─ Inductor.java
    │  • getTerminalMapping()              └─ VoltageSrc.java
    │                                      
    └─ BJTTransistor.java (Implements)
       • 3 Terminal Nodes
       • Ebers-Moll Model
       • Terminal Mapping: u = [v_B-v_E, v_B-v_C]
       • Editable: alphaF, alphaR, I_ES, I_CS

┌──────────────────────────────────────────────────────────────────┐
│              Harmonic Balance Solver Layer (Java)                │
│                    HarmonicBalanceSolver.java                    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  1. DC Operating Point      (Existing linear solver)       │  │
│  │  2. Harmonic Spectrum Init   (Active frequencies)          │  │
│  │  3. Newton Iteration Loop                                  │  │
│  │     ├─ Reconstruct time-domain (inverse DFT via C++)       │  │
│  │     ├─ Evaluate nonlinear devices (BJT model)              │  │
│  │     ├─ Assemble admittance matrix + Jacobian               │  │
│  │     ├─ Solve linear system (direct solver)                 │  │
│  │     └─ Damped step update (adaptive damping)               │  │
│  │  4. Convergence Check        (Residual norm)               │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│            JNI Bridge & C++ Native Layer                         │
│            HarmonicBalanceJNI.java ←→ harmonic_balance_jni.cpp  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  Frequency ←→ Time Domain Transforms:                     │  │
│  │  • frequencyToTime()  : Inverse DFT                        │  │
│  │  • timeToFrequency()  : Forward DFT                        │  │
│  │                                                            │  │
│  │  Device Evaluation (Frequency Domain):                    │  │
│  │  • evaluateBJT()      : Current computation              │  │
│  │  • calculateBJTJacobian() : Derivative matrices           │  │
│  │                                                            │  │
│  │  Safety Features:                                         │  │
│  │  • safe_exp() : Overflow/underflow protection             │  │
│  │  • Conjugate symmetry : Real-signal enforcement            │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                   │
│  Compiled: ElectricalJNI.dll (CMake + MSBuild)                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Data Flow: Nonlinear Component Evaluation

### Time-Domain Evaluation Path

```
┌─────────────────────────────────┐
│  Global Node Voltages           │
│  V = [v_1, v_2, ..., v_N]       │
└───────────┬─────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────┐
│  BJT Terminal Mapping                           │
│  u = B × V                                      │
│  ├─ B ∈ ℝ^(2×N)  [sparse mapping matrix]       │
│  └─ u ∈ ℝ^2       [v_B-v_E, v_B-v_C]           │
└───────────┬─────────────────────────────────────┘
            │
            ▼
┌──────────────────────────────────────────┐
│  BJT Device Model (Time Domain)          │
│  i_F = I_ES × (exp(u_1/V_T) - 1)        │
│  i_R = I_CS × (exp(u_2/V_T) - 1)        │
│                                          │
│  Output Terminal Currents:               │
│  i_c = α_F × i_F - i_R                  │
│  i_b = i_F/β_F + i_R/β_R                │
│  i_e = -i_F + α_R × i_R                 │
│                                          │
│  Result: f(u) ∈ ℝ^3                    │
└───────────┬──────────────────────────────┘
            │
            ▼
┌────────────────────────────────────────┐
│  Inverse Terminal Mapping              │
│  i_node = B^T × f(u)                   │
│  ├─ i_node ∈ ℝ^N [node currents]      │
│  └─ Assemble into MNA equations        │
└────────────────────────────────────────┘
```

### Frequency-Domain Evaluation Path (Future)

```
┌──────────────────────────────────────────┐
│  Frequency-Domain Voltage Solution       │
│  V_freq = [V_0, V_1, V_2, ...]           │
│  Packing: [V_DC, Re(V₁), Im(V₁), ...]   │
│  (with conjugate symmetry: V₋ₖ = V*ₖ)   │
└────────────────┬─────────────────────────┘
                 │ (via C++ JNI)
                 ▼
        ┌─────────────────────┐
        │ frequencyToTime()   │
        │ Inverse DFT         │
        │ (via C++)           │
        └────────────┬────────┘
                     │
                     ▼
        ┌──────────────────────────────┐
        │ Time-Domain Samples          │
        │ v(t_m) ∀ m ∈ [0, M-1]       │
        │ (one period, Nyquist sampled)│
        └────────────┬─────────────────┘
                     │
                     ▼
        ┌──────────────────────────────┐
        │ Evaluate Nonlinear Device    │
        │ Loop over time samples:      │
        │  for m = 0 to M-1:           │
        │   i(t_m) = f(v(t_m))         │
        └────────────┬─────────────────┘
                     │
                     ▼
        ┌─────────────────────┐
        │ timeToFrequency()   │
        │ Forward DFT         │
        │ (via C++)           │
        └────────────┬────────┘
                     │
                     ▼
        ┌──────────────────────────────┐
        │ Frequency-Domain Currents    │
        │ I_freq = [I_0, I_1, I_2, ...]│
        │ (packed with symmetry)       │
        └──────────────────────────────┘
```

---

## Component Lifecycle: BJT Transistor

### Creation Phase

```
1. User drags "BJT Transistor" from panel
   ↓
2. MainController.circuitCanvas.onDragDropped()
   ├─ Detects drop location
   ├─ Factory creates: BJTTransistor transistor = new BJTTransistor()
   ├─ Sets parent network: transistor.setParent(network)
   └─ Calls: transistor.build()
   
3. BJTTransistor.build()
   ├─ generateMultiTerminalNodes(3)
   │  ├─ terminals.clear()
   │  ├─ Creates 3 ComponentNodes (e, b, c)
   │  ├─ Registers with parent network
   │  └─ Assigns roles: terminal[0]=input, terminal[2]=output
   ├─ Assign local references:
   │  ├─ emitterNode = getTerminal(0)
   │  ├─ baseNode = getTerminal(1)
   │  └─ collectorNode = getTerminal(2)
   ├─ Create virtual edges (network representation)
   ├─ Initialize frequency parameters
   └─ Setup editable properties (alphaF, alphaR, I_ES, I_CS)

4. GUI updates
   ├─ Draw component symbol at canvas position
   ├─ Draw 3 terminal connection points
   ├─ Show properties panel with 6 properties
   └─ Enable property editing
```

### Interaction Phase

```
1. User connects wires to terminals
   ├─ Drag wire from emitter → voltage source node
   ├─ Drag wire from base → control signal
   └─ Drag wire from collector → load

2. User edits properties (property panel)
   ├─ Double-click alphaF field → edit
   ├─ Parser validates range [0.5, 0.9999]
   ├─ Update local value: this.alphaF = newValue
   └─ Automatically triggers revalidation

3. Simulation runs
   ├─ HarmonicBalanceSolver.solve()
   ├─ Calls network.evaluateNonlinearDevices()
   ├─ Gets transistor.getTerminalMapping()
   ├─ Calls transistor.evaluateTimeDomain(reducedVoltages)
   └─ Returns DeviceEvaluationResult (currents, Jacobians)
```

### Destruction Phase

```
1. User right-clicks component → Delete
   ↓
2. MainController removes from circuit
   ├─ Calls component.destroy()
   └─ Disconnects all wires
   
3. BJTTransistor.destroy()
   ├─ removeMultiTerminalNodes()
   │  ├─ Loop through terminals[]
   │  ├─ Remove from parent.getComponentNodes()
   │  └─ Clear terminal list
   ├─ Remove edges from network
   │  ├─ parent.removeEdge(collectorEdge)
   │  ├─ parent.removeEdge(baseEdge)
   │  └─ parent.removeEdge(emitterEdge)
   └─ Cleanup references

4. Memory freed
   └─ Transistor object eligible for garbage collection
```

---

## Terminal Mapping Mathematics

### BJT Terminal Mapping

**Reduced voltage space** (2D):
```
u = [v_be, v_bc]^T  where v_be = v_B - v_E, v_bc = v_B - v_C
```

**Mapping matrix** B ∈ ℝ^(2×N):
```
       n_E  n_B  n_C  ... n_N
B[0] = [-1   1    0   ...  0 ]  → u[0] = v_B - v_E
B[1] = [ 0   1   -1   ...  0 ]  → u[1] = v_B - v_C
```

**Example**: If emitter connects to node 2, base to node 3, collector to node 5:
```
V_global = [v_1, v_2, v_3, v_4, v_5, ...]

u = B × V =
    [-1  0   1   0   0  ...]     v_1        [v_3 - v_2]
    [ 0  0   1  0  -1  ...] ×  v_2   =   [v_3 - v_5]
                                v_3
                                v_4
                                v_5
```

**Inverse transformation** (for assembling node currents):
```
i_node = B^T × f(u)  where f(u) = [i_C, i_B, i_E]^T

i_node ∈ ℝ^N  (only 3 non-zero entries: at nodes 2, 3, 5)
```

---

## Harmonic Balance Solution Vector

### Packing Scheme

**Real-signal conjugate symmetry**:
```
V_{-k} = conj(V_k)  ⟹  only need V_0, V_1, ..., V_{N/2}
```

**Packed solution** (for M sampling points, K active harmonics):
```
x = [V_DC, Re(V₁), Im(V₁), Re(V₂), Im(V₂), ..., Re(V_K), Im(V_K)]

Dimension: 1 + 2K  (DC is real-only, each harmonic uses 2 values)
Constraint: Real signal → conjugate symmetry enforced
```

### Frequency-Domain Assembly

**Admittance matrix** Y(ω):
```
Y_ij(ω) = (Conductance stamps) + (Frequency-dependent L/C terms)

For frequency ω_k:
• Resistor (1/R stays same) → linear stamp
• Capacitor (jkω₀C) → frequency-dependent entry
• Inductor (1/(jkω₀L)) → frequency-dependent entry
• BJT (nonlinear) → linearized Jacobian at solution point
```

**Newton residual**:
```
r = (MNA equations) - (Device currents)
  = (Linear part) + (Nonlinear part from BJT)
```

---

## Safety Features

### 1. Numerically Stable Exponential

```cpp
double safe_exp(double x) {
    // Prevents overflow/underflow in exp() computation
    if (x > 100.0) return 1e43;      // exp(100) ≈ 2.7e43
    if (x < -100.0) return 0.0;      // exp(-100) ≈ 0
    return exp(x);                   // Use standard exp
}

// Why needed:
// BJT current: i = I_S * (exp(v/V_T) - 1)
// V_T ≈ 26 mV, so v=10V → exponent ≈ 385
// exp(385) overflows to infinity → NaN in Jacobian
```

### 2. Parameter Validation

```java
private void validateParameters() {
    if (alphaF < 0.5 || alphaF > 0.9999)
        throw new IllegalArgumentException("alphaF out of range");
    if (alphaR < 0 || alphaR > 0.5)
        throw new IllegalArgumentException("alphaR out of range");
    if (I_ES <= 0 || I_CS <= 0)
        throw new IllegalArgumentException("I_ES, I_CS must be > 0");
    
    // Consistency check
    double expectedI_CS = (1.0 - alphaR) / alphaR * alphaF * I_ES;
    double tolerance = 0.5;  // Allow ±50%
    if (Math.abs(I_CS - expectedI_CS) > tolerance * expectedI_CS)
        System.out.println("Warning: I_CS inconsistent with alpha parameters");
}
```

### 3. Current Conservation

```
Constraint (always holds by model design):
i_C + i_B + i_E = 0

Verification in tests:
double sum = Math.abs(i_C + i_B + i_E);
assertTrue(sum < 1e-12);  // Numerical precision
```

---

## Extension Points: Adding New Devices

### To Add MOSFET (4-terminal, future)

```java
public class MOSFETTransistor extends Component implements NonlinearDevice {
    private ComponentNode sourceNode;
    private ComponentNode gateNode;
    private ComponentNode bodyNode;
    private ComponentNode drainNode;
    
    @Override
    public void build() {
        generateMultiTerminalNodes(4);
        sourceNode = getTerminal(0);
        gateNode = getTerminal(1);
        bodyNode = getTerminal(2);
        drainNode = getTerminal(3);
        // ... initialize model parameters
    }
    
    @Override
    public TerminalMapping getTerminalMapping() {
        return new MOSFETTerminalMapping(this);
    }
    
    @Override
    public DeviceEvaluationResult evaluateTimeDomain(...) {
        // MOSFET model: Shichman-Hodges or more advanced
    }
}
```

### To Add Diode (2-terminal, simpler)

```java
public class DiodeComponent extends Component implements NonlinearDevice {
    // Uses generateEndNodes() like existing 2-terminal (backward compat)
    
    @Override
    public DeviceEvaluationResult evaluateTimeDomain(...) {
        double v = getInput().getPos() - getOutput().getPos();
        double i = I_S * (exp(v/V_T) - 1);
        // Return currents for MNA assembly
    }
}
```

---

## Performance Characteristics

| Operation | Time | Notes |
|-----------|------|-------|
| Compile Java | ~2.6s | 41 files including new classes |
| Compile C++ | ~0.5s | CMake + MSBuild |
| Run BJT tests | ~0.11s | 10 tests pass |
| Total test suite | ~0.5s | 36 tests pass |
| Harmonic balance iteration (future) | ~100-1000ms | Depends on circuit size, spectrum size |

---

## Error Handling Strategy

### Java Layer

- **Parameter validation**: Exceptions on invalid inputs
- **Null checks**: ComponentNode not null before accessing
- **Bounds checking**: Terminal index within [0, poleCount)

### C++ Layer

- **Overflow protection**: safe_exp() clamps exponentials
- **Array bounds**: JNI bounds checking on array access
- **Memory safety**: RAII (smart pointers in future)

### Solver Layer

- **Convergence monitoring**: Residual norm tracking
- **Iteration limit**: Max iterations to prevent infinite loops
- **Damping control**: Adaptive step size to avoid divergence

---

## See Also

- [Detailed Implementation Status](IMPLEMENTATION_STATUS.md)
- [Quick Reference Guide](QUICK_REFERENCE.md)
- [Multi-Terminal Components Guide](MULTITERMINAL_COMPONENTS.md)
- [Source: Component.java](../src/main/java/network/Component.java)
- [Source: BJTTransistor.java](../src/main/java/network/BJTTransistor.java)
