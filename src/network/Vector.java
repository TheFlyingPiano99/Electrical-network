package network;

public class Vector {
    private float n[];
    public int dimension;

    public Vector(int d) {
    	dimension = d;
        n = new float[dimension];
    }

    public Vector(Vector v) {
    	dimension = v.dimension; 
        n = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            n[i] = v.at(i);
        }
    }

    public float at(int i) {
        return n[i];
    }
    
	public void setAt(int i, float val) {
		n[i] = val;		
	}
	
    void fill (float val) {
        for (int i = 0; i < dimension; i++) {
            n[i] = val;
        }
    }

    public Vector multiply(float s) {
        for (int i = 0; i < dimension; i++) {
            n[i] *= s;
        }
        return this;
    }
    
    public Vector divide(float s) {
        s = 1.0F / s;
        for (int i = 0; i < dimension; i++) {
            n[i] *= s;
        }
        return this;
    }

    public Vector add(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i] += v.at(i);
        }
        return this;
    }
    
    public Vector subtrackt(Vector v) {
        for (int i = 0; i < dimension; i++) {
            n[i] -= v.at(i);
        }
        return this;
    }
    
    public Vector copy(Vector v) {
        if (!this.equals(v)) {
            if (this.dimension != v.dimension) {
                dimension = v.dimension;
                n = new float [dimension];
            }
            for (int i = 0; i < dimension; i++) {
                n[i] = v.at(i);
            }
        }
        return this;    	
    }
    

}
