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
	Component k;
	Component l;
	Component m;
	Component n;
	Component o;
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUpBefore() throws Exception {
		network = new Network();
		
	}

	public void buildSympleNetwork() {
		v = new VoltageSource(10.0F);
		w = new Resistance(10);
		r = new Resistance(10);
		k = new Wire();
		
		network.addComponent(v);
		network.addComponent(w);
		network.addComponent(r);		
		network.addComponent(k);

		network.grabComponentNode(w.getInput());
		network.moveComponentNode(w.getInput(), new Coordinate(30,10));
		network.releaseComponentNode(w.getInput());
		
		network.grabComponentNode(w.getOutput());
		network.moveComponentNode(w.getOutput(), new Coordinate(30,30));
		network.releaseComponentNode(w.getOutput());
		
		network.grabComponentNode(r.getInput());
		network.moveComponentNode(r.getInput(), new Coordinate(30,30));
		network.releaseComponentNode(r.getInput());

		network.grabComponentNode(r.getOutput());
		network.moveComponentNode(r.getOutput(), new Coordinate(60,60));
		network.releaseComponentNode(r.getOutput());

		network.grabComponentNode(v.getInput());
		network.moveComponentNode(v.getInput(), new Coordinate(60,60));
		network.releaseComponentNode(v.getInput());

		network.grabComponentNode(v.getOutput());
		network.moveComponentNode(v.getOutput(), new Coordinate(80,90));
		network.releaseComponentNode(v.getOutput());

		network.grabComponentNode(k.getInput());
		network.moveComponentNode(k.getInput(), new Coordinate(80,90));
		network.releaseComponentNode(k.getInput());

		network.grabComponentNode(k.getOutput());
		network.moveComponentNode(k.getOutput(), new Coordinate(30,10));
		network.releaseComponentNode(k.getOutput());
	}
	
	/**
	 * 		----l-------
	 * 		|			|
	 * 		n			m
	 * 		|			|
	 * 		-----r------
	 * 		|			|
	 * 		k			w
	 * 		|			|
	 * 		-----|v|-----
	 */
	
	public void buildParalelNetwork() {
		v = new VoltageSource(10.0F);

		w = new Resistance(10.0F);				
		r = new Resistance(10.0F);				
		k = new Resistance(10.0F);
		l = new Resistance(10.0F);
		
		m = new Wire();
		n = new Wire();
		
		network.addComponent(v);
		network.addComponent(w);
		network.addComponent(r);		
		network.addComponent(k);
		network.addComponent(l);
		network.addComponent(m);
		network.addComponent(n);
		
		//v:
		network.grabComponentNode(v.getInput());
		network.moveComponentNode(v.getInput(), new Coordinate(30, 100));
		network.releaseComponentNode(v.getInput());
		
		network.grabComponentNode(v.getOutput());
		network.moveComponentNode(v.getOutput(), new Coordinate(60, 100));
		network.releaseComponentNode(v.getOutput());
		
		//w:
		network.grabComponentNode(w.getInput());
		network.moveComponentNode(w.getInput(), new Coordinate(60, 100));
		network.releaseComponentNode(w.getInput());
		
		network.grabComponentNode(w.getOutput());
		network.moveComponentNode(w.getOutput(), new Coordinate(60, 70));
		network.releaseComponentNode(w.getOutput());
		
		//r:
		network.grabComponentNode(r.getInput());
		network.moveComponentNode(r.getInput(), new Coordinate(60, 70));
		network.releaseComponentNode(r.getInput());
		
		network.grabComponentNode(r.getOutput());
		network.moveComponentNode(r.getOutput(), new Coordinate(30, 70));
		network.releaseComponentNode(r.getOutput());
		
		//k:
		network.grabComponentNode(k.getInput());
		network.moveComponentNode(k.getInput(), new Coordinate(30, 70));
		network.releaseComponentNode(k.getInput());
		
		network.grabComponentNode(k.getOutput());
		network.moveComponentNode(k.getOutput(), new Coordinate(30, 100));
		network.releaseComponentNode(k.getOutput());

		//l:
		network.grabComponentNode(l.getInput());
		network.moveComponentNode(l.getInput(), new Coordinate(60, 40));
		network.releaseComponentNode(l.getInput());
		
		network.grabComponentNode(l.getOutput());
		network.moveComponentNode(l.getOutput(), new Coordinate(30, 40));
		network.releaseComponentNode(l.getOutput());
		
		//m:
		network.grabComponentNode(m.getInput());
		network.moveComponentNode(m.getInput(), new Coordinate(60, 70));
		network.releaseComponentNode(m.getInput());
		
		network.grabComponentNode(m.getOutput());
		network.moveComponentNode(m.getOutput(), new Coordinate(60, 40));
		network.releaseComponentNode(m.getOutput());
		
		//n:
		network.grabComponentNode(n.getInput());
		network.moveComponentNode(n.getInput(), new Coordinate(30, 40));
		network.releaseComponentNode(n.getInput());
		
		network.grabComponentNode(n.getOutput());
		network.moveComponentNode(n.getOutput(), new Coordinate(30, 70));
		network.releaseComponentNode(n.getOutput());

	}
	
	/**
	 * Test method for {@link network.Network#gatherSourceVoltages()}.
	 */
	@Test
	public void testGatherSourceVoltages() {
		
		Edge e1 = new Edge();
		network.addEdge(e1);
		e1.setSourceVoltage(10);
		
		Edge e2 = new Edge();
		network.addEdge(e2);
		e2.setSourceVoltage(20);

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
	public void testSimulateOnSimpleNetwork() {
		buildSympleNetwork();
		
		network.simulate();
		
		System.out.println("Test Simple Simulate:");

		System.out.println("I[v] = " + v.getCurrent());
		System.out.println("I[r] = " + r.getCurrent());
		System.out.println("I[w] = " + w.getCurrent());
		System.out.println("I[k] = " + k.getCurrent());
		System.out.println("--------------------------");
		
		assertTrue(0.5F == v.getCurrent());
		assertTrue(0.5F == r.getCurrent());
		assertTrue(0.5F == w.getCurrent());
		assertTrue(0.5F == k.getCurrent());
		
	}

	/**
	 * Test method for {@link network.Network#simulate()}.
	 */
	@Test
	public void testSimulateOnParalelNetwork() {
		buildParalelNetwork();
		
		network.simulate();
		
		System.out.println("Test Paralel Simulate:");

		System.out.println("I[v] = " + v.getCurrent());
		System.out.println("I[r] = " + r.getCurrent());
		System.out.println("I[w] = " + w.getCurrent());
		System.out.println("I[k] = " + k.getCurrent());
		System.out.println("I[l] = " + l.getCurrent());
		System.out.println("I[m] = " + m.getCurrent());
		System.out.println("I[n] = " + n.getCurrent());
		System.out.println("--------------------------");
		
		assertTrue(0.5F == v.getCurrent());
		assertTrue(0.5F == r.getCurrent());
		assertTrue(0.5F == w.getCurrent());
		assertTrue(0.5F == k.getCurrent());
		assertTrue(0.5F == l.getCurrent());
		assertTrue(0.5F == m.getCurrent());
		assertTrue(0.5F == n.getCurrent());
		
	}
	
	/**
	 * Test method for {@link network.Network#DFS(math.Matrix, math.Matrix)}.
	 */
	@Test
	public void testDFS() {
		buildParalelNetwork();
		
		Matrix incidence = new Matrix(0,0);
		Matrix cycle = new Matrix(0,0);
		
		network.DFS(incidence, cycle);

		//Print:
		System.out.println("DFS test:");
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
		System.out.println("-----------------------------------");
		
		Matrix exp = new Matrix(4,3);
		exp.fill(0);

		
		exp.setAt(0, 0, 1.0F);
		exp.setAt(0, 1, -1.0F);

		exp.setAt(1, 1, 1.0F);
		exp.setAt(1, 2, -1.0F);

		exp.setAt(2, 0, -1.0F);
		exp.setAt(2, 2, 1.0F);

		exp.setAt(3, 0, -1.0F);
		exp.setAt(3, 2, 1.0F);

		assertEquals(exp, incidence);

		exp = new Matrix(4, 1);
		exp.fill(1.0F);
		assertEquals(exp, cycle);
		
		
		
	}

	/**
	 * Test method for {@link network.Network#addEdge(network.Edge)}.
	 */
	@Test
	public void testAddComponent() {
		Edge added = new Edge();
		network.addEdge(added);		
	}

	/**
	 * Test method for {@link network.Network#removeEdge(network.Edge)}.
	 */
	@Test
	public void testRemoveComponent() {
		Edge added = new Edge();
		network.addEdge(added);
		
		
		network.removeEdge(added);
		
	}
	
	@Test
	public void testBuildSympleNetwork() {
		buildSympleNetwork();
		assertTrue(4 == network.getComponents().size());
		assertTrue(4 == network.getComponentNodes().size());
		
	}

	@Test
	public void testBuildParalelNetwork() {
		buildParalelNetwork();
		assertTrue(7 == network.getComponents().size());
		assertTrue(6 == network.getComponentNodes().size());
		
	}
	
	/**
	 * Test method for {@link network.Network#tryToMergeNode(network.Node)}.
	 */
	@Test
	public void testTryToMergeNode() {
		Component c1 = new Resistance(10);
		Component c2 = new Resistance(10);
		
		network.addComponent(c1);
		network.addComponent(c2);

		assertTrue(4 == network.getComponentNodes().size());
		
		network.grabComponentNode(c1.getOutput());
		network.moveComponentNode(c1.getOutput(), new Coordinate(30,30));
		network.releaseComponentNode(c1.getOutput());
		
		network.grabComponentNode(c2.getOutput());
		network.moveComponentNode(c2.getOutput(), new Coordinate(28,32));
		network.releaseComponentNode(c2.getOutput());
		
		assertTrue(3 == network.getComponentNodes().size());		
	}
	
	@Test
	public void testRemoveComponents() {
		buildSympleNetwork();
		network.removeComponent(v);
		network.removeComponent(r);
		network.removeComponent(w);
		network.removeComponent(k);
		
		assertTrue(network.getComponents().isEmpty());
		assertTrue(network.getComponentNodes().isEmpty());
	}

}
