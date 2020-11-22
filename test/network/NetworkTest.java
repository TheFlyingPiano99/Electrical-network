/**
 * 
 */
package network;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import math.Coordinate;
import math.Gauss;
import math.Matrix;
import math.Vector;

/**
 * @author simon
 *
 */
public class NetworkTest {

	private	Network network;

	Component v;
	Component w;
	Component r;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUpBefore() throws Exception {
		network = new Network();
		
	}

	public void buildSympleNetwork() {
		Component v = new VoltageSource(10.0F);
		Component w = new Wire();
		Component r = new Resistance(10);
			
		network.addComponent(v);
		network.addComponent(w);
		network.addComponent(r);
		
		network.grabNode(v.getInput());
		network.moveNode(v.getInput(), new Coordinate(10,10));
		network.releaseNode(v.getInput());

		network.grabNode(v.getOutput());
		network.moveNode(v.getOutput(), new Coordinate(30,10));
		network.releaseNode(v.getOutput());

		network.grabNode(w.getInput());
		network.moveNode(w.getInput(), new Coordinate(30,10));
		network.releaseNode(w.getInput());
		
		network.grabNode(w.getOutput());
		network.moveNode(w.getOutput(), new Coordinate(30,30));
		network.releaseNode(w.getOutput());

		network.grabNode(r.getInput());
		network.moveNode(r.getInput(), new Coordinate(30,30));
		network.releaseNode(r.getInput());

		network.grabNode(r.getOutput());
		network.moveNode(r.getOutput(), new Coordinate(10,10));
		network.releaseNode(r.getOutput());

	}
	
	/**
	 * Test method for {@link network.Network#gatherSourceVoltages()}.
	 */
	@Test
	public void testGatherSourceVoltages() {
		
		network.addComponent(new VoltageSource(10.0F));
		network.addComponent(new VoltageSource(20.0F));
		
		Vector exp = new Vector(2);
		exp.setAt(0, 10);
		exp.setAt(1, 20);
		
		assertEquals(exp, network.gatherSourceVoltages());
		
	}

	/**
	 * Test method for {@link network.Network#CalculateCurrent()}.
	 */
	@Test
	public void testCalculateCurrent() {
		Matrix incidence = new Matrix(3,3);
		incidence.fill(0);

		incidence.setAt(0, 0, -1.0F);
		incidence.setAt(0, 2, 1.0F);
		
		incidence.setAt(1, 0, 1.0F);
		incidence.setAt(1, 1, -1.0F);

		incidence.setAt(2, 1, 1.0F);
		incidence.setAt(2, 2, -1.0F);

		Matrix cycle = new Matrix(3,1);
		cycle.fill(1);

		Vector resistance = new Vector(3);
		resistance.setAt(0, 20);
		resistance.setAt(1, 10);
		resistance.setAt(2, 10);
		Vector sourceVoltage = new Vector(3);
		sourceVoltage.setAt(0, 10);
		sourceVoltage.setAt(1, 0);
		sourceVoltage.setAt(2, 0);
		
		for (int r = 0; r < incidence.row; r++) {
			for (int c = 0; c < incidence.column; c++) {
				System.out.print(incidence.at(r, c) + ", ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");

		for (int r = 0; r < cycle.row; r++) {
			for (int c = 0; c < cycle.column; c++) {
				System.out.print(cycle.at(r, c) + ", ");
			}
			System.out.print("\n");
		}

		LinearSystemForCurrent system = new LinearSystemForCurrent(incidence, cycle,
				resistance, sourceVoltage);
		System.out.print("\n");

		for (int r = 0; r < system.row; r++) {
			for (int c = 0; c < system.column; c++) {
				System.out.print(system.at(r, c) + ", ");
			}
			System.out.print("\n");
		}
		Vector current = Gauss.Eliminate(system);
		
		System.out.println(current.at(0) + ", " + current.at(1) + ", " + current.at(2));
		
	}

	/**
	 * Test method for {@link network.Network#simulate()}.
	 */
	@Test
	public void testSimulate() {
		buildSympleNetwork();
		
		network.simulate();
		
		assertTrue(1 == v.getCurrent());
		assertTrue(1 == r.getCurrent());
		
	}

	/**
	 * Test method for {@link network.Network#DFS(math.Matrix, math.Matrix)}.
	 */
	@Test
	public void testDFS() {
		buildSympleNetwork();
		
		Matrix incidence = new Matrix(0,0);
		Matrix cycle = new Matrix(0,0);
		
		network.DFS(incidence, cycle);
		
		for (int r = 0; r < incidence.row; r++) {
			for (int c = 0; c < incidence.column; c++) {
				System.out.print(incidence.at(r, c) + ", ");
			}
			System.out.print("\n");
		}
		System.out.print("\n");

		for (int r = 0; r < cycle.row; r++) {
			for (int c = 0; c < cycle.column; c++) {
				System.out.print(cycle.at(r, c) + ", ");
			}
			System.out.print("\n");
		}
		
		Matrix exp = new Matrix(3,3);
		exp.fill(0);

		exp.setAt(0, 0, -1.0F);
		exp.setAt(0, 2, 1.0F);
		
		exp.setAt(1, 0, 1.0F);
		exp.setAt(1, 1, -1.0F);

		exp.setAt(2, 1, 1.0F);
		exp.setAt(2, 2, -1.0F);

		assertEquals(exp, incidence);

		exp = new Matrix(3, 1);
		exp.fill(1);
		assertEquals(exp, incidence);
		
	}

	/**
	 * Test method for {@link network.Network#addComponent(network.Component)}.
	 */
	@Test
	public void testAddComponent() {
		Component added = new Wire();
		network.addComponent(added);
		
		assertTrue(1 == network.getComponents().size());
		assertEquals(added, network.getComponents().get(0));
		
		assertTrue(2 == network.getNodes().size());
		
	}

	/**
	 * Test method for {@link network.Network#removeComponent(network.Component)}.
	 */
	@Test
	public void testRemoveComponent() {
		Component added = new Wire();
		network.addComponent(added);
		
		assertEquals(added, network.getComponents().get(0));
		
		network.removeComponent(added);
		
		assertFalse(network.getComponents().contains(added));
		assertTrue(network.getNodes().isEmpty());
	}

	/**
	 * Test method for {@link network.Network#grabNode(network.Node)}.
	 */
	@Test
	public void testGrabNode() {
		Component comp = new Resistance();
		network.addComponent(comp);
		Node node = network.getNodes().get(0);
		network.grabNode(node);
		
		assertTrue(node.isMerge());
		assertTrue(node.isGrabbed());		
	}

	/**
	 * Test method for {@link network.Network#moveNode(network.Node, math.Coordinate)}.
	 */
	@Test
	public void testMoveNode() {
		Component comp = new Resistance();
		network.addComponent(comp);
		Node node = network.getNodes().get(0);
		network.grabNode(node);
		Coordinate pos = new Coordinate(15, 11);
		
		network.moveNode(node, pos);
		
		assertTrue(node.isMerge());
		assertTrue(node.isGrabbed());
		assertEquals(pos, node.getPos());
	}
	
	@Test
	public void testbuildSympleNetwork() {
		buildSympleNetwork();
		ArrayList<Component> components = network.getComponents();
		assertEquals(components.get(0).getInput(), components.get(2).getOutput());
		assertEquals(components.get(1).getInput(), components.get(0).getOutput());
		assertEquals(components.get(2).getInput(), components.get(1).getOutput());
		
		assertTrue(3 == network.getComponents().size());
		assertTrue(3 == network.getNodes().size());
		
	}
	
	/**
	 * Test method for {@link network.Network#releaseNode(network.Node)}.
	 */
	@Test
	public void testReleaseNode() {
		Component comp = new Resistance();
		network.addComponent(comp);
		Node node = network.getNodes().get(0);
		network.grabNode(node);
		Coordinate pos = new Coordinate(15, 11);
		
		network.moveNode(node, pos);
		
		network.releaseNode(node);
		
		assertTrue(node.isMerge());
		assertFalse(node.isGrabbed());	//!
		assertEquals(pos, node.getPos());
	}

	/**
	 * Test method for {@link network.Network#grabComponent(network.Component)}.
	 */
	@Test
	public void testGrabComponent() {
		Component comp = new Resistance();
		network.addComponent(comp);
		
		network.grabComponent(comp);
		
		assertTrue(comp.isGrabbed());
		assertTrue(comp.getInput().isMerge());
		assertTrue(comp.getOutput().isMerge());
		
	}

	/**
	 * Test method for {@link network.Network#moveComponent(network.Component, math.Coordinate)}.
	 */
	@Test
	public void testMoveComponent() {
		Component comp = new Resistance();
		network.addComponent(comp);
		
		network.grabComponent(comp);
		Coordinate pos = new Coordinate(16, 12);
		network.moveComponent(comp, pos);
		
		assertEquals(pos, comp.getInput().getPos());
		assertEquals(pos, comp.getOutput().getPos());
		
	}

	/**
	 * Test method for {@link network.Network#releaseComponent(network.Component)}.
	 */
	@Test
	public void testReleaseComponent() {
		Component comp = new Resistance();
		network.addComponent(comp);
		
		network.grabComponent(comp);
		Coordinate pos = new Coordinate(16, 12);
		network.moveComponent(comp, pos);
		network.releaseComponent(comp);
		
		assertFalse(comp.isGrabbed());
		assertTrue(comp.getInput().isMerge());
		assertTrue(comp.getOutput().isMerge());
	}

	/**
	 * Test method for {@link network.Network#tryToMergeNode(network.Node)}.
	 */
	@Test
	public void testTryToMergeNode() {
		ArrayList<Node> nodes = network.getNodes();
		Node n1 = new Node();
		Node n2 = new Node();
		Node n3 = new Node();
		nodes.add(n1);
		nodes.add(n2);
		nodes.add(n3);
		
		n1.setPos(new Coordinate(10, 10));
		n2.setPos(new Coordinate(0, 0));
		
		network.tryToMergeNode(n1);
		
		assertTrue(3 == network.getNodes().size());
		
		n2.setPos(new Coordinate(11, 8));
		
		network.tryToMergeNode(n1);
		
		assertTrue(2 == network.getNodes().size());
		
	}

}
