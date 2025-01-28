package math;


/**
 * Vector of double values.
 * HUN: Vektor lebegőpontos értékek tárolására.
 * @author Simon Zoltán
 *
 */
public class Vector {
	private double n[];
    public int dimension;


	//Constructors:-------------------------------------------------
	
    public Vector(int d) {
    	dimension = d;
        n = new double[dimension];
    }

    public Vector(Vector v) {
    	dimension = v.dimension; 
        n = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            n[i] = v.at(i);
        }
    }

    //Override of default hashCode and equals:---------------------------------------
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dimension;
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
		Vector other = (Vector) obj;
		if (dimension != other.dimension)
			return false;
		for (int i = 0; i < n.length; i++) {
			if (n[i] != other.at(i)) {
				return false;
			}			
		}
		return true;
	}

	//Indexing-methods:-----------------------------------------------------
	
	/**
     * Indexing. Returns the value stored at position i. 
	 * @param i index
	 * @return value
	 */
	public double at(int i) {
        return n[i];
    }
    
	/**
     * Writing. Sets the value at position i to the given value. 
	 * @param i index
	 * @param val new value
	 */
	public void setAt(int i, double val) {
		n[i] = val;		
	}
	
	/**
     * Fills up vector with given parameter.
     * HUN: Vektor feltöltése a kapott értékkel.
	 * @param val value
	 */
    public void fill (double val) {
        for (int i = 0; i < dimension; i++) {
            n[i] = val;
        }
    }

    //Operations:------------------------------------------------------

    /**
     *  Multiply this vector by scalar s.
     * @param s scalar double value to multiply with.
     * @return this
     */
    public Vector multiply(double s) {
        for (int i = 0; i < dimension; i++) {
            n[i] *= s;
        }
        return this;
    }
    
    /**
     *  Divide this vector by scalar s.
     * @param s scalar double value to divide by.
     * @return this
     */
    public Vector divide(double s) {
        s = 1.0F / s;
        for (int i = 0; i < dimension; i++) {
            n[i] *= s;
        }
        return this;
    }

    /**
     *  Add vector v to this vector.
     * @param v vector to add.
     * @return this
     */
    public Vector add(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i] += v.at(i);
        }
        return this;
    }
    
    /**
     *  Subtract vector v from this vector.
     * @param v vector to subtract.
     * @return this
     */
    public Vector subtract(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i] -= v.at(i);
        }
        return this;
    }
    
    /**
     * Copy content from vector v to this vector.
     * @param v source vector.
     * @return this
     */
    public Vector copy(Vector v) {
        if (!this.equals(v)) {
            if (this.dimension != v.dimension) {
                dimension = v.dimension;
                n = new double [dimension];
            }
            for (int i = 0; i < dimension; i++) {
                n[i] = v.at(i);
            }
        }
        return this;    	
    }
    

}
