package network;

import javax.management.RuntimeErrorException;

import math.*;

public class LinearSystemForCurrent extends Matrix {
	int cycleOffset;
	int noOfVariables;
	
	public LinearSystemForCurrent(Matrix incidence, Matrix cycle, Vector sourceVoltage) {
		super(incidence.row + 1, incidence.column + cycle.column);
		
		if (incidence.row != cycle.row) {
			throw new RuntimeException("Number of variables (unknown currents) not equal in given incidence and cycle matrices.");
		}
		
		noOfVariables = incidence.row;
		cycleOffset = incidence.column;				
		
		if (sourceVoltage != null) {
			for (int i = 0; i < sourceVoltage.dimension; i++) {
				this.setAt(this.row - 1, cycleOffset + i, sourceVoltage.at(i));;
			}
		}
	}
	
	public void updateSourceVoltage(Vector sourceVoltage) {
		for (int i = 0; i < sourceVoltage.dimension; i++) {
			this.setAt(this.row - 1, cycleOffset + i, sourceVoltage.at(i));;
		}
	}
	
}
