package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import math.*;

public class Network {
	private ArrayList<Node> nodes;
	private ArrayList<Component> components;
	
	private LinearSystemForCurrent linSystem;
	boolean updateGraph = true;
	boolean updateVoltage = true;
	boolean updateResistance = true;
	boolean updateCurrent = true;
	

	public Network() {
		nodes = new ArrayList<Node>();
		components = new ArrayList<Component>();
		
		linSystem = null;
		
		
	}

	private Vector gatherResistances() {
    	Vector resistances = new Vector(components.size());
    	for (int i = 0; i < components.size(); i++) {
    		resistances.setAt(i, components.get(i).getResistance());
    	}		
    	return resistances;
	}
	
	public Vector gatherSourceVoltages() {
    	Vector sourceVoltages = new Vector(components.size());
    	for (int i = 0; i < components.size(); i++) {
    		sourceVoltages.setAt(i, components.get(i).getSourceVoltage());
    	}
    	return sourceVoltages;
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
		//ManageLinearSystem----------------------------------
		
	    if (updateGraph || linSystem == null) {
	    	updateGraph = false;
	    	
	    	//Graph representations:
	    	Matrix incidence = null, cycle = null;
	    	try {
		    	DFS(incidence, cycle);
	    	
		    	//Parameters:
		    	Vector resistances = gatherResistances();
			    Vector sourceVoltage = gatherSourceVoltages(); //Voltage sources;
			    
		    	//Create system:
		    	linSystem = new LinearSystemForCurrent(incidence, cycle, resistances, sourceVoltage);
			} catch (RuntimeException e) {
				updateCurrent = false;				
			}
	    }
	    else {
	    	if (updateResistance) {
	    		updateResistance = false;
		    	linSystem.updateResistances(gatherResistances());
	    	}
	    	if (updateVoltage) {	    
	    		updateVoltage = false;
		    	linSystem.updateSourceVoltage(gatherSourceVoltages());
		    }
	    }
	    //----------------------------------------------
	    if (updateCurrent) {
			updateCurrent = false;
			CalculateCurrent();		
		}

	}
	
	public void DFS (Matrix incidence, Matrix cycle) {
		if (nodes.isEmpty()) {
			throw new RuntimeException("No nodes to work with.");
		}
		
	    Node s = nodes.get(0);  //Starting vertex

	    Map<Node, Integer> depth = new HashMap<Node, Integer>();
	    Map<Node, Integer> finish = new HashMap<Node, Integer>();
	    Map<Node, Node> previous = new HashMap<Node, Node>();
	    Node current;
	    int GreatestDepth;
	    int GreatestFinish;

	    ///Initialization:
	    depth.put(s, 1);
        previous.put(s, null);
        finish.put(s, -1);

	    ///Using -1 as undefined value:
	    previous.put(s, null);
	    for (Node iter : nodes) {
	        if (iter != s) {
	            depth.put(iter, -1);
		        previous.put(iter, null);
	        }
	    }

	    GreatestDepth = 1;
	    GreatestFinish = 0;
	    current = s;

	    ///Cycle:
	    boolean run = true;
	    while (run) {
	        ///Finding adjacent vertex with (*) depth:
	        Node v = current;
	        for (Component iter : current.getOutgoing()) {
	            if (-1 == depth.get(iter.getOutput())) {
	                v = iter.getOutput();
	                break;
	            }
	        }
	        if (current != v) { //Found adjacent vertex with (*) depth
	            GreatestDepth++;
	            depth.put(v, GreatestDepth);
	            previous.put(v, current);
	            current = v;
	        } else {
	            GreatestFinish++;
	            finish.put(current, GreatestFinish);
	            if (null != previous.get(current)) {
	                current = previous.get(current);
	            } else {
	                ///Finding vertex with (*) depth:
	                v = current;
	                for (Node iter : nodes) {
	                    if (-1 == depth.get(iter)) {
	                        v = iter;
	                        break;
	                    }
	                }
	                if (current != v) { //Found adjacent vertex with (*) depth
	                    current = v;
	                } else {
	                    run = false;
	                }
	            }
	        }
	    }
	    incidence = new Matrix(components.size(), nodes.size());
	    incidence.fill(0);
	    for (int i = 0; i < nodes.size(); i++) {
	        if (null != previous.get(nodes.get(i))) {
	            incidence.setAt(i, nodes.indexOf(previous.get(nodes.get(i))), 1);
	            incidence.setAt(i, i, -1);
	        }
	    }

	    //Getting fundamental base cycles:
	    int noOfCycles = 0;             //First count the cycles:
	    for (Node iter : nodes) {
	        if (null == previous.get(iter)) {
	            noOfCycles++;
	        }
	    }
	    
	    cycle = new Matrix(components.size(), noOfCycles);
	    cycle.fill(0);
	    for (int i = 0; i < nodes.size(); i++) {     //Then fill cycle matrix
	        if (null == previous.get(nodes.get(i))) {
	            cycle.setAt(i, i, 1);
	            for (int j = 0; j < nodes.size(); j++) {
	                if (null != previous.get(nodes.get(j))) {
	                    cycle.setAt(j, i, 1);
	                }
	            }
	        }
	        //TODO
	    }
	}
	
	public void addComponent(Component component) {
		Node input = new Node();
		Node output = new Node();
		
		component.setInput(input);
		component.setOutput(output);
		
		components.add(component);
		nodes.add(input);
		nodes.add(output);
		
	}
	
	public void removeComponent(Component component) {
		components.remove(component);
	}
}



