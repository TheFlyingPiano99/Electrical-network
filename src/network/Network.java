package network;

import java.util.ArrayList;

import math.*;

public class Network {
	private ArrayList<Node> nodes;
	private ArrayList<Component> components;
	
	private Matrix linSystem;
	boolean updateGraph = true;
	boolean updateVoltage = true;

	public Network() {
		nodes = new ArrayList<Node>();
		components = new ArrayList<Component>();
		
		linSystem = null;
		
		
	}
	
	public void BuildLinearSystem () {
		
	    if (updateGraph) {
			linSystem = new Matrix(components.size() + 1, incidence.column + cycle.column);
	    }
	    
	    if (updateVoltage) {
		    Vector U0 = new Vector(nodes.size());	//Voltage sources;
	    	for (int i = 0; i < nodes.size(); i++) {
	    		linSystem.setAt(linSystem.row - 1, inc, cycle.at(r, c - nodes.get(i).getSourceVoltage()));
	    	}
	    }

	    //The left  side of the system: (First e columns are the columns of the incidence matrix.
	    //                              The remaining part of the matrix is the cycle matrix * resistance of the edges)
	    //The right side of the system: (first e elements 0, then U0, if the source is part of given cycle)
	    //_______________________________
	    //| incidence   |  cycle matrix |   V -edges
	    //|  matrix     |    * R        |   V
	    //-------------------------------
	    //|   0000000   |   U0 / 0      |   <- right side of equations.
	    //-------------------------------

	    
	    for (int c = 0; c < linSystem.column; c++) {
	        for (int r = 0; r < linSystem.row; r++) {
	            if (nodes.size() <= c) {
	                if (r < linSystem.row - 1) {
	                    linSystem.setAt(r, c, cycle.at(r, c - nodes.size()));
	                }
	            }
	            else if (nodes.size() > c) {
	                if (r == linSystem.row - 1) {
	                    linSystem.setAt(r, c, 0);
	                }
	                else {
	                    linSystem.setAt(r, c, incidence.at(r, c));
	                }
	            }
	        }
	    }
	}
	
	
	public boolean CalculateCurrent() {
		try {
			Vector current = Gauss.Eliminate(linSystem);			

			for (int i = 0; i < components.size(); i++) {
				components.get(i).setCurrent(current.at(i));
			}
		}
		catch (GaussException e) {
			return false;
		}
		
		return true;
	}
	
	public void simulate () {
		BuildLinearSystem(incidence, cycle);
		CalculateCurrent();

		//ToDo

	}
	
	public void DFS (Matrix incidence, Matrix cycle) {
		
	}
	
}



