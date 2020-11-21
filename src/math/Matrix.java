package math;

public class Matrix {
    float n[];
    public int row;
    public int column;

    /**
     * Constructor
     * @param r - number of rows
     * @param c - number of columns
     * @return
     */
    public Matrix(int r,int c) {
    	row = r;
    	column = c;
        n = new float[row * column];
        /*if (n == nullptr) {
            throw runtime_error("Initialisation failed!");
        }*/
    }

    public Matrix(Matrix M) {
    	row = M.row;
    	column = M.column;
        n = new float[row * column];
/*        if (n == nullptr) {
            throw std::runtime_error("Initialisation failed!");
        }*/
        for (int c = 0; c < column; c++) {
            for  (int r = 0; r < row; r++) {
                n[c * row + r] = M.at(r, c);
            }
        }
    }

    /**
     * Indexing
     * @param r - row index
     * @param c - columns index
     * @return
     */
    public float at(int r, int c) { //indexeles (sor, oszlop)
    	return n[c * row + r];
    }
    
    public void setAt(int r, int c, float val) {
    	n[c * row + r] = val;
    }

    /**
     * Fills up matrix with given parameter
     * @param val - to fill with
     */
    public void fill(float val) {
        for (int c = 0; c < column; c++) {
            for (int r = 0; r < row; r++) {
            	n[c * row + r] = val;
            }
        }
    }

    public Matrix copy (Matrix M) {
        if (!this.equals(M)) {
            for (int c = 0; c < column; c++) {
                for (int r = 0; r < row; r++) {
                	n[c * row + r] = M.at(r, c);
                }
            }
        }
        return this;
    }

    public Matrix copyWithResize (Matrix M) {
        if (!this.equals(M)) {
            row = M.row;
            column = M.column;
            n = new float[row * column];
            
            for (int c = 0; c < column; c++) {
                for (int r = 0; r < row; r++) {
                	n[c * row + r] = M.at(r, c);
                }
            }
        }
        return this;
    }


	//Other
	//float Determinant (Matrix M);

}
