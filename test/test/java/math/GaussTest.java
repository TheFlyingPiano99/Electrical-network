package test.java.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import math.Complex;
import math.Gauss;
import math.InfiniteSolutions;
import math.Matrix;
import math.NoSolution;
import math.Vector;

public class GaussTest {

	@Test
	public void testEliminateInfiniteSolutions() {
		Matrix system = new Matrix(5, 4);
		system.fill(new Complex(0, 0));

		assertThrows(InfiniteSolutions.class, () -> Gauss.Eliminate(system));
	}

	@Test
	public void testEliminateNoSolution() {
		Matrix system = new Matrix(5, 4);
		system.fill(new Complex(0, 0));
		system.setAt(4, 0, new Complex(1, 0));
		system.setAt(3, 2, new Complex(1, 0));

		assertThrows(NoSolution.class, () -> Gauss.Eliminate(system));
	}

	@Test
	public void testEliminateCleanSystem() {
		Matrix system = new Matrix(5, 4);
		system.fill(new Complex(0, 0));
		for (int i = 0; i < 4; i++) {
			system.setAt(i, i, new Complex(1, 0));
			system.setAt(4, i, new Complex(i == 0 ? 1 : i * 3, 0));
		}

		Vector expected = new Vector(4);
		expected.setAt(0, new Complex(1, 0));
		expected.setAt(1, new Complex(3, 0));
		expected.setAt(2, new Complex(6, 0));
		expected.setAt(3, new Complex(9, 0));

		assertEquals(expected, Gauss.Eliminate(system));
	}

	@Test
	public void testCoefficientMatrix() {
		Matrix left = new Matrix(2, 2);
		left.fill(new Complex(2, 0));
		Vector right = new Vector(2);
		right.fill(new Complex(3, 0));

		Matrix expected = new Matrix(3, 2);
		expected.fill(new Complex(2, 0));
		expected.setAt(2, 0, new Complex(3, 0));
		expected.setAt(2, 1, new Complex(3, 0));

		assertEquals(expected, Gauss.CoefficientMatrix(left, right));
	}
}
