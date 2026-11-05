package network;

import math.Coordinate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A closed loop without an explicit Ground component must still be solvable.
 */
public class FloatingCircuitTest {

    private Network buildLoop(boolean withGround) {
        Network network = new Network();

        SinusoidalVoltageSource source = new SinusoidalVoltageSource(10.0, 2.0 * Math.PI * 50);
        network.dropComponent(source, new Coordinate(210, 200));   // 180 -> 240

        Resistance resistor = new Resistance();
        network.dropComponent(resistor, new Coordinate(270, 200)); // 240 -> 300
        resistor.setResistance(100.0);

        // The wire starts where the resistor ends and is stretched back to the source input.
        Wire wire = new Wire();
        network.dropComponent(wire, new Coordinate(330, 200));     // 300 -> 360
        wire.getOutput().setPos(new Coordinate(180, 200));
        network.tryToMergeComponentNode(wire.getOutput());

        if (withGround) {
            network.dropComponent(new Ground(), new Coordinate(270, 260));
            // Attach the ground to the resistor input node.
            Ground ground = (Ground) network.getComponents().get(network.getComponents().size() - 1);
            ground.getInput().setPos(new Coordinate(240, 200));
            network.tryToMergeComponentNode(ground.getInput());
        }
        return network;
    }

    @Test
    public void floatingLoopIsValid() {
        Network network = buildLoop(false);
        assertEquals(3, network.getEdges().size());
        assertEquals(4, network.getVertices().size());
        network.evaluate(true);
        assertTrue(network.isValid(), "Floating loop should be valid");
    }

    @Test
    public void floatingLoopCurrentFollowsOhmsLaw() {
        Network network = buildLoop(false);
        network.evaluate(true);
        int k = network.getAngularFrequencyIndex(2.0 * Math.PI * 50);
        assertTrue(k >= 0);
        for (Edge edge : network.getEdges()) {
            assertEquals(0.1, edge.getCurrent().at(k).getAbs(), 1e-9);
        }
    }

    @Test
    public void groundedLoopIsStillValid() {
        Network network = buildLoop(true);
        network.evaluate(true);
        assertTrue(network.isValid(), "Grounded loop should be valid");
    }
}
