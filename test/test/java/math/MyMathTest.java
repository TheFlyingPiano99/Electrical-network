/**
 * 
 */
package test.java.math;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import main.java.math.Matrix;
import main.java.math.MyMath;
import main.java.math.Vector;


/**
 * @author Simon Zoltán
 *
 */
public class MyMathTest {

	/**
	 * Test method for {@link main.java.math.MyMath#multiply(double, main.java.math.Matrix)}.
	 */
	@Test
	public void testMultiplyDoubleMatrix() {
		double f = 0;
		Matrix M = new Matrix(3,3);
		M.fill(0);
		
		Matrix exp = new Matrix(3,3);
		exp.fill(0);
		
		assertEquals(exp, MyMath.multiply(f, M));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multiply(main.java.math.Matrix, main.java.math.Matrix)}.
	 */
	@Test
	public void testMultiplyMatrixMatrix() {
		Matrix A = new Matrix (2,2);
		Matrix B = new Matrix (2,2);
		
		A.fill(1);
		B.fill(1);
		
		Matrix exp = new Matrix(2, 2);
		exp.fill(2);
		
		assertEquals(exp, MyMath.multiply(A, B));
		
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multiply(main.java.math.Matrix, main.java.math.Vector)}.
	 */
	@Test
	public void testMultiplyMatrixVector() {
		Matrix A = new Matrix (3,2);
		Vector v = new Vector(2);
		
		A.fill(1);
		v.fill(1);
		
		Vector exp = new Vector(3);
		exp.fill(2);
		
		assertEquals(exp, MyMath.multiply(A, v));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multiply(main.java.math.Vector, main.java.math.Matrix)}.
	 */
	@Test
	public void testMultiplyVectorMatrix() {
		Vector v = new Vector(3);
		Matrix A = new Matrix (3,2);
		
		A.fill(1);
		v.fill(1);
		
		Vector exp = new Vector(2);
		exp.fill(3);
		
		assertEquals(exp, MyMath.multiply(v, A));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multiply(main.java.math.Matrix, float)}.
	 */
	@Test
	public void testMultiplyMatrixDouble() {
		float f = 0;
		Matrix M = new Matrix(3,3);
		M.fill(0);
		
		Matrix exp = new Matrix(3,3);
		exp.fill(0);
		
		assertEquals(exp, MyMath.multiply(M, f));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#transpose(main.java.math.Matrix)}.
	 */
	@Test
	public void testTranspose() {
		Matrix m = new Matrix(3,4);
		m.fill(0);
		m.setAt(0, 0, 1);
		m.setAt(1, 1, 2);
		m.setAt(2, 2, 3);
		m.setAt(0, 1, 4);	//!
		
		Matrix exp = new Matrix(4,3);
		exp.fill(0);
		exp.setAt(0, 0, 1);
		exp.setAt(1, 1, 2);
		exp.setAt(2, 2, 3);
		exp.setAt(1, 0, 4);	//!
		
		assertEquals(exp, MyMath.transpose(m));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#add(main.java.math.Matrix, main.java.math.Matrix)}.
	 */
	@Test
	public void testAddMatrixMatrix() {
		Matrix A = new Matrix (2,2);
		Matrix B = new Matrix (2,2);
		
		A.fill(4);
		B.fill(3);
		
		Matrix exp = new Matrix(2, 2);
		exp.fill(7);
		
		assertEquals(exp, MyMath.add(A, B));
	
	}

	/**
	 * Test method for {@link main.java.math.MyMath#subtrackt(main.java.math.Matrix, main.java.math.Matrix)}.
	 */
	@Test
	public void testSubtracktMatrixMatrix() {
		Matrix A = new Matrix (2,2);
		Matrix B = new Matrix (2,2);
		
		A.fill(4);
		B.fill(3);
		
		Matrix exp = new Matrix(2, 2);
		exp.fill(1);
		
		assertEquals(exp, MyMath.subtrackt(A, B));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#identity(int)}.
	 */
	@Test
	public void testIdentity() {
		Matrix exp = new Matrix (3,3);
		exp.fill(0);
		exp.setAt(0,0, 1);
		exp.setAt(1,1, 1);
		exp.setAt(2,2, 1);
		
		assertEquals(exp, MyMath.identity(3));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#diagonal(main.java.math.Vector)}.
	 */
	@Test
	public void testDiagonal() {
		Vector v = new Vector(3);
		v.setAt(0, 1);
		v.setAt(1, 3);
		v.setAt(2, 6);
		
		Matrix exp = new Matrix (3,3);
		exp.fill(0);
		exp.setAt(0,0, 1);
		exp.setAt(1,1, 3);
		exp.setAt(2,2, 6);
		
		assertEquals(exp, MyMath.diagonal(v));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#removeColumns(main.java.math.Matrix, java.util.ArrayList)}.
	 */
	@Test
	public void testRemoveColumns() {
		Matrix M = new Matrix(3,6);
		M.fill(2);
		
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
	    
		toRemove.add(3);
		toRemove.add(1);
		
		Matrix exp = new Matrix(3,4);
		exp.fill(2);
		
		assertEquals(exp, MyMath.removeColumns(M, toRemove));		
	}

	/**
	 * Test method for {@link main.java.math.MyMath#removeRows(main.java.math.Matrix, java.util.List)}.
	 */
	@Test
	public void testRemoveRows() {
		Matrix M = new Matrix(6,3);
		M.fill(2);
		
		ArrayList<Integer> toRemove = new ArrayList<>();
		toRemove.add(3);
		toRemove.add(1);
		
		Matrix exp = new Matrix(4,3);
		exp.fill(2);
		
		assertEquals(exp, MyMath.removeRows(M, toRemove));		
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multiplyRow(main.java.math.Matrix, int, float)}.
	 */
	@Test
	public void testMultiplyRow() {
		Matrix M = new Matrix(2,2);
		M.fill(2);
		Matrix exp = new Matrix(2,2);
		exp.setAt(0,0, 6);
		exp.setAt(0,1, 6);
		exp.setAt(1,0, 2);
		exp.setAt(1,1, 2);
		
		assertEquals(exp, MyMath.multiplyRow(M, 0, 3));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multipyColumn(main.java.math.Matrix, int, float)}.
	 */
	@Test
	public void testMultipyColumn() {
		Matrix M = new Matrix(2,2);
		M.fill(2);
		Matrix exp = new Matrix(2,2);
		exp.setAt(0,0, 6);
		exp.setAt(1,0, 6);
		exp.setAt(0,1, 2);
		exp.setAt(1,1, 2);
		
		assertEquals(exp, MyMath.multipyColumn(M, 0, 3));
	}

	//--------------------------------------------------------------------------------------------------------
	//Vector related:
	
	/**
	 * Test method for {@link main.java.math.MyMath#multiply(main.java.math.Vector, float)}.
	 */
	@Test
	public void testMultiplyVectorDouble() {
		Vector v = new Vector(3);
		v.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(6);
		
		assertEquals(exp, MyMath.multiply(v, 3));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#multiply(float, main.java.math.Vector)}.
	 */
	@Test
	public void testMultiplyDoubleVector() {
		Vector v = new Vector(3);
		v.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(6);
		
		assertEquals(exp, MyMath.multiply(3, v));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#dot(main.java.math.Vector, main.java.math.Vector)}.
	 */
	@Test
	public void testDot() {
		Vector a = new Vector(3);
		a.fill(2);
		
		Vector b = new Vector(3);
		b.fill(3);
		
		float exp = a.at(0)*b.at(0) + a.at(1)*b.at(1) + a.at(2)*b.at(2);
		
		assertTrue(MyMath.dot(a, b) == exp);
	}

	/**
	 * Test method for {@link main.java.math.MyMath#divide(main.java.math.Vector, float)}.
	 */
	@Test
	public void testDivideVectorDouble() {
		Vector v = new Vector(3);
		v.fill(4);
		
		Vector exp = new Vector(3);
		exp.fill(2);
		
		assertEquals(exp, MyMath.divide(v, 2));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#divide(main.java.math.Vector, main.java.math.Vector)}.
	 */
	@Test
	public void testDivideVectorVector() {
		Vector a = new Vector(3);
		a.fill(8);
		Vector b = new Vector(3);
		b.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(4);
		
		assertEquals(exp, MyMath.divide(a, b));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#negate(main.java.math.Vector)}.
	 */
	@Test
	public void testNegate() {
		Vector v = new Vector(3);
		v.fill(4);
		
		Vector exp = new Vector(3);
		exp.fill(-4);
		
		assertEquals(exp, MyMath.negate(v));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#magnitude(main.java.math.Vector)}.
	 */
	@Test
	public void testMagnitude() {
		Vector v = new Vector(3);
		v.fill(3);
		
		float exp = (float) Math.sqrt(v.at(0)*v.at(0) + v.at(1)*v.at(1) + v.at(2)*v.at(2));
		assertTrue(MyMath.magnitude(v) == exp);
	}

	/**
	 * Test method for {@link main.java.math.MyMath#normalize(main.java.math.Vector)}.
	 */
	@Test
	public void testNormalize() {
		Vector v = new Vector(3);
		v.fill(0);
		
		Vector exp = new Vector(3);

		float mag = (float) Math.sqrt(v.at(0)*v.at(0) + v.at(1)*v.at(1) + v.at(2)*v.at(2));
		if (mag != 0) {
			exp.setAt(0, v.at(0) / mag);
			exp.setAt(1, v.at(1) / mag);
			exp.setAt(2, v.at(2) / mag);
		}
		else {
			exp.copy(v);
		}
		
		
		assertEquals(exp, MyMath.normalize(v));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#add(main.java.math.Vector, main.java.math.Vector)}.
	 */
	@Test
	public void testAddVectorVector() {
		Vector a = new Vector(3);
		a.fill(3);
		Vector b = new Vector(3);
		b.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(5);
		
		assertEquals(exp, MyMath.add(a, b));
	}

	/**
	 * Test method for {@link main.java.math.MyMath#subtract(main.java.math.Vector, main.java.math.Vector)}.
	 */
	@Test
	public void testSubtracktVectorVector() {
		Vector a = new Vector(3);
		a.fill(3);
		Vector b = new Vector(3);
		b.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(1);
		
		assertEquals(exp, MyMath.subtract(a, b));
	}

}
