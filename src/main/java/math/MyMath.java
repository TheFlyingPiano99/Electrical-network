package math;

import java.util.*;

/**
 * Mathematical operations.
 * HUN: Matematikai műveletek.
 * 
 * @author Zoltán Simon
 *
 */
public class MyMath {

	/**
	 * Convert {@link Coordinate} to {@link Vector}.
	 * HUN: Koordinátát vektorrá alakít.
	 * 
	 * @param c {@link Coordinate} to convert.
	 * @return {@link Vector} from {@link Coordinate}.
	 */
	public static Vector coordToVector(Coordinate c) {
		Vector v = new Vector(2);
		v.setAt(0, new Complex(c.x, 0));
		v.setAt(1, new Complex(c.y, 0));
		return v;
	}

	/**
	 * Reject vector a onto vector b.
	 * 
	 * @param a to reject
	 * @param b to reject on
	 * @return Rejected vector.
	 */
	public static Vector reject(Vector a, Vector b) {
		return subtract(a, multiply(b, new Complex(dot(a, b) / dot(b, b), 0)));
	}

	// Multiplication:---------------------------------------------------------------------------

	/**
	 * Multiply {@link Matrix} by scalar double s.
	 * HUN: Mátrix és skalár szorzata.
	 * 
	 * @param s scalar, double s.
	 * @param M {@link Matrix}.
	 * @return M multiplied by s.
	 */
	public static Matrix multiply(Complex s, Matrix M) {
		Matrix retM = new Matrix(M.row, M.column);
		for (int c = 0; c < M.column; c++) {
			for (int r = 0; r < M.row; r++) {
				retM.setAt(r, c, Complex.multiply(M.at(r, c), s));
			}
		}
		return retM;
	}

	/**
	 * Multiply matrix with matrix.
	 * HUN: Két mátrix szorzata.
	 * 
	 * @param A Matrix to multiply with from left.
	 * @param B Matrix to multiply with from right.
	 * @return solution
	 */
	public static Matrix multiply(Matrix A, Matrix B) {
		int row1 = A.row;
		int column1row2 = A.column;
		int column2 = B.column;

		Matrix retM = new Matrix(row1, column2);
		for (int c = 0; c < column2; c++) {
			for (int r = 0; r < row1; r++) {
				Complex n = new Complex(0, 0);
				for (int k = 0; k < column1row2; k++) {
					n.add(Complex.multiply(A.at(r, k), B.at(k, c)));
				}
				retM.setAt(r, c, n);
			}
		}
		return retM;
	}

	/**
	 * Multiply matrix with vector from right.
	 * HUN: Mátrix és vektor szorzata.
	 * 
	 * @param M Matrix to multiply.
	 * @param v Vector to multiply with.
	 * @return solution
	 */
	public static Vector multiply(Matrix M, Vector v) {
		Vector retV = new Vector(M.row);
		for (int r = 0; r < M.row; r++) {
			Complex n = new Complex(0, 0);
			for (int c = 0; c < M.column; c++) {
				n.add(Complex.multiply(M.at(r, c), v.at(c)));
			}
			retV.setAt(r, n);
		}
		return retV;
	}

	/**
	 * Multiply matrix with vector from left.
	 * HUN: Vektor és Mátrixszorzata.
	 * 
	 * @param v Vector to multiply with from left.
	 * @param M Matrix to multiply.
	 * @return solution
	 */
	public static Vector multiply(Vector v, Matrix M) {
		Vector retV = new Vector(M.column);
		for (int c = 0; c < M.column; c++) {
			Complex n = new Complex(0, 0);
			for (int r = 0; r < M.row; r++) {
				n.add(Complex.multiply(v.at(r), M.at(r, c)));
			}
			retV.setAt(c, n);
		}
		return retV;
	}

	/**
	 * Multiply matrix with scalar.
	 * HUN: Mátrix szorzata skalárral.
	 * 
	 * @param M Matrix to multiply.
	 * @param s Scalar to multiply with.
	 * @return solution
	 */
	public static Matrix multiply(Matrix M, Complex s) {
		Matrix retM = new Matrix(M.row, M.column);
		for (int c = 0; c < M.column; c++) {
			for (int r = 0; r < M.row; r++) {
				retM.setAt(r, c, Complex.multiply(M.at(r, c), s));
			}
		}
		return retM;
	}

	/// Addition
	/// / Subtraction:-----------------------------------------------------------------

	/**
	 * Add two matrices.
	 * HUN: Két mátrix összege.
	 * 
	 * @param A matrix
	 * @param B matrix
	 * @return sum
	 */
	public static Matrix add(Matrix A, Matrix B) {
		Matrix retM = new Matrix(A.row, A.column);
		for (int c = 0; c < A.column; c++) {
			for (int r = 0; r < A.row; r++) {
				retM.setAt(r, c, Complex.add(A.at(r, c), B.at(r, c)));
			}
		}
		return retM;
	}

	/**
	 * Subtract two matrices.
	 * HUN: Két mátrix különbsége.
	 * 
	 * @param A matrix
	 * @param B matrix
	 * @return difference
	 */
	public static Matrix subtract(Matrix A, Matrix B) {
		Matrix retM = new Matrix(A.row, A.column);
		for (int c = 0; c < A.column; c++) {
			for (int r = 0; r < A.row; r++) {
				retM.setAt(r, c, Complex.subtract(A.at(r, c), B.at(r, c)));
			}
		}
		return retM;
	}

	/// ----------------------------------------------------------------------
	/// Vector related:

	// Multiply:

	/**
	 * Multiply {@link Vector} by scalar double.
	 * HUN: Vektor és skalár szorzata.
	 * 
	 * @param v Vector to be multiplied.
	 * @param s Scalar double to multiply by.
	 * @return solution
	 */
	public static Vector multiply(Vector v, Complex s) {
		Vector retV = new Vector(v.dimension);
		for (int i = 0; i < v.dimension; i++) {
			retV.setAt(i, Complex.multiply(v.at(i), s));
		}
		return retV;
	}

	// A A
	// V Same, only parameters swapped V

	/**
	 * Multiply {@link Vector} by scalar double.
	 * HUN: Vektor és skalár szorzata.
	 * 
	 * @param s Scalar double to multiply by.
	 * @param v Vector to be multiplied.
	 * @return solution
	 */
	public static Vector multiply(Complex s, Vector v) {
		Vector retV = new Vector(v.dimension);
		for (int i = 0; i < v.dimension; i++) {
			retV.setAt(i, Complex.multiply(v.at(i), s));
		}
		return retV;
	}

	/**
	 * Dot product of two vectors.
	 * HUN: Két vektor skaláris szorzata.
	 * 
	 * @param a {@link Vector}
	 * @param b {@link Vector}
	 * @return double
	 */
	public static double dot(Vector a, Vector b) {
		double sum = 0;
		for (int i = 0; i < a.dimension; i++) {
			sum += Complex.multiply(a.at(i).conjugate(), b.at(i)).getRe();
		}
		return sum;
	}

	/**
	 * Divide vector by scalar double.
	 * HUN: Vektor osztása skalárral.
	 * 
	 * @param v {@link Vector}
	 * @param s Scalar double to divide by.
	 * @return {@link Vector}
	 */
	public static Vector divide(Vector v, Complex s) {
		Vector retV = new Vector(v.dimension);
		for (int i = 0; i < v.dimension; i++) {
			retV.setAt(i, Complex.divide(v.at(i), s));
		}
		return retV;
	}

	/**
	 * Divide {@link Vector} by {@link Vector}.
	 * HUN: Két vektor hányadosa
	 * 
	 * @param a {@link Vector}
	 * @param b {@link Vector}
	 * @return {@link Vector}
	 */
	public static Vector divide(Vector a, Vector b) {
		Vector retV = new Vector(a.dimension);
		for (int i = 0; i < a.dimension; i++) {
			retV.setAt(i, Complex.divide(a.at(i), b.at(i)));
		}
		return retV;
	}

	/**
	 * Change sign of elements of {@link Vector}.
	 * HUN: Előjelcsere a vektor minden elemén.
	 * 
	 * @param v {@link Vector} to negate
	 * @return negated vector
	 */
	public static Vector negate(Vector v) {
		Vector retV = new Vector(v.dimension);
		for (int i = 0; i < v.dimension; i++) {
			retV.setAt(i, new Complex(-v.at(i).getRe(), -v.at(i).getIm()));
		}
		return retV;
	}

	/**
	 * Calculates magnitude of given {@link Vector}.
	 * HUN: Kiszámolja egy adott vektor hosszát.
	 * 
	 * @param v vector to calculate magnitude of
	 * @return double magnitude
	 */
	public static double magnitude(Vector v) {
		double sum = 0;
		for (int i = 0; i < v.dimension; i++) {
			sum += Complex.multiply(v.at(i).conjugate(), v.at(i)).getRe();
		}
		return Math.sqrt(sum);
	}

	/**
	 * Normalizes {@link Vector}, to unit vector.
	 * HUN: Normalizálja a vektor hosszát.
	 * 
	 * @param v {@link Vector} to normalize.
	 * @return normalized {@link Vector}
	 */
	public static Vector normalize(Vector v) {
		double mag = magnitude(v);
		if (mag != 0) {
			return divide(v, new Complex(mag, 0));
		}
		return v;
	}

	/// Addition / Subtraction:---------------------------------------------------

	/**
	 * Add two vectors.
	 * HUN: Két vektor összege.
	 * 
	 * @param a {@link Vector} to add
	 * @param b {@link Vector} to add
	 * @return sum {@link Vector}
	 */
	public static Vector add(Vector a, Vector b) {
		Vector retV = new Vector(a.dimension);
		for (int i = 0; i < a.dimension; i++) {
			retV.setAt(i, Complex.add(a.at(i), b.at(i)));
		}
		return retV;
	}

	/**
	 * Subtract two vectors.
	 * HUN: Két vektor különbsége.
	 * 
	 * @param a {@link Vector} to subtract from
	 * @param b {@link Vector} to subtract
	 * @return difference {@link Vector}
	 */
	public static Vector subtract(Vector a, Vector b) {
		Vector retV = new Vector(a.dimension);
		for (int i = 0; i < a.dimension; i++) {
			retV.setAt(i, Complex.subtract(a.at(i), b.at(i)));
		}
		return retV;
	}

	// Coordinate
	// related:------------------------------------------------------------------------------------

	/**
	 * Magnitude of coordinate as vector.
	 * HUN: A koordináta hossza, vektorként.
	 * 
	 * @param c {@link Coordinate}
	 * @return magnitude double
	 */
	public static double magnitude(Coordinate c) {
		return Math.sqrt(c.x * c.x + c.y * c.y);
	}

	/**
	 * Add two coordinates as vectors.
	 * HUN: Két koordináta összege, vektorként.
	 * 
	 * @param a first
	 * @param b second
	 * @return sum {@link Coordinate}
	 */
	public static Coordinate add(Coordinate a, Coordinate b) {
		return new Coordinate(a.x + b.x, a.y + b.y);
	}

	/**
	 * Subtract two coordinates as vectors.
	 * HUN: Két koordináta különbsége, vektorként.
	 * 
	 * @param a first
	 * @param b second
	 * @return difference {@link Coordinate}
	 */
	public static Coordinate subtract(Coordinate a, Coordinate b) {
		return new Coordinate(a.x - b.x, a.y - b.y);
	}

	public static double min(double val1, double val2) {
		if (val1 < val2) {
			return val1;
		} else {
			return val2;
		}
	}

	public static double max(double val1, double val2) {
		if (val1 > val2) {
			return val1;
		} else {
			return val2;
		}
	}

}
