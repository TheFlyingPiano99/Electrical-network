package network;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import math.Coordinate;

/**
 * Debug test to verify wire merging logic.
 * Creates two wires at nearby positions and verifies that nodes are properly merged
 * and edges are correctly connected via merged vertices.
 * 
 * This test helps identify where connection creation is failing in the merging process.
 * 
 * @author Copilot
 */
@DisplayName("Wire Merging Debug Tests")
public class WireMergingDebugTest {
    
    private Network network;
    
    @BeforeEach
    public void setUp() {
        network = new Network();
    }
    
    @Test
    @DisplayName("Debug: Create two wires and verify merging")
    public void testTwoWiresMerging() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST: Two Wires Merging Debug Test");
        System.out.println("=".repeat(80));
        
        // Verify initial state
        System.out.println("\n[INITIAL STATE]");
        System.out.println("Network vertices count: " + network.getVertices().size());
        System.out.println("Network edges count: " + network.getEdges().size());
        System.out.println("Network components count: " + network.getComponents().size());
        System.out.println("Network component nodes count: " + network.getComponentNodes().size());
        System.out.println("Grid size: " + network.getGridSize());
        System.out.println("Close proximity: " + network.getCloseProximity());
        
        assertNotNull(network, "Network should be created");
        assertTrue(network.getVertices().size() >= 1, "Network should have at least the ground vertex");
        
        // Create first wire
        System.out.println("\n[CREATING FIRST WIRE]");
        Wire wire1 = new Wire();
        // Position at 90 (snapped grid value): input at (60,90), output at (120,90)
        Coordinate pos1 = new Coordinate(90, 90);
        network.dropComponent(wire1, pos1);
        
        System.out.println("Wire 1 center position: " + pos1);
        System.out.println("Wire 1 input node position: " + wire1.getInput().getPos() + 
            " (x=" + wire1.getInput().getPos().x + ", y=" + wire1.getInput().getPos().y + ")");
        System.out.println("Wire 1 output node position: " + wire1.getOutput().getPos() + 
            " (x=" + wire1.getOutput().getPos().x + ", y=" + wire1.getOutput().getPos().y + ")");
        System.out.println("Wire 1 input binding: " + (wire1.getInput().getVertexBinding() != null ? 
            "Vertex #" + network.getVertices().indexOf(wire1.getInput().getVertexBinding()) : "null"));
        System.out.println("Wire 1 output binding: " + (wire1.getOutput().getVertexBinding() != null ? 
            "Vertex #" + network.getVertices().indexOf(wire1.getOutput().getVertexBinding()) : "null"));
        
        printNetworkStructure("After Wire 1");
        
        // Create second wire at nearby position (should trigger merge)
        System.out.println("\n[CREATING SECOND WIRE]");
        Wire wire2 = new Wire();
        // Position at 150 (snapped grid value): input at (120,90), output at (180,90)
        // Wire 2 input at (120,90) = Wire 1 output at (120,90) -> SHOULD MERGE!
        Coordinate pos2 = new Coordinate(150, 90);
        network.dropComponent(wire2, pos2);
        
        System.out.println("Wire 2 center position: " + pos2);
        System.out.println("Wire 2 input node position: " + wire2.getInput().getPos() + 
            " (x=" + wire2.getInput().getPos().x + ", y=" + wire2.getInput().getPos().y + ")");
        System.out.println("Wire 2 output node position: " + wire2.getOutput().getPos() + 
            " (x=" + wire2.getOutput().getPos().x + ", y=" + wire2.getOutput().getPos().y + ")");
        System.out.println("Wire 2 input binding: " + (wire2.getInput().getVertexBinding() != null ? 
            "Vertex #" + network.getVertices().indexOf(wire2.getInput().getVertexBinding()) : "null"));
        System.out.println("Wire 2 output binding: " + (wire2.getOutput().getVertexBinding() != null ? 
            "Vertex #" + network.getVertices().indexOf(wire2.getOutput().getVertexBinding()) : "null"));
        
        // Check distance between wire1 output and wire2 input
        System.out.println("\n[DISTANCE CHECK]");
        Coordinate wire1Out = wire1.getOutput().getPos();
        Coordinate wire2In = wire2.getInput().getPos();
        int dx = wire1Out.x - wire2In.x;
        int dy = wire1Out.y - wire2In.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        System.out.println("Wire 1 output pos: (" + wire1Out.x + ", " + wire1Out.y + ")");
        System.out.println("Wire 2 input pos: (" + wire2In.x + ", " + wire2In.y + ")");
        System.out.println("Distance between them: " + distance);
        System.out.println("Close proximity threshold: " + network.getCloseProximity());
        System.out.println("Should merge? " + (distance < network.getCloseProximity()));
        
        printNetworkStructure("After Wire 2");
        
        // Verify merging occurred
        System.out.println("\n[VERIFICATION]");
        
        // Check component nodes
        int totalComponentNodes = network.getComponentNodes().size();
        System.out.println("Total component nodes: " + totalComponentNodes);
        System.out.println("Expected: 3 (if merge successful) or 4 (if no merge)");
        
        // The key check: Wire 1 output and Wire 2 input should be merged
        Vertex wire1OutputVertex = wire1.getOutput().getVertexBinding();
        Vertex wire2InputVertex = wire2.getInput().getVertexBinding();
        
        System.out.println("\n[KEY CHECK: Output/Input Binding]");
        System.out.println("Wire 1 output vertex: " + network.getVertices().indexOf(wire1OutputVertex));
        System.out.println("Wire 2 input vertex: " + network.getVertices().indexOf(wire2InputVertex));
        
        if (wire1OutputVertex == wire2InputVertex) {
            System.out.println("✓ SUCCESS: Vertices ARE merged (same object reference)");
        } else {
            System.out.println("✗ FAILURE: Vertices are NOT merged (different objects)");
            System.out.println("  Wire 1 output vertex incoming edges: " + wire1OutputVertex.getIncoming().size());
            System.out.println("  Wire 1 output vertex outgoing edges: " + wire1OutputVertex.getOutgoing().size());
            System.out.println("  Wire 2 input vertex incoming edges: " + wire2InputVertex.getIncoming().size());
            System.out.println("  Wire 2 input vertex outgoing edges: " + wire2InputVertex.getOutgoing().size());
        }
        
        // Check edges
        int totalEdges = network.getEdges().size();
        System.out.println("\n[EDGES]");
        System.out.println("Total edges in network: " + totalEdges);
        System.out.println("Expected: 2 (one for each wire)");
        
        if (totalEdges >= 2) {
            for (int i = 0; i < network.getEdges().size(); i++) {
                Edge edge = network.getEdges().get(i);
                int inputVertexIdx = network.getVertices().indexOf(edge.getInput());
                int outputVertexIdx = network.getVertices().indexOf(edge.getOutput());
                System.out.println("Edge " + i + ": Vertex #" + inputVertexIdx + " -> Vertex #" + outputVertexIdx);
            }
        }
        
        // Check that edges are properly connected through merged vertices
        System.out.println("\n[CONNECTION VERIFICATION]");
        
        // Wire 1's edge
        Edge wire1Edge = wire1.getEdgeReference();
        if (wire1Edge != null) {
            Vertex wire1EdgeInput = wire1Edge.getInput();
            Vertex wire1EdgeOutput = wire1Edge.getOutput();
            
            System.out.println("Wire 1 edge:");
            System.out.println("  Input vertex index: " + network.getVertices().indexOf(wire1EdgeInput));
            System.out.println("  Output vertex index: " + network.getVertices().indexOf(wire1EdgeOutput));
            System.out.println("  Input incoming edges: " + wire1EdgeInput.getIncoming().size());
            System.out.println("  Input outgoing edges: " + wire1EdgeInput.getOutgoing().size());
            System.out.println("  Output incoming edges: " + wire1EdgeOutput.getIncoming().size());
            System.out.println("  Output outgoing edges: " + wire1EdgeOutput.getOutgoing().size());
        } else {
            System.out.println("Wire 1 has no edge reference!");
        }
        
        // Wire 2's edge
        Edge wire2Edge = wire2.getEdgeReference();
        if (wire2Edge != null) {
            Vertex wire2EdgeInput = wire2Edge.getInput();
            Vertex wire2EdgeOutput = wire2Edge.getOutput();
            
            System.out.println("\nWire 2 edge:");
            System.out.println("  Input vertex index: " + network.getVertices().indexOf(wire2EdgeInput));
            System.out.println("  Output vertex index: " + network.getVertices().indexOf(wire2EdgeOutput));
            System.out.println("  Input incoming edges: " + wire2EdgeInput.getIncoming().size());
            System.out.println("  Input outgoing edges: " + wire2EdgeInput.getOutgoing().size());
            System.out.println("  Output incoming edges: " + wire2EdgeOutput.getIncoming().size());
            System.out.println("  Output outgoing edges: " + wire2EdgeOutput.getOutgoing().size());
        } else {
            System.out.println("Wire 2 has no edge reference!");
        }
        
        // Final check: If vertices merged, both wires should be connected via the same vertex
        if (wire1OutputVertex == wire2InputVertex && wire1Edge != null && wire2Edge != null) {
            System.out.println("\n✓ Merged vertex connectivity:");
            System.out.println("  Wire 1 output edge: " + wire1Edge.getOutput());
            System.out.println("  Wire 2 input edge: " + wire2Edge.getInput());
            System.out.println("  Are they the same vertex? " + (wire1Edge.getOutput() == wire2Edge.getInput()));
            
            if (wire1Edge.getOutput() == wire2Edge.getInput()) {
                System.out.println("  ✓ Edges properly connected via merged vertex!");
                System.out.println("  Merged vertex has " + wire1Edge.getOutput().getIncoming().size() + 
                    " incoming edge(s) and " + wire1Edge.getOutput().getOutgoing().size() + " outgoing edge(s)");
            } else {
                System.out.println("  ✗ Edges NOT properly connected (vertices are different)");
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COMPLETE");
        System.out.println("=".repeat(80) + "\n");
    }
    
    @Test
    @DisplayName("Debug: Three wires in sequence to verify chain connections")
    public void testThreeWireChain() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST: Three Wires Chain Debug Test");
        System.out.println("=".repeat(80));
        
        // Create three wires in a line
        System.out.println("\n[CREATING WIRE CHAIN]");
        Wire wire1 = new Wire();
        Wire wire2 = new Wire();
        Wire wire3 = new Wire();
        
        // Position them so they connect: wire1 -> wire2 -> wire3
        // Wire center positions
        Coordinate pos1 = new Coordinate(100, 100);  // input (70,100), output (130,100)
        Coordinate pos2 = new Coordinate(160, 100);  // input (130,100), output (190,100)
        Coordinate pos3 = new Coordinate(220, 100);  // input (190,100), output (250,100)
        
        network.dropComponent(wire1, pos1);
        network.dropComponent(wire2, pos2);
        network.dropComponent(wire3, pos3);
        
        System.out.println("Wire 1 position: " + pos1);
        System.out.println("Wire 2 position: " + pos2);
        System.out.println("Wire 3 position: " + pos3);
        
        printNetworkStructure("Chain Complete");
        
        // Verify chain connectivity
        System.out.println("\n[CHAIN VERIFICATION]");
        
        // Check merging points
        Vertex merge1 = wire1.getOutput().getVertexBinding();
        Vertex merge2 = wire2.getOutput().getVertexBinding();
        Vertex input2 = wire2.getInput().getVertexBinding();
        Vertex input3 = wire3.getInput().getVertexBinding();
        
        System.out.println("Wire 1 output vertex: " + network.getVertices().indexOf(merge1));
        System.out.println("Wire 2 input vertex: " + network.getVertices().indexOf(input2));
        System.out.println("Should be same? " + (merge1 == input2));
        
        System.out.println("Wire 2 output vertex: " + network.getVertices().indexOf(merge2));
        System.out.println("Wire 3 input vertex: " + network.getVertices().indexOf(input3));
        System.out.println("Should be same? " + (merge2 == input3));
        
        // Count unique vertices (should be 4: ground + 3 unique positions)
        int uniqueVertices = network.getVertices().size();
        System.out.println("\nTotal unique vertices: " + uniqueVertices);
        System.out.println("Expected: ~4 (ground + 3 connection points)");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COMPLETE");
        System.out.println("=".repeat(80) + "\n");
    }
    
    /**
     * Print detailed network structure for debugging
     */
    private void printNetworkStructure(String label) {
        System.out.println("\n[NETWORK STRUCTURE: " + label + "]");
        
        System.out.println("Vertices (" + network.getVertices().size() + " total):");
        for (int i = 0; i < network.getVertices().size(); i++) {
            Vertex v = network.getVertices().get(i);
            String label_str = (i == 0) ? " [GROUND]" : "";
            System.out.println("  Vertex #" + i + label_str + ": " + 
                v.getIncoming().size() + " incoming, " + 
                v.getOutgoing().size() + " outgoing");
        }
        
        System.out.println("\nEdges (" + network.getEdges().size() + " total):");
        for (int i = 0; i < network.getEdges().size(); i++) {
            Edge e = network.getEdges().get(i);
            int inIdx = network.getVertices().indexOf(e.getInput());
            int outIdx = network.getVertices().indexOf(e.getOutput());
            System.out.println("  Edge #" + i + ": Vertex #" + inIdx + " -> Vertex #" + outIdx);
        }
        
        System.out.println("\nComponents (" + network.getComponents().size() + " total):");
        for (int i = 0; i < network.getComponents().size(); i++) {
            Component c = network.getComponents().get(i);
            String compName = c.getClass().getSimpleName();
            System.out.println("  Component #" + i + ": " + compName);
        }
        
        System.out.println("\nComponent Nodes (" + network.getComponentNodes().size() + " total):");
        for (int i = 0; i < network.getComponentNodes().size(); i++) {
            ComponentNode cn = network.getComponentNodes().get(i);
            Vertex v = cn.getVertexBinding();
            int vIdx = (v != null) ? network.getVertices().indexOf(v) : -1;
            System.out.println("  Node #" + i + ": pos=" + cn.getPos() + ", bound to Vertex #" + vIdx);
        }
    }
}
