package test.java.math;

import static org.junit.Assert.*;

import org.junit.Test;

import main.java.math.Gauss;
import main.java.math.GaussException;
import main.java.math.InfiniteSolutions;
import main.java.math.Matrix;
import main.java.math.NoSolution;
import main.java.math.Vector;

public class GaussTest {

	@Test(expected=InfiniteSolutions.class)
	public void testEliminateInfSol() throws GaussException {
		Matrix linSys = new Matrix(5, 4);
		linSys.fill(0);
		
		Gauss.Eliminate(linSys);
	}
	
	@Test(expected=NoSolution.class)
	public void testEliminateForbiden() throws GaussException {
		Matrix linSys = new Matrix(5, 4);
		linSys.fill(0);
		linSys.setAt(4, 0, 1);
		linSys.setAt(3, 2, 1);
		
		Gauss.Eliminate(linSys);
	}
	
	@Test
	public void testEliminateClean() throws GaussException {
		Matrix linSys = new Matrix(5, 4);
		linSys.fill(0);
		//"Left" side: (Top)
		linSys.setAt(0, 0, 1);
		linSys.setAt(1, 1, 1);
		linSys.setAt(2, 2, 1);
		linSys.setAt(3, 3, 1);

		//"Right" side:	(Bottom)
		linSys.setAt(4, 0, 1);
		linSys.setAt(4, 1, 3);
		linSys.setAt(4, 2, 6);
		linSys.setAt(4, 3, 9);
		
		//Expected solution:
		Vector exp = new Vector(4);
		exp.setAt(0, 1);
		exp.setAt(1, 3);
		exp.setAt(2, 6);
		exp.setAt(3, 9);
		
		assertEquals(exp, Gauss.Eliminate(linSys));
	}

	@Test
	public void testReduce() {
		Matrix linSys = new Matrix(5, 4);
		linSys.fill(0);
		
		//Vector exp = new Vector(0);
		Gauss.Reduce(linSys);
		//assertEquals(exp, Gauss.Eliminate(linSys));
	}

	@Test
	public void testCoefficientMatrix() {
		Matrix left = new Matrix(2,2);
		left.fill(2);
		Vector right = new Vector(2);
		right.fill(3);
		Matrix exp = new Matrix(3,2);
		exp.fill(2);
		exp.setAt(2, 0, 3);
		exp.setAt(2, 1, 3);
		
		assertEquals(exp, Gauss.CoefficientMatrix(left, right));
	}

}
