package network;

import math.Complex;
import math.Matrix;
import math.Vector;

/**
 * Linear system, representing equations for network in matrix form.
 * HUN: Lineáris rendszer, ami a hálózat egyenletrendszerét írja le mátrix formában.
 * @author Simon Zoltán
 * 
 *<h2>The "left  side" of the equations:</h2>{First e columns are the columns of the incidence matrix.
 *                              The remaining part of the matrix is the cycle matrix multiplied by resistances of the edges}<br>
 *                              
 *<h2>The "right side" of the equations:</h2>{first e elements 0, then the sum of source voltages in that cycle.}<br>
 *
 *<pre>
 *_______________________________
 *| incidence   |  cycle matrix |   A-edges
 *|  matrix     |    * R        |   V
 *-------------------------------	=
 *| ground|0000 |   U0 / 0      |   &lt;- "right side" of equations.
 *-------------------------------
 *
 *</pre>
 *
 */
public class LinearSystemForCurrent extends Matrix {
	int cycleOffset;
	int noOfEdges;
	Matrix cycle;
	
	/**
	 * Constructor
	 * @param incidence		Incidence matrix of the graph representation of network.
	 * @param cycle			Base cycle matrix of the graph representation of network.
	 * @param impedance	Vector of impedance of edges in same order as the order of edges in the incidence and cycle matrices.
	 * @param sourceVoltage	Vector of source voltages of edges in same order as the order of edges in the incidence and cycle matrices.
	 * @param inputCurrents Vector of currents inputed to individual vertices.
	 */
	public LinearSystemForCurrent(Matrix incidence, Matrix cycle, Vector impedance, Vector sourceVoltage, Vector inputCurrents) {
		super(incidence.row + 1, incidence.column + cycle.column);
		
		if (incidence.row != cycle.row) {
			throw new RuntimeException("Number of variables (unknown currents) not equal in given incidence and cycle matrices.");
		}
		
		this.cycle = cycle;
		
		noOfEdges = incidence.row;
		cycleOffset = incidence.column;	//Number of vertices.
		
		for (int c = 0; c < incidence.column; c++) {
			for (int r = 0; r < incidence.row + 1; r++) {
				if (r < incidence.row) {
					this.setAt(r, c, incidence.at(r, c));					
				} else {
					this.setAt(r, c, new Complex(0, 0));
				}
			}
		}

		for (int c = 0; c < this.cycle.column; c++) {
			for (int r = 0; r < this.cycle.row + 1; r++) {
				if (r < this.cycle.row) {
					this.setAt(r, cycleOffset + c, this.cycle.at(r, c));					
				} else {
					this.setAt(r, c, new Complex(0, 0));
				}
			}
		}
		
		if (impedance != null) {
			updateImpedances(impedance);
		}
		if (sourceVoltage != null) {
			updateSourceVoltage(sourceVoltage);
		}
		if (inputCurrents != null) {
			updateInputCurrents(inputCurrents);
		}
	}
	
	
	/**
	 * Updates only the "source voltage" part of the matrix.
	 * HUN: Frissíti a mátrix feszültésforrásokat leíró részét.
	 * @param sourceVoltages	{@link Vector} of source voltages. 
	 */
	public void updateSourceVoltage(Vector sourceVoltages) {
		for (int c = 0; c < this.cycle.column; c++) {
			Complex sumOfVoltages = new Complex(0, 0);
			for (int r = 0; r < sourceVoltages.dimension; r++) {
				if (this.cycle.at(r, c).compareTo(new Complex(0, 0)) > 0) {
					sumOfVoltages.add(sourceVoltages.at(r));
				}
				else if (this.cycle.at(r, c).compareTo(new Complex(0, 0)) < 0) {
					sumOfVoltages.subtract(sourceVoltages.at(r));
				}
			}
			this.setAt(this.row-1, cycleOffset + c, sumOfVoltages);
		}
	}
	
	
	/**
	 * Updates only the "impedances" part of the matrix.
	 * HUN: Frissíti a mátrix impedanciákat leíró részét.
	 * @param impedances {@link Vector} of resistances.
	 */
	public void updateImpedances(Vector impedances) {
		for (int c = 0; c < this.cycle.column; c++) {
			for (int r = 0; r < impedances.dimension; r++) {
				if (this.cycle.at(r, c).getRe() > 0) {
					this.setAt(r, cycleOffset + c, impedances.at(r));
				}
				else if (this.cycle.at(r, c).getRe() < 0) {
					this.setAt(r, cycleOffset + c, impedances.at(r).negate());
				}
				else {
					this.setAt(r, cycleOffset + c, new Complex(0, 0));
				}
			}
		}		
	}
	
	
	/**
	 * Updates only the "inputCurrents" part of the matrix. (Left half of the bottom row.)
	 * Also updates current flowing into ground. (sum of inputs * -1) 
	 * @param inputCurrent
	 */
	public void updateInputCurrents(Vector inputCurrent) {
		Complex sum = new Complex(0, 0);
		for (int c = 1; c < cycleOffset; c++) {
			this.setAt(this.row-1, c, inputCurrent.at(c));
			sum.add(inputCurrent.at(c));
		}
		this.setAt(this.row-1, 0, sum.negate());
	}
	
}
