package math;


/**
 * Vector of double values.
 * HUN: Vektor lebegőpontos értékek tárolására.
 * @author Simon Zoltán
 *
 */
public class Vector {
	private Complex n[];
    public int dimension;


	//Constructors:-------------------------------------------------
	
    public Vector(int d) {
    	dimension = d;
        n = new Complex[dimension];
    }

    public static Vector Zeros(int d) {
        Vector v = new Vector(d);
        for (int i = 0; i < d; i++) {
            v.setAt(i, new Complex(0, 0));
        }
        return v;
    }

    public Vector(Vector v) {
    	dimension = v.dimension; 
        n = new Complex[dimension];
        for (int i = 0; i < dimension; i++) {
            n[i] = v.at(i).copy();
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
	public Complex at(int i) {
        return n[i];
    }
    
	/**
     * Writing. Sets the value at position i to the given value. 
	 * @param i index
	 * @param val new value
	 */
	public void setAt(int i, Complex val) {
		n[i] = val.copy();
	}
	
	/**
     * Fills up vector with given parameter.
     * HUN: Vektor feltöltése a kapott értékkel.
	 * @param val value
	 */
    public void fill (Complex val) {
        for (int i = 0; i < dimension; i++) {
            n[i] = val.copy();
        }
    }

    //Operations:------------------------------------------------------

    /**
     *  Multiply this vector by scalar s.
     * @param s complex value to multiply with.
     */
    public void multiply(Complex s) {
        for (int i = 0; i < dimension; i++) {
            n[i].multiply(s);
        }
    }

    /**
     *  Multiply this vector by scalar s.
     * @param v vector value to multiply with.
     */
    public void multiply(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i].multiply(v.at(i));
        }
    }

    /**
     *  Divide this vector by scalar s.
     * @param s scalar double value to divide by.
     */
    public void divide(Complex s) {
        for (int i = 0; i < dimension; i++) {
            n[i].divide(s);
        }
    }

    /**
     *  Add vector v to this vector.
     * @param v vector to add.
     */
    public void add(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i].add(v.at(i));
        }
    }
    
    /**
     *  Subtract vector v from this vector.
     * @param v vector to subtract.
     */
    public void subtract(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i].subtract(v.at(i));
        }
    }
    
    /**
     * Copy content from vector v to this vector.
     * @param v source vector.
     * @return a copy of this vector
     */
    public Vector copy(Vector v) {
        if (!this.equals(v)) {
            if (this.dimension != v.dimension) {
                dimension = v.dimension;
                n = new Complex [dimension];
            }
            for (int i = 0; i < dimension; i++) {
                n[i] = v.at(i);
            }
        }
        return this;    	
    }

    // Static functions ------------------------------------------------------

    /**
     *  Multiply this vector by scalar s.
     * @param v vector value to multiply.
     * @param s complex value to multiply with.
     */
    public static Vector multiply(Vector v, Complex s) {
        Vector retVal = new Vector(v);
        for (int i = 0; i < retVal.dimension; i++) {
            retVal.at(i).multiply(s);
        }
        return retVal;
    }

    /**
     *  Multiply this vector by scalar s.
     * @param v vector value to multiply.
     * @param u vector value to multiply.
     */
    public static Vector multiply(Vector v, Vector u) {
        Vector retVal = new Vector(v);
        for (int i = 0; i < retVal.dimension; i++) {
            retVal.at(i).multiply(u.at(i));
        }
        return retVal;
    }

    /**
     *  Divide vector v by scalar s.
     * @param v vector to divide.
     * @param s complex value to divide by.
     */
    public static Vector divide(Vector v, Complex s) {
        Vector retVal = new Vector(v);
        for (int i = 0; i < retVal.dimension; i++) {
            retVal.at(i).divide(s);
        }
        return retVal;
    }

    /**
     *  Add vector u to vector v.
     * @param u vector to add.
     * @param v vector to add.
     */
    public static Vector add(Vector u, Vector v) {
        Vector retVal = new Vector(u);
        for (int i = 0; i < retVal.dimension; i++) {
            retVal.at(i).add(v.at(i));
        }
        return retVal;
    }

    /**
     *  Subtract vector v from vector u.
     * @param u vector to subtract from.
     * @param v vector to subtract.
     */
    public static Vector subtract(Vector u, Vector v) {
        Vector retVal = new Vector(u);
        for (int i = 0; i < retVal.dimension; i++) {
            retVal.at(i).subtract(v.at(i));
        }
        return retVal;
    }

}
