package network;

import math.Coordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that validates circuit setup and helps diagnose "Hibás kapcsolás" (wrong connection) issues.
 * 
 * The "Hibás kapcsolás" message appears when:
 * 1. Network has no components
 * 2. Network has no edges (components not connected)
 * 3. Network structure results in singular matrix (no valid solution)
 * 4. Components are not grounded
 */
public class ValidCircuitTest {
    
    @Test
    public void testSimpleValidCircuit() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST: Simple Valid Circuit (DC voltage + resistance + ground)");
        System.out.println("=".repeat(80));
        
        Network network = new Network();
        
        // Create a simple circuit: DC Voltage Source -> Resistor -> Ground
        // This should be solvable because there's a clear current path
        
        // 1. Add voltage source at position (150, 200)
        DCVoltageSource voltage = new DCVoltageSource();
        network.dropComponent(voltage, new Coordinate(150, 200));
        voltage.setSourceVoltage(10.0);  // 10V DC - set after build()
        System.out.println("\n✓ Added DC voltage source at (150, 200)");
        System.out.println("  Input node:  " + voltage.getInput().getPos());
        System.out.println("  Output node: " + voltage.getOutput().getPos());
        
        // 2. Add wire from voltage output to resistor input
        Wire wire1 = new Wire();
        network.dropComponent(wire1, new Coordinate(210, 200));
        System.out.println("\n✓ Added wire 1 at (210, 200)");
        System.out.println("  Input node:  " + wire1.getInput().getPos());
        System.out.println("  Output node: " + wire1.getOutput().getPos());
        
        // 3. Add resistor
        Resistance resistor = new Resistance();
        network.dropComponent(resistor, new Coordinate(270, 200));
        resistor.setResistance(100.0);  // 100Ω - set after build()
        System.out.println("\n✓ Added resistor at (270, 200)");
        System.out.println("  Input node:  " + resistor.getInput().getPos());
        System.out.println("  Output node: " + resistor.getOutput().getPos());
        
        // 4. Add wire to connect resistor to ground
        Wire wire2 = new Wire();
        network.dropComponent(wire2, new Coordinate(330, 200));
        System.out.println("\n✓ Added wire 2 at (330, 200)");
        System.out.println("  Input node:  " + wire2.getInput().getPos());
        System.out.println("  Output node: " + wire2.getOutput().getPos());
        
        // 5. Add ground
        Ground ground = new Ground();
        network.dropComponent(ground, new Coordinate(390, 200));
        System.out.println("\n✓ Added ground at (390, 200)");
        System.out.println("  Input node:  " + ground.getInput().getPos());
        System.out.println("  Output node: " + ground.getOutput().getPos());
        
        // Print network structure
        printNetworkStructure(network);
        
        // Evaluate the network
        System.out.println("\n[EVALUATING NETWORK]");
        network.evaluate(true);
        
        // Check validity
        boolean isValid = network.isValid();
        System.out.println("\n✓ Network is valid: " + isValid);
        
        if (isValid) {
            // Calculate expected current: I = V / R = 10V / 100Ω = 0.1A
            System.out.println("✓ Expected current (DC): 10V / 100Ω = 0.1A");
            
            // Get actual currents from edges
            java.util.ArrayList<Edge> edges = network.getEdges();
            System.out.println(String.format("✓ Network has %d edges (wires/components)", edges.size()));
            
            for (int i = 0; i < edges.size(); i++) {
                Edge edge = edges.get(i);
                double currentMagnitude = edge.getCurrent().at(0).getAbs();
                System.out.println(String.format("  Edge %d current magnitude: %.4f A", i, currentMagnitude));
            }
        } else {
            System.out.println("✗ Network is INVALID - this is the \"Hibás kapcsolás\" error!");
            System.out.println("  Possible causes:");
            System.out.println("    1. Components not properly connected");
            System.out.println("    2. Missing ground connection");
            System.out.println("    3. No valid current path");
            System.out.println("    4. Singular matrix (mathematical issue with circuit)");
        }
        
        assertTrue(isValid, "Network should be valid");
    }
    
    @Test
    public void testCircuitWithoutGround() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST: Circuit WITHOUT Ground (should be invalid)");
        System.out.println("=".repeat(80));
        
        Network network = new Network();
        
        // Add components but DON'T connect to ground
        DCVoltageSource voltage = new DCVoltageSource();
        network.dropComponent(voltage, new Coordinate(150, 200));
        voltage.setSourceVoltage(10.0);  // Set after build()
        
        Wire wire = new Wire();
        network.dropComponent(wire, new Coordinate(210, 200));
        
        Resistance resistor = new Resistance();
        network.dropComponent(resistor, new Coordinate(270, 200));
        resistor.setResistance(100.0);  // Set after build()
        
        printNetworkStructure(network);
        
        System.out.println("\n[EVALUATING NETWORK WITHOUT GROUND]");
        network.evaluate(true);
        
        boolean isValid = network.isValid();
        System.out.println("\n✓ Network is valid: " + isValid);
        System.out.println("  (Should be false because there's no ground reference)");
        
        assertFalse(isValid, "Network without ground should be invalid");
    }
    
    @Test
    public void testDisconnectedComponents() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST: Disconnected Components (should be invalid)");
        System.out.println("=".repeat(80));
        
        Network network = new Network();
        
        // Add first component
        DCVoltageSource voltage1 = new DCVoltageSource();
        network.dropComponent(voltage1, new Coordinate(150, 200));
        voltage1.setSourceVoltage(10.0);  // Set after build()
        
        // Add completely separate second component (too far away to connect)
        Resistance resistor = new Resistance();
        network.dropComponent(resistor, new Coordinate(500, 200));  // Far away
        resistor.setResistance(100.0);  // Set after build()
        
        System.out.println("\n✓ Added voltage source at (150, 200)");
        System.out.println("✓ Added resistor at (500, 200) - too far to connect!");
        
        printNetworkStructure(network);
        
        System.out.println("\n[EVALUATING DISCONNECTED NETWORK]");
        network.evaluate(true);
        
        boolean isValid = network.isValid();
        System.out.println("\n✓ Network is valid: " + isValid);
        System.out.println("  (Should be false because components are disconnected)");
        
        assertFalse(isValid, "Network with disconnected components should be invalid");
    }
    
    private void printNetworkStructure(Network network) {
        System.out.println("\n[NETWORK STRUCTURE]");
        System.out.println(String.format("  Components: %d", network.getComponents().size()));
        System.out.println(String.format("  Component Nodes: %d", network.getComponentNodes().size()));
        System.out.println(String.format("  Edges: %d", network.getEdges().size()));
        System.out.println(String.format("  Vertices: %d", network.getVertices().size()));
        
        System.out.println("\n  Components:");
        for (int i = 0; i < network.getComponents().size(); i++) {
            Component c = network.getComponents().get(i);
            System.out.println(String.format("    [%d] %s", i, c.getClass().getSimpleName()));
        }
        
        System.out.println("\n  Edges:");
        for (int i = 0; i < network.getEdges().size(); i++) {
            Edge e = network.getEdges().get(i);
            int inIdx = network.getVertices().indexOf(e.getInput());
            int outIdx = network.getVertices().indexOf(e.getOutput());
            System.out.println(String.format("    [%d] Vertex %d -> Vertex %d", i, inIdx, outIdx));
        }
    }
}
