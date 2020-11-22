package network;

import javax.management.RuntimeErrorException;

import math.*;


//The left  side of the system: (First e columns are the columns of the incidence matrix.
//                              The remaining part of the matrix is the cycle matrix * resistance of the edges)
//The right side of the system: (first e elements 0, then U0, if the source is part of given cycle)
//_______________________________
//| incidence   |  cycle matrix |   V -edges
//|  matrix     |    * R        |   V
//-------------------------------
//|   0000000   |   U0 / 0      |   <- right side of equations.
//-------------------------------
public class LinearSystemForCurrent extends Matrix {
	int cycleOffset;
	int noOfVariables;
	
	public LinearSystemForCurrent(Matrix incidence, Matrix cycle, Vector resistances, Vector sourceVoltage) {
		super(incidence.row + 1, incidence.column + cycle.column);
		
		if (incidence.row != cycle.row) {
			throw new RuntimeException("Number of variables (unknown currents) not equal in given incidence and cycle matrices.");
		}
		
		noOfVariables = incidence.row;
		cycleOffset = incidence.column;				
		
		if (resistances != null) {
			updateResistances(resistances);
		}
		if (sourceVoltage != null) {
			updateSourceVoltage(sourceVoltage);
		}
	}
	
	public void updateSourceVoltage(Vector sourceVoltages) {
		for (int c = cycleOffset; c < this.column; c++) {
			this.setAt(this.column-1, c, 0);
			for (int r = 0; r < sourceVoltages.dimension; r++) {
				if (this.at(r, c) > 0) {
					this.setAt(this.column-1, c, this.at(this.column-1, c) + sourceVoltages.at(r));									
				}
				else if (this.at(r, c) < 0) {
					this.setAt(this.column-1, c, this.at(this.column-1, c) - sourceVoltages.at(r));														
				}
			}
		}
	}
	
	public void updateResistances(Vector resistances) {
		for (int r = 0; r < this.column - 1; r++) {
			for (int c = cycleOffset; c < this.column; c++) {
				if (this.at(r, c) > 0) {
					this.setAt(r, c, resistances.at(r));									
				}
				else if (this.at(r, c) < 0) {
					this.setAt(r, c, -resistances.at(r));														
				}
			}
		}		
	}
	
}
