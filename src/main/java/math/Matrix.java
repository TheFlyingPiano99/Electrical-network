package math;


/**
 * Column major matrix, for double values.
 * HUN: Oszlopfolytonos mátrix, lebegőpontos értékek tárolására.
 * @author Simon Zoltán
 *
 */
public class Matrix {
	Complex n[];
    public int row;
    public int column;


	//Constructors:---------------------------------------------------
	
    /**
     * Constructor
     * @param r - number of rows
     * @param c - number of columns
     */    
    public Matrix(int r,int c) {
    	row = r;
    	column = c;
        n = new Complex[row * column];
        for (int j = 0; j < column; j++) {
            for (int i = 0; i < row; i++) {
                n[j * row + i] = new Complex(0, 0);
            }
        }
        /*if (n == nullptr) {
            throw runtime_error("Initialisation failed!");
        }*/
    }

    /**
     * Constructor
     * @param M source matrix
     */
    public Matrix(Matrix M) {
    	row = M.row;
    	column = M.column;
        n = new Complex[row * column];
        /*if (n == nullptr) {
            throw std::runtime_error("Initialisation failed!");
        }*/
        for (int c = 0; c < column; c++) {
            for  (int r = 0; r < row; r++) {
                n[c * row + r] = M.at(r, c).copy();
            }
        }
    }

    //hashCode/equals:---------------------------------------------------
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix other = (Matrix) obj;
		if (column != other.column)
			return false;
		if (row != other.row)
			return false;
		for (int c = 0; c < column; c++) {
			for (int r = 0; r < row; r++) {
				if (n[c * row + r] != other.at(r, c)) {
					return false;
				}
			}
		}
		return true;
	}

    //Indexing:-------------------------------------------------------
    
    /**
     * Indexing. Returns the value stored at position of r. row and c. column. 
     * @param r - row index
     * @param c - columns index
     * @return	value
     */
    public Complex at(int r, int c) { //indexeles (sor, oszlop)
    	return n[c * row + r];
    }
    
    /**
     * Writing. Sets the value at position of r. row and c. column to the given value. 
     * @param r	row index
     * @param c column index
     * @param val new value at given position.
     */
    public void setAt(int r, int c, Complex val) {
    	n[c * row + r] = val.copy();
    }

    /**
     * Fills up matrix with given parameter
     * HUN: Mátrix feltöltése a kapott értékkel.
     * @param val - to fill with
     */
    public void fill(Complex val) {
        for (int c = 0; c < column; c++) {
            for (int r = 0; r < row; r++) {
            	n[c * row + r] = val.copy();
            }
        }
    }

    /**
     * Copy content of parameter matrix to this.
     * HUN: Tartalom másolása. 
     * @param M Source matrix. Must have same size as this!
     * @return this
     */
    public Matrix copy (Matrix M) {
        if (!this.equals(M)) {
            for (int c = 0; c < column; c++) {
                for (int r = 0; r < row; r++) {
                	n[c * row + r] = M.at(r, c).copy();
                }
            }
        }
        return this;
    }

    /**
     * Copy content of parameter matrix to this with resize.
     * HUN: Tartalom másolása újraméretezéssel. 
     * @param M Source matrix.
     * @return this
     */    
    public Matrix copyWithResize (Matrix M) {
        if (!this.equals(M)) {
            row = M.row;
            column = M.column;
            n = new Complex[row * column];
            
            for (int c = 0; c < column; c++) {
                for (int r = 0; r < row; r++) {
                	n[c * row + r] = M.at(r, c).copy();
                }
            }
        }
        return this;
    }

    @Override
    public String toString()
    {
        String str = "(";
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < column; c++) {
                str = str + this.at(r, c);
                if (c != column - 1) {
                    str += ", ";
                }
            }
            if (r != row - 1) {
                str += ",\n";
            }
        }
        str += ")";
        return str;
    }

	//Other
	//double Determinant (Matrix M);
    
}
