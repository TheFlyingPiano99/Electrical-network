/**
 * 
 */
package math;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;


/**
 * @author simon
 *
 */
public class MyMathTest {

	/**
	 * Test method for {@link math.MyMath#multiply(float, math.Matrix)}.
	 */
	@Test
	public void testMultiplyFloatMatrix() {
		float f = 0;
		Matrix M = new Matrix(3,3);
		M.fill(0);
		
		Matrix exp = new Matrix(3,3);
		exp.fill(0);
		
		assertEquals(exp, MyMath.multiply(f, M));
	}

	/**
	 * Test method for {@link math.MyMath#multiply(math.Matrix, math.Matrix)}.
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
	 * Test method for {@link math.MyMath#multiply(math.Matrix, math.Vector)}.
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
	 * Test method for {@link math.MyMath#multiply(math.Vector, math.Matrix)}.
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
	 * Test method for {@link math.MyMath#multiply(math.Matrix, float)}.
	 */
	@Test
	public void testMultiplyMatrixFloat() {
		float f = 0;
		Matrix M = new Matrix(3,3);
		M.fill(0);
		
		Matrix exp = new Matrix(3,3);
		exp.fill(0);
		
		assertEquals(exp, MyMath.multiply(M, f));
	}

	/**
	 * Test method for {@link math.MyMath#Transpose(math.Matrix)}.
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
		
		assertEquals(exp, MyMath.Transpose(m));
	}

	/**
	 * Test method for {@link math.MyMath#add(math.Matrix, math.Matrix)}.
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
	 * Test method for {@link math.MyMath#subtrackt(math.Matrix, math.Matrix)}.
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
	 * Test method for {@link math.MyMath#Identity(int)}.
	 */
	@Test
	public void testIdentity() {
		Matrix exp = new Matrix (3,3);
		exp.fill(0);
		exp.setAt(0,0, 1);
		exp.setAt(1,1, 1);
		exp.setAt(2,2, 1);
		
		assertEquals(exp, MyMath.Identity(3));
	}

	/**
	 * Test method for {@link math.MyMath#Diagonal(math.Vector)}.
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
		
		assertEquals(exp, MyMath.Diagonal(v));
	}

	/**
	 * Test method for {@link math.MyMath#RemoveColumns(math.Matrix, java.util.ArrayList)}.
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
		
		assertEquals(exp, MyMath.RemoveColumns(M, toRemove));		
	}

	/**
	 * Test method for {@link math.MyMath#RemoveRows(math.Matrix, java.util.List)}.
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
		
		assertEquals(exp, MyMath.RemoveRows(M, toRemove));		
	}

	/**
	 * Test method for {@link math.MyMath#MultiplyRow(math.Matrix, int, float)}.
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
		
		assertEquals(exp, MyMath.MultiplyRow(M, 0, 3));
	}

	/**
	 * Test method for {@link math.MyMath#MultipyColumn(math.Matrix, int, float)}.
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
		
		assertEquals(exp, MyMath.MultipyColumn(M, 0, 3));
	}

	//--------------------------------------------------------------------------------------------------------
	//Vector related:
	
	/**
	 * Test method for {@link math.MyMath#multiply(math.Vector, float)}.
	 */
	@Test
	public void testMultiplyVectorFloat() {
		Vector v = new Vector(3);
		v.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(6);
		
		assertEquals(exp, MyMath.multiply(v, 3));
	}

	/**
	 * Test method for {@link math.MyMath#multiply(float, math.Vector)}.
	 */
	@Test
	public void testMultiplyFloatVector() {
		Vector v = new Vector(3);
		v.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(6);
		
		assertEquals(exp, MyMath.multiply(3, v));
	}

	/**
	 * Test method for {@link math.MyMath#Dot(math.Vector, math.Vector)}.
	 */
	@Test
	public void testDot() {
		Vector a = new Vector(3);
		a.fill(2);
		
		Vector b = new Vector(3);
		b.fill(3);
		
		float exp = a.at(0)*b.at(0) + a.at(1)*b.at(1) + a.at(2)*b.at(2);
		
		assertTrue(MyMath.Dot(a, b) == exp);
	}

	/**
	 * Test method for {@link math.MyMath#divide(math.Vector, float)}.
	 */
	@Test
	public void testDivideVectorFloat() {
		Vector v = new Vector(3);
		v.fill(4);
		
		Vector exp = new Vector(3);
		exp.fill(2);
		
		assertEquals(exp, MyMath.divide(v, 2));
	}

	/**
	 * Test method for {@link math.MyMath#divide(math.Vector, math.Vector)}.
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
	 * Test method for {@link math.MyMath#negate(math.Vector)}.
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
	 * Test method for {@link math.MyMath#Magnitude(math.Vector)}.
	 */
	@Test
	public void testMagnitude() {
		Vector v = new Vector(3);
		v.fill(3);
		
		float exp = (float) Math.sqrt(v.at(0)*v.at(0) + v.at(1)*v.at(1) + v.at(2)*v.at(2));
		assertTrue(MyMath.Magnitude(v) == exp);
	}

	/**
	 * Test method for {@link math.MyMath#Normalize(math.Vector)}.
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
		
		
		assertEquals(exp, MyMath.Normalize(v));
	}

	/**
	 * Test method for {@link math.MyMath#add(math.Vector, math.Vector)}.
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
	 * Test method for {@link math.MyMath#subtrackt(math.Vector, math.Vector)}.
	 */
	@Test
	public void testSubtracktVectorVector() {
		Vector a = new Vector(3);
		a.fill(3);
		Vector b = new Vector(3);
		b.fill(2);
		
		Vector exp = new Vector(3);
		exp.fill(1);
		
		assertEquals(exp, MyMath.subtrackt(a, b));
	}

}
