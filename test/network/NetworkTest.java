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

	Edge v;
	Edge w;
	Edge r;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUpBefore() throws Exception {
		network = new Network();
		
	}

	public void buildSympleNetwork() {
		v = new VoltageSource(20.0F, 0.0F, 10.0F);
		w = new Wire(10, 0, 0);
		r = new Resistance(10, 0, 0);
			
		network.addEdge(v);
		network.addEdge(w);
		network.addEdge(r);
		
		network.grabComponentNode(v.getInput());
		network.moveComponentNode(v.getInput(), new Coordinate(10,10));
		network.releaseNode(v.getInput());

		network.grabComponentNode(v.getOutput());
		network.moveComponentNode(v.getOutput(), new Coordinate(30,10));
		network.releaseNode(v.getOutput());

		network.grabComponentNode(w.getInput());
		network.moveComponentNode(w.getInput(), new Coordinate(30,10));
		network.releaseNode(w.getInput());
		
		network.grabComponentNode(w.getOutput());
		network.moveComponentNode(w.getOutput(), new Coordinate(30,30));
		network.releaseNode(w.getOutput());

		network.grabComponentNode(r.getInput());
		network.moveComponentNode(r.getInput(), new Coordinate(30,30));
		network.releaseNode(r.getInput());

		network.grabComponentNode(r.getOutput());
		network.moveComponentNode(r.getOutput(), new Coordinate(10,10));
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
		
		Vector expected = new Vector(3);
		expected.setAt(0, 0.25F);
		expected.setAt(1, 0.25F);
		expected.setAt(2, 0.25F);
		assertEquals(expected, current);
		
	}

	/**
	 * Test method for {@link network.Network#simulate()}.
	 */
	@Test
	public void testSimulate() {
		buildSympleNetwork();
		
		network.simulate();
		
		System.out.println(v.getCurrent());
		System.out.println(r.getCurrent());
		System.out.println(w.getCurrent());
		
		assertTrue(0 == v.getCurrent());
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

		//Print:
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

		
		exp.setAt(0, 0, 1.0F);
		exp.setAt(0, 1, -1.0F);

		exp.setAt(1, 1, 1.0F);
		exp.setAt(1, 2, -1.0F);

		assertEquals(exp, incidence);

		exp = new Matrix(3, 1);
		exp.fill(1.0F);
		assertEquals(exp, cycle);
		
		
		
	}

	/**
	 * Test method for {@link network.Network#addEdge(network.Edge)}.
	 */
	@Test
	public void testAddComponent() {
		Edge added = new Wire();
		network.addEdge(added);
		
		assertTrue(1 == network.getEdges().size());
		assertEquals(added, network.getEdges().get(0));
		
		assertTrue(2 == network.getNodes().size());
		
	}

	/**
	 * Test method for {@link network.Network#removeEdge(network.Edge)}.
	 */
	@Test
	public void testRemoveComponent() {
		Edge added = new Wire();
		network.addEdge(added);
		
		assertEquals(added, network.getEdges().get(0));
		
		network.removeEdge(added);
		
		assertFalse(network.getEdges().contains(added));
		assertTrue(network.getNodes().isEmpty());
	}

	/**
	 * Test method for {@link network.Network#grabComponentNode(network.Node)}.
	 */
	@Test
	public void testGrabNode() {
		Edge comp = new Resistance();
		network.addEdge(comp);
		Node node = network.getNodes().get(0);
		network.grabComponentNode(node);
		
		assertTrue(node.isMerge());
		assertTrue(node.isGrabbed());		
	}

	/**
	 * Test method for {@link network.Network#moveComponentNode(ComponentNode, math.Coordinate)}.
	 */
	@Test
	public void testMoveNode() {
		Edge comp = new Resistance();
		network.addEdge(comp);
		Node node = network.getNodes().get(0);
		network.grabComponentNode(node);
		Coordinate pos = new Coordinate(15, 11);
		
		network.moveComponentNode(node, pos);
		
		assertTrue(node.isMerge());
		assertTrue(node.isGrabbed());
		assertEquals(pos, node.getPos());
	}
	
	@Test
	public void testbuildSympleNetwork() {
		buildSympleNetwork();
		ArrayList<Edge> components = network.getEdges();
		assertEquals(components.get(0).getInput(), components.get(2).getOutput());
		assertEquals(components.get(1).getInput(), components.get(0).getOutput());
		assertEquals(components.get(2).getInput(), components.get(1).getOutput());
		
		assertTrue(3 == network.getEdges().size());
		assertTrue(3 == network.getNodes().size());
		
	}
	
	/**
	 * Test method for {@link network.Network#releaseNode(network.Node)}.
	 */
	@Test
	public void testReleaseNode() {
		Edge comp = new Resistance();
		network.addEdge(comp);
		Node node = network.getNodes().get(0);
		network.grabComponentNode(node);
		Coordinate pos = new Coordinate(15, 11);
		
		network.moveComponentNode(node, pos);
		
		network.releaseNode(node);
		
		assertTrue(node.isMerge());
		assertFalse(node.isGrabbed());	//!
		assertEquals(pos, node.getPos());
	}

	/**
	 * Test method for {@link network.Network#grabComponent(Component)}.
	 */
	@Test
	public void testGrabComponent() {
		Edge comp = new Resistance();
		network.addEdge(comp);
		
		network.grabComponent(comp);
		
		assertTrue(comp.isGrabbed());
		assertTrue(comp.getInput().isMerge());
		assertTrue(comp.getOutput().isMerge());
		
	}

	/**
	 * Test method for {@link network.Network#moveComponent(network.Edge, math.Coordinate)}.
	 */
	@Test
	public void testMoveComponent() {
		Edge comp = new Resistance();
		network.addEdge(comp);
		
		network.grabComponent(comp);
		Coordinate pos = new Coordinate(16, 12);
		network.moveComponent(comp, pos);
		
		assertEquals(pos, comp.getInput().getPos());
		assertEquals(pos, comp.getOutput().getPos());
		
	}

	/**
	 * Test method for {@link network.Network#releaseComponent(network.Edge)}.
	 */
	@Test
	public void testReleaseComponent() {
		Edge comp = new Resistance();
		network.addEdge(comp);
		
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
