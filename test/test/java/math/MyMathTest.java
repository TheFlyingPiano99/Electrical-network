package test.java.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import math.Complex;
import math.Matrix;
import math.MyMath;
import math.Vector;

public class MyMathTest {

	@Test
	public void testMultiplyMatrices() {
		Matrix left = new Matrix(2, 2);
		left.setAt(0, 0, new Complex(1, 0));
		left.setAt(0, 1, new Complex(2, 0));
		left.setAt(1, 0, new Complex(3, 0));
		left.setAt(1, 1, new Complex(4, 0));

		Matrix right = new Matrix(2, 2);
		right.setAt(0, 0, new Complex(5, 0));
		right.setAt(0, 1, new Complex(6, 0));
		right.setAt(1, 0, new Complex(7, 0));
		right.setAt(1, 1, new Complex(8, 0));

		Matrix expected = new Matrix(2, 2);
		expected.setAt(0, 0, new Complex(19, 0));
		expected.setAt(0, 1, new Complex(22, 0));
		expected.setAt(1, 0, new Complex(43, 0));
		expected.setAt(1, 1, new Complex(50, 0));

		assertEquals(expected, MyMath.multiply(left, right));
	}

	@Test
	public void testTranspose() {
		Matrix matrix = new Matrix(2, 3);
		matrix.setAt(0, 1, new Complex(4, 0));

		Matrix expected = new Matrix(3, 2);
		expected.setAt(1, 0, new Complex(4, 0));

		assertEquals(expected, MyMath.transpose(matrix));
	}

	@Test
	public void testSubtractMatrices() {
		Matrix left = new Matrix(1, 2);
		left.fill(new Complex(4, 0));
		Matrix right = new Matrix(1, 2);
		right.fill(new Complex(3, 0));

		Matrix expected = new Matrix(1, 2);
		expected.fill(new Complex(1, 0));

		assertEquals(expected, MyMath.subtract(left, right));
	}

	@Test
	public void testRemoveColumns() {
		Matrix matrix = new Matrix(2, 3);
		matrix.setAt(0, 0, new Complex(1, 0));
		matrix.setAt(0, 1, new Complex(2, 0));
		matrix.setAt(0, 2, new Complex(3, 0));
		matrix.setAt(1, 0, new Complex(4, 0));
		matrix.setAt(1, 1, new Complex(5, 0));
		matrix.setAt(1, 2, new Complex(6, 0));

		Matrix expected = new Matrix(2, 2);
		expected.setAt(0, 0, new Complex(1, 0));
		expected.setAt(1, 0, new Complex(4, 0));
		expected.setAt(0, 1, new Complex(3, 0));
		expected.setAt(1, 1, new Complex(6, 0));

		assertEquals(expected, MyMath.removeColumns(matrix, Arrays.asList(1)));
	}

	@Test
	public void testMultiplyVectorByScalar() {
		Vector vector = new Vector(3);
		vector.fill(new Complex(2, 0));

		Vector expected = new Vector(3);
		expected.fill(new Complex(6, 0));

		assertEquals(expected, MyMath.multiply(vector, new Complex(3, 0)));
	}
}
