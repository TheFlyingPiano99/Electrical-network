package main.java.math;

import java.util.*;

public class MyMath {
	
	public static Vector coordToVector(Coordinate c) {
		Vector v = new Vector(2);
		v.setAt(0, (float)c.x);
		v.setAt(1, (float)c.y);
		return v; 
	}

	public static Matrix multiply(float s, Matrix M) {
	    Matrix retM = new Matrix(M.row, M.column);
	    for (int c = 0; c < M.column; c++) {
	        for (int r = 0; r < M.row; r++) {
	            retM.setAt(r, c, M.at(r, c) * s);
	        }
	    }
	    return retM;
	}

	///Multiplication

    public static Matrix multiply(Matrix A, Matrix B) {
	    int row1 = A.row;
	    int column1row2 = A.column;
	    int column2 = B.column;

	    Matrix retM = new Matrix(row1, column2);
	    for (int c = 0; c < column2; c++) {
	        for (int r = 0; r < row1; r++) {
	            float n = 0;
	            for (int k = 0; k < column1row2; k++) {
	                n += A.at(r, k) * B.at(k, c);
	            }
	            retM.setAt(r, c, n);
	        }
	    }
	    return retM;
	}

	public static Vector multiply(Matrix M, Vector v) {
	    Vector retV = new Vector(M.row);
	    for ( int r = 0; r < M.row; r++) {
	        float n = 0;
	        for (int c = 0; c < M.column; c++) {
	            n += M.at(r, c) * v.at(c);
	        }
	        retV.setAt(r, n);
	    }
	    return retV;
	}
	
	public static Vector multiply(Vector v, Matrix M) {
	    Vector retV = new Vector(M.column);
	    for (int c = 0; c < M.column; c++) {
            float n = 0;
            for (int r = 0; r < M.row; r++) {
	                n += v.at(r) * M.at(r, c);
	        }
            retV.setAt(c, n);
	    }
	    return retV;
	}
	
	public static Matrix multiply(Matrix M, float s) {
	    Matrix retM = new Matrix(M.row, M.column);
	    for (int c = 0; c < M.column; c++) {
	        for (int r = 0; r < M.row; r++) {
	            retM.setAt(r, c, M.at(r, c) * s);
	        }
	    }
	    return retM;
	}
	
	public static Matrix transpose(Matrix M) {
	    Matrix retM = new Matrix(M.column, M.row);
	    for (int c = 0; c < M.column; c++) {
	        for (int r = 0; r < M.row; r++) {
	            retM.setAt(c, r, M.at(r, c));
	        }
	    }
	    return retM;
	}

	///Addition / Subtraction
	public static Matrix add(Matrix A, Matrix B) {
	    Matrix retM = new Matrix(A.row, A.column);
	    for (int c = 0; c < A.column; c++) {
	        for (int r = 0; r < A.row; r++) {
	            retM.setAt(r, c, A.at(r, c) + B.at(r, c));
	        }
	    }
	    return retM;
	}
	
	public static Matrix subtrackt(Matrix A, Matrix B) {
	    Matrix retM = new Matrix(A.row, A.column);
	    for (int c = 0; c < A.column; c++) {
	        for (int r = 0; r < A.row; r++) {
	            retM.setAt(r, c, A.at(r, c) - B.at(r, c));
	        }
	    }
	    return retM;
	}



/*
	public static std::ostream &operator<<(std::ostream &stream, const Matrix &M) {
	    for (unsigned int r = 0; r < M.row; r++) {
	        stream << "|";
	        for (unsigned int c = 0; c < M.column; c++) {
	            stream << M(r, c) << "|";
	        }
	        stream << "\n";
	    }
	    return stream;
	}
*/
	
	public static Matrix identity(int size) {
	    Matrix retM = new Matrix(size, size);
	    retM.fill(0);
	    for (int i = 0; i < size; i++) {
	        retM.setAt(i, i, 1.0f);
	    }
	    return retM;
	}

	/**
	 * The diagonal of this matrix is occupied by the elements of the v vector.
	 * @param v vector
	 * @return matrix
	 */
	public static Matrix diagonal(Vector v) {
	    Matrix retM = new Matrix(v.dimension, v.dimension);
	    retM.fill(0);
	    for (int i = 0; i < v.dimension; i++) {
	        retM.setAt(i, i, v.at(i));
	    }
	    return retM;
	}

	/**
	 * Removes columns given by they index in List.
	 * @param toRemoveIndexes - indexes to remove
	 */
	public static Matrix removeColumns(Matrix M, List<Integer> toRemoveIndexes) {
	    Matrix tempM = new Matrix(M.row, M.column - toRemoveIndexes.size());
	    
	    toRemoveIndexes.sort(null);
	    int indexOfcurrentRemov = 0;
	    int ct = 0; //Index of the temporary matrix.
	    for (int c = 0; c < M.column; c++) {
	        if (indexOfcurrentRemov < toRemoveIndexes.size() && toRemoveIndexes.get(indexOfcurrentRemov).equals(c)) {
	        	indexOfcurrentRemov++;
	        }
	        else {
	            for (int r = 0; r < M.row; r++) {
	                tempM.setAt(r, ct, M.at(r, c));
	            }
	            ct++;
	        }
	    }
	    return tempM;
	}
	
	/**
	 * Removes rows given by they index in List.
	 * @param toRemoveIndexes - indexes to remove
	 */
	public static Matrix removeRows(Matrix M, List<Integer> toRemoveIndexes) {
	    Matrix tempM = new Matrix(M.row - toRemoveIndexes.size(), M.column);
	    
	    toRemoveIndexes.sort(null);
	    int indexOfcurrentRemov = 0;

	    int rt = 0; //Index of the temporary matrix.
	    for (int r = 0; r < M.row; r++) {
	        if (indexOfcurrentRemov < toRemoveIndexes.size() && toRemoveIndexes.get(indexOfcurrentRemov).equals(r)) {
	        	indexOfcurrentRemov++;
	        }
	        else {
	            for (int i = 0; i < M.column; i++) {
	                tempM.setAt(rt, i, M.at(r, i));
	            }
	            rt++;
	        }
	    }
	    return tempM;
	}

	/**
	 * Multiply row by value.
	 * @param val - factor
	 */
	public static Matrix multiplyRow(Matrix M, int row, float val) {
	    Matrix retM = new Matrix(M.row, M.column);
	    retM.copy(M);
	    for (int c = 0; c < M.column; c++) {
	        retM.setAt(row, c, M.at(row, c) * val);
	    }
	    return retM;
	}

	/**
	 * Multiply column by value.
	 * @param val - factor
	 */
	public static Matrix multipyColumn(Matrix M, int column, float val) {
	    Matrix retM = new Matrix(M.row, M.column);
	    retM.copy(M);
	    for (int r = 0; r < M.row; r++) {
	        retM.setAt(r, column, M.at(r, column) * val);
	    }
	    return retM;
	}

	///----------------------------------------------------------------------
	///Vector related:
	
	//Multiply:

	public static Vector multiply(Vector v, float s) {
	    Vector retV = new Vector(v.dimension);
	    for (int i = 0; i < v.dimension; i++) {
	        retV.setAt(i, v.at(i) * s);
	    }
	    return retV;
	}


	public static Vector multiply(float s, Vector v) {
	    Vector retV = new Vector(v.dimension);
	    for (int i = 0; i < v.dimension; i++) {
	        retV.setAt(i, v.at(i) * s);
	    }
	    return retV;
	}

	public static float dot (Vector a, Vector b) {
	    float sum = 0;
	    for (int i = 0; i < a.dimension; i++) {
	        sum += a.at(i) * b.at(i);
	    }
	    return sum;
	}


	public static Vector divide(Vector v, float s) {
	    s = 1.0F / s;
	    Vector retV = new Vector(v.dimension);
	    for (int i = 0; i < v.dimension; i++) {
	        retV.setAt(i, v.at(i)* s);
	    }
	    return retV;
	}


	public static Vector divide(Vector a, Vector b) {
	    Vector retV = new Vector(a.dimension);
	    for (int i = 0; i < a.dimension; i++) {
	        retV.setAt(i, a.at(i) * (1.0F/b.at(i)));
	    }
	    return retV;
	}


	public static Vector negate(Vector v) {
	    Vector retV = new Vector(v.dimension);
	    for (int i = 0; i < v.dimension; i++) {
	        retV.setAt(i, -v.at(i));
	    }
	    return retV;
	}


	public static float magnitude(Vector v) {
	    float sum = 0;
	    for (int i = 0; i < v.dimension; i++) {
	        sum += v.at(i) * v.at(i);
	    }
	    return (float) Math.sqrt(sum);
	}


	public static Vector normalize(Vector v) {
		float mag = magnitude(v);
		if (mag != 0) {
		    return  divide(v, mag);
		}
		return v;
	}

	///Addition / Subtraction

	public static Vector add(Vector a, Vector b) {
	    Vector retV = new Vector(a.dimension);
	    for (int i = 0; i < a.dimension; i++) {
	        retV.setAt(i, a.at(i) + b.at(i));
	    }
	    return retV;
	}


	public static Vector subtrackt(Vector a, Vector b) {
	    Vector retV = new Vector(a.dimension);
	    for (int i = 0; i < a.dimension; i++) {
	        retV.setAt(i, a.at(i) - b.at(i));
	    }
	    return retV;
	}

	/*
	public static std::ostream &operator<<(std::ostream &stream, const Vector &v) {
	    stream << "(";
	    for (unsigned int e = 0; e < v.dimension; e++) {
	        stream << v[e];
	        if (e < v.dimension-1) {
	            stream << ", ";
	        }
	    }
	    stream << ")";
	    return stream;
	}
	 */
	
	public Matrix concatenateRight (Matrix A, Matrix B) {
		if (A.row != B.row) {
			throw new RuntimeException("Concatenation of matrices with nonequal number of rows.");
		}
		Matrix retM = new Matrix(A.row, A.column + B.column);
		
		for (int c = 0; c < retM.column; c++) {
			for (int r = 0; r < retM.row; r++) {
				if (c < A.column) {
					retM.setAt(r, c, A.at(r, c));
				}
				else {
					retM.setAt(r, c, B.at(r, c - A.column));					
				}
			}
		}
		
		return retM; 
	}
	
	public Matrix concatenateRight (Matrix A, Vector v) {
		if (A.row != v.dimension) {
			throw new RuntimeException("Concatenation of matrix and vector with nonequal number of rows.");
		}
		Matrix retM = new Matrix(A.row, A.column + 1);
		
		for (int c = 0; c < retM.column; c++) {
			for (int r = 0; r < retM.row; r++) {
				if (c < A.column) {
					retM.setAt(r, c, A.at(r, c));
				}
				else {
					retM.setAt(r, c, v.at(r));					
				}
			}
		}
		
		return retM; 
	}
	

	public Matrix concatenateBottom (Matrix A, Matrix B) {
		if (A.column != B.column) {
			throw new RuntimeException("Concatenation of matrices with nonequal number of columns.");
		}
		Matrix retM = new Matrix(A.row + B.row, A.column);
		
		for (int r = 0; r < retM.column; r++) {
			for (int c = 0; c < retM.row; c++) {
				if (r < A.row) {
					retM.setAt(r, c, A.at(r, c));
				}
				else {
					retM.setAt(r, c, B.at(r - A.row, c));					
				}
			}
		}
		
		return retM; 
	}

	public Matrix concatenateBottom (Matrix A, Vector v) {
		if (A.column != v.dimension) {
			throw new RuntimeException("Concatenation of matrix and vector with nonequal number of columns.");
		}
		Matrix retM = new Matrix(A.row + 1, A.column);
		
		for (int r = 0; r < retM.column; r++) {
			for (int c = 0; c < retM.row; c++) {
				if (r < A.row) {
					retM.setAt(r, c, A.at(r, c));
				}
				else {
					retM.setAt(r, c, v.at(c));					
				}
			}
		}
		
		return retM; 
	}

	//Coordinate related:------------------------------------------------------------------------------------
	public static double magnitude(Coordinate c) {
		return Math.sqrt(c.x * c.x + c.y * c.y);
	}
	
	public static Coordinate add(Coordinate a, Coordinate b) {
		return new Coordinate(a.x + b.x, a.y + b.y);
	}

	public static Coordinate subtrackt(Coordinate a, Coordinate b) {
		return new Coordinate(a.x - b.x, a.y - b.y);
	}
	
	
	
}
