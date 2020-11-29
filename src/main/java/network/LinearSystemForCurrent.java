package main.java.network;

import main.java.math.Matrix;
import main.java.math.Vector;

/**
 * Linear system, representing a equations for electric network.
 * @author Simon Zoltán
 *<h2>The left  side of the equations:</h2>{First e columns are the columns of the incidence matrix.
 *                              The remaining part of the matrix is the cycle matrix multiplied by resistances of the edges}</b>
 *                              
 *<h2>The right side of the equations:</h2>{first e elements 0, then the sum of source voltages in that cycle.}<br>
 *
 *<pre>
 *_______________________________
 *| incidence   |  cycle matrix |   A-edges
 *|  matrix     |    * R        |   V
 *-------------------------------	=
 *|   0000000   |   U0 / 0      |   <- right side of equations.<nl>
 *-------------------------------
 *
 *</pre>
 *
 */
public class LinearSystemForCurrent extends Matrix {
	int cycleOffset;
	int noOfVariables;
	Matrix cycle;
	
	/**
	 * 
	 * @param incidence		Incidence matrix of the graph representation of network.
	 * @param cycle			Base cycle matrix of the graph representation of network.
	 * @param resistances	Vector of resistances of edges in same order as the order of edges in the incidence and cycle matrices.
	 * @param sourceVoltage	Vector of source voltages of edges in same order as the order of edges in the incidence and cycle matrices.
	 */
	public LinearSystemForCurrent(Matrix incidence, Matrix cycle, Vector resistances, Vector sourceVoltage) {
		super(incidence.row + 1, incidence.column + cycle.column);
		
		if (incidence.row != cycle.row) {
			throw new RuntimeException("Number of variables (unknown currents) not equal in given incidence and cycle matrices.");
		}
		
		this.cycle = cycle;
		
		noOfVariables = incidence.row;
		cycleOffset = incidence.column;
		
		for (int c = 0; c < incidence.column; c++) {
			for (int r = 0; r < incidence.row + 1; r++) {
				if (r < incidence.row) {
					this.setAt(r, c, incidence.at(r, c));					
				} else {
					this.setAt(r, c, 0);					
				}
			}
		}

		for (int c = 0; c < this.cycle.column; c++) {
			for (int r = 0; r < this.cycle.row + 1; r++) {
				if (r < this.cycle.row) {
					this.setAt(r, cycleOffset + c, this.cycle.at(r, c));					
				} else {
					this.setAt(r, c, 0);					
				}
			}
		}
		
		if (resistances != null) {
			updateResistances(resistances);
		}
		if (sourceVoltage != null) {
			updateSourceVoltage(sourceVoltage);
		}
	}
	
	/**
	 * Updates only the "source voltage" part of the matrix.
	 * @param sourceVoltages	{@link Vector} of source voltages. 
	 */
	public void updateSourceVoltage(Vector sourceVoltages) {
		for (int c = 0; c < this.cycle.column; c++) {
			this.setAt(this.column-1, cycleOffset + c, 0);
			for (int r = 0; r < sourceVoltages.dimension; r++) {
				if (this.cycle.at(r, c) > 0) {
					this.setAt(this.column-1, cycleOffset + c, this.at(this.column-1, cycleOffset + c) + sourceVoltages.at(r));									
				}
				else if (this.cycle.at(r, c) < 0) {
					this.setAt(this.column-1, cycleOffset + c, this.at(this.column-1, cycleOffset + c) - sourceVoltages.at(r));														
				}
			}
		}
	}
	
	/**
	 * Updates only the "resistances" part of the matrix.
	 * @param resistances {@link Vector} of resistances.
	 */
	public void updateResistances(Vector resistances) {
		for (int r = 0; r < this.column - 1; r++) {
			for (int c = 0; c < this.cycle.column; c++) {
				if (this.cycle.at(r, c) > 0) {
					this.setAt(r, cycleOffset + c, resistances.at(r));									
				}
				else if (this.cycle.at(r, c) < 0) {
					this.setAt(r, cycleOffset + c, -resistances.at(r));														
				}
				else {
					this.setAt(r, cycleOffset + c, 0);																			
				}
			}
		}		
	}
	
}
