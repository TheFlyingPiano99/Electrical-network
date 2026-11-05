package test.java.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import network.Network;
import network.Resistance;

public class NetworkTest {

	@Test
	public void testAddAndRemoveComponent() {
		Network network = new Network();
		Resistance resistance = new Resistance(10);

		network.addComponent(resistance);
		assertEquals(1, network.getComponents().size());
		assertEquals(2, network.getComponentNodes().size());
		assertEquals(10, resistance.getTimeDomainResistance(), 0.0);

		network.removeComponent(resistance);
		assertTrue(network.getComponents().isEmpty());
		assertTrue(network.getComponentNodes().isEmpty());
	}

	@Test
	public void testRequestAndReleaseAngularFrequency() {
		Network network = new Network();

		int frequencyIndex = network.requestAngularFrequency(50);
		assertTrue(network.getSimulatedAngularFrequencies().contains(50.0));
		assertEquals(frequencyIndex, network.getAngularFrequencyIndex(50));

		network.releaseAngularFrequency(50);
		assertTrue(!network.getSimulatedAngularFrequencies().contains(50.0));
	}
}
