/**
 * 
 */
package test.java.network;

import static org.junit.Assert.*;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

import main.java.math.Coordinate;
import main.java.math.Gauss;
import main.java.math.Matrix;
import main.java.math.Vector;
import main.java.network.Component;
import main.java.network.Edge;
import main.java.network.LinearSystemForCurrent;
import main.java.network.Network;
import main.java.network.Resistance;
import main.java.network.VoltageSource;
import main.java.network.Wire;

/**
 * @author Simon Zoltán
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

		network.grabComponentNode(w.getInput(), new Coordinate(0,0));
		network.dragComponentNode(w.getInput(), new Coordinate(30,10));
		network.releaseComponentNode(w.getInput());
		
		network.grabComponentNode(w.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(w.getOutput(), new Coordinate(30,30));
		network.releaseComponentNode(w.getOutput());
		
		network.grabComponentNode(r.getInput(), new Coordinate(0,0));
		network.dragComponentNode(r.getInput(), new Coordinate(30,30));
		network.releaseComponentNode(r.getInput());

		network.grabComponentNode(r.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(r.getOutput(), new Coordinate(60,60));
		network.releaseComponentNode(r.getOutput());

		network.grabComponentNode(v.getInput(), new Coordinate(0,0));
		network.dragComponentNode(v.getInput(), new Coordinate(60,60));
		network.releaseComponentNode(v.getInput());

		network.grabComponentNode(v.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(v.getOutput(), new Coordinate(80,90));
		network.releaseComponentNode(v.getOutput());

		network.grabComponentNode(k.getInput(), new Coordinate(0,0));
		network.dragComponentNode(k.getInput(), new Coordinate(80,90));
		network.releaseComponentNode(k.getInput());

		network.grabComponentNode(k.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(k.getOutput(), new Coordinate(30,10));
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
		network.grabComponentNode(v.getInput(), new Coordinate(0,0));
		network.dragComponentNode(v.getInput(), new Coordinate(30, 100));
		network.releaseComponentNode(v.getInput());
		
		network.grabComponentNode(v.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(v.getOutput(), new Coordinate(60, 100));
		network.releaseComponentNode(v.getOutput());
		
		//w:
		network.grabComponentNode(w.getInput(), new Coordinate(0,0));
		network.dragComponentNode(w.getInput(), new Coordinate(60, 100));
		network.releaseComponentNode(w.getInput());
		
		network.grabComponentNode(w.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(w.getOutput(), new Coordinate(60, 70));
		network.releaseComponentNode(w.getOutput());
		
		//r:
		network.grabComponentNode(r.getInput(), new Coordinate(0,0));
		network.dragComponentNode(r.getInput(), new Coordinate(60, 70));
		network.releaseComponentNode(r.getInput());
		
		network.grabComponentNode(r.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(r.getOutput(), new Coordinate(30, 70));
		network.releaseComponentNode(r.getOutput());
		
		//k:
		network.grabComponentNode(k.getInput(), new Coordinate(0,0));
		network.dragComponentNode(k.getInput(), new Coordinate(30, 70));
		network.releaseComponentNode(k.getInput());
		
		network.grabComponentNode(k.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(k.getOutput(), new Coordinate(30, 100));
		network.releaseComponentNode(k.getOutput());

		//l:
		network.grabComponentNode(l.getInput(), new Coordinate(0,0));
		network.dragComponentNode(l.getInput(), new Coordinate(60, 40));
		network.releaseComponentNode(l.getInput());
		
		network.grabComponentNode(l.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(l.getOutput(), new Coordinate(30, 40));
		network.releaseComponentNode(l.getOutput());
		
		//m:
		network.grabComponentNode(m.getInput(), new Coordinate(0,0));
		network.dragComponentNode(m.getInput(), new Coordinate(60, 70));
		network.releaseComponentNode(m.getInput());
		
		network.grabComponentNode(m.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(m.getOutput(), new Coordinate(60, 40));
		network.releaseComponentNode(m.getOutput());
		
		//n:
		network.grabComponentNode(n.getInput(), new Coordinate(0,0));
		network.dragComponentNode(n.getInput(), new Coordinate(30, 40));
		network.releaseComponentNode(n.getInput());
		
		network.grabComponentNode(n.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(n.getOutput(), new Coordinate(30, 70));
		network.releaseComponentNode(n.getOutput());

	}
	
	/**
	 * Test method for {@link main.java.network.Network#CalculateCurrent()}.
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
	 * Test method for {@link main.java.network.Network#simulate(Duration)}.
	 */
	@Test
	public void testSimulateOnSimpleNetwork() {
		buildSympleNetwork();
		
		network.simulate(null);
		
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
	 * Test method for {@link main.java.network.Network#simulate(Duration)}.
	 */
	@Test
	public void testSimulateOnParalelNetwork() {
		buildParalelNetwork();
		
		network.simulate(null);
		
		System.out.println("Test Paralel Simulate:");

		System.out.println("I[v] = " + v.getCurrent());
		System.out.println("I[r] = " + r.getCurrent());
		System.out.println("I[w] = " + w.getCurrent());
		System.out.println("I[k] = " + k.getCurrent());
		System.out.println("I[l] = " + l.getCurrent());
		System.out.println("I[m] = " + m.getCurrent());
		System.out.println("I[n] = " + n.getCurrent());
		System.out.println("--------------------------");
		/*
		assertTrue(0.5F == v.getCurrent());
		assertTrue(0.5F == r.getCurrent());
		assertTrue(0.5F == w.getCurrent());
		assertTrue(0.5F == k.getCurrent());
		assertTrue(0.5F == l.getCurrent());
		assertTrue(0.5F == m.getCurrent());
		assertTrue(0.5F == n.getCurrent());
		*/
	}
	
	
	@Test
	public void testSave() {
		buildParalelNetwork();
		
		StringBuilder writer = new StringBuilder();
		for (Component component: network.getComponents()) {
			component.save(writer);
		}
		
		System.out.print(writer.toString());
	
		network.save("test01.txt");
		
	}
	
	/**
	 * Test method for {@link main.java.network.Network#addEdge(main.java.network.Edge)}.
	 */
	@Test
	public void testAddComponent() {
		Edge added = new Edge();
		network.addEdge(added);		
	}

	/**
	 * Test method for {@link main.java.network.Network#removeEdge(main.java.network.Edge)}.
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
	 * Test method for {@link main.java.network.Network#tryToMergeNode(network.Node)}.
	 */
	@Test
	public void testTryToMergeNode() {
		Component c1 = new Resistance(10);
		Component c2 = new Resistance(10);
		
		network.addComponent(c1);
		network.addComponent(c2);

		assertTrue(4 == network.getComponentNodes().size());
		
		network.grabComponentNode(c1.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(c1.getOutput(), new Coordinate(30,30));
		network.releaseComponentNode(c1.getOutput());
		
		network.grabComponentNode(c2.getOutput(), new Coordinate(0,0));
		network.dragComponentNode(c2.getOutput(), new Coordinate(28,32));
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
	
	@Test 
	public void testLoad() {
		buildParalelNetwork();
		System.out.println("After build:");
		for (Component component : network.getComponents()) {
			System.out.println(component.toString());
		}
		
		network.save("testSave.txt");
		
		network.load("testSave.txt");
		
		System.out.println("After load:");

		for (Component component : network.getComponents()) {
			System.out.println(component.toString());
		}
		
	}
	
	@Test public void testDragComponent() {
		buildParalelNetwork();
		
		network.simulate(null);
		System.out.println("Current in connected network:");
		System.out.println(v.getCurrent());
		System.out.println(r.getCurrent());
		System.out.println(w.getCurrent());
		System.out.println("stb...");

		
		network.grabComponent(v, new Coordinate(0, 0));		
		network.dragComponent(v, new Coordinate(500, 2));
		network.releaseComponent(v);
		
		network.simulate(null);
		
		System.out.println("Current in disconnected network:");
		System.out.println(v.getCurrent());
		System.out.println(r.getCurrent());
		System.out.println(w.getCurrent());
		System.out.println("stb...");
	
		assertTrue(0 == v.getInput().getNoOfIncoming());
		assertTrue(1 == v.getInput().getNoOfOutgoing());

		assertTrue(1 == v.getOutput().getNoOfIncoming());
		assertTrue(0 == v.getOutput().getNoOfOutgoing());

//------------------------------------------------------------------------
		
		network.grabComponent(v, new Coordinate(0, 0));		
		network.dragComponent(v, new Coordinate(-500, -2));
		network.releaseComponent(v);
		
		network.simulate(null);
		
		System.out.println("Current in reconnected network:");
		System.out.println(v.getCurrent());
		System.out.println(r.getCurrent());
		System.out.println(w.getCurrent());
		System.out.println("stb...");

		assertTrue(1 == v.getInput().getNoOfIncoming());
		assertTrue(1 == v.getInput().getNoOfOutgoing());

		assertTrue(1 == v.getOutput().getNoOfIncoming());
		assertTrue(1 == v.getOutput().getNoOfOutgoing());
}
	
}
