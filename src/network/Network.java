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
	
	int mergeProximity = 10;
	
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
	
	public Vector CalculateCurrent() {
		try {
			return Gauss.Eliminate(linSystem);			
		}
		catch (GaussException e) {
			return null;
		}
	}
	
	public void simulate () {
		//ManageLinearSystem----------------------------------
		
	    if (updateGraph || linSystem == null) {
	    	updateGraph = false;
	    	
	    	//Graph representations:
	    	Matrix incidence = new Matrix(0,0);
	    	Matrix cycle = new Matrix(0,0);
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
			Vector current = CalculateCurrent();		
			for (int i = 0; i < components.size(); i++) {
				components.get(i).setCurrent(current.at(i));
			}
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
	    
	    incidence.copyWithResize(new Matrix(components.size(), nodes.size()));
	    incidence.fill(0);
	    int noOfCycles = 0;             //First count the cycles:
	    for (int i = 0; i < components.size(); i++) {
	    	Component edge = components.get(i);
        	Node in = edge.getInput();
        	Node out = edge.getOutput();
	        if (in == previous.get(out)) {
	            incidence.setAt(i, nodes.indexOf(in), 1);
	            incidence.setAt(i, nodes.indexOf(out), -1);
	        }
	        else {
	            noOfCycles++;       	
	        }
	    }
	    
	    cycle.copyWithResize(new Matrix(components.size(), noOfCycles));
	    cycle.fill(0);
	    int currentCycle = 0;
	    for (int i = 0; i < components.size(); i++) {
	    	Component edge = components.get(i);
        	Node in = edge.getInput();
        	Node out = edge.getOutput();
	        if (in != previous.get(out)) {
	        	int dIn = depth.get(in);
	        	int dOut = depth.get(out);
	        	if (dIn > dOut) {
	        		//Backward edge
        			cycle.setAt(i, currentCycle, 1);
	        		Node step = in;
	        		while (true) {
	        			for (Component iter : step.getIncoming()) {
	        				if (iter.getInput() == previous.get(step)) {
			        			cycle.setAt(components.indexOf(iter), currentCycle, 1);
	        				}	        				
	        			}
        				step = previous.get(step);
	        			if (step == out) {
	        				break;
	        			}
	        		}
	        	}
	        	else if (dIn < dOut) {
	        		//Forward edge
        			cycle.setAt(i, currentCycle, -1);
	        		Node step = out;
	        		while (true) {
	        			for (Component iter : step.getIncoming()) {
	        				if (iter.getInput() == previous.get(step)) {
			        			cycle.setAt(components.indexOf(iter), currentCycle, 1);
	        				}	        				
	        			}
        				step = previous.get(step);
	        			if (step == in) {
	        				break;
	        			}
	        		}
	        	}
	        	else {
	        		//Cross edge
        			cycle.setAt(i, currentCycle, 1);
	        		Node step1 = in;
	        		Node step2 = out;
	        		while (true) {
	        			for (Component iter : step1.getIncoming()) {
	        				if (iter.getInput() == previous.get(step1)) {
			        			cycle.setAt(components.indexOf(iter), currentCycle, 1);
	        				}	        				
	        			}
	        			for (Component iter : step2.getIncoming()) {
	        				if (iter.getInput() == previous.get(step2)) {
			        			cycle.setAt(components.indexOf(iter), currentCycle, -1);
	        				}	        				
	        			}
        				step1 = previous.get(step1);
        				step2 = previous.get(step2);
	        			if (step1 == step2) {
	        				break;
	        			}
	        		}	        		
	        	}
	        	currentCycle++;
		    	if (currentCycle >= noOfCycles) {
		    		break;
		    	}
	        }
	    }     	    
	}

	
	//Manipulating components:---------------------------------------
	
	public void addComponent(Component component) {
		Node input = new Node();
		Node output = new Node();
		
		component.setInput(input);
		component.setOutput(output);
		
		input.addOutgoing(component);
		output.addIncoming(component);
		components.add(component);
		nodes.add(input);
		nodes.add(output);
		
	}

	
	public void removeComponent(Component component) {
		if (component.getInput().getNoOfIncoming() == 0 && component.getInput().getNoOfOutgoing() == 1) {
			nodes.remove(component.getInput());
		}
		if (component.getOutput().getNoOfIncoming() == 1 && component.getOutput().getNoOfOutgoing() == 0) {
			nodes.remove(component.getOutput());
		}
		
		if (component.getInput().isMerge() ||  component.getOutput().isMerge()) {
			updateAll();
		}
		
		components.remove(component);
	}
	
	public void grabNode(Node node) {
		if (!nodes.contains(node)) {
			throw new RuntimeException("Invalid node grabbed.");
		}
		
		node.setMerge(true);
		node.setGrabbed(true);
			
	}
	
	public void moveNode(Node node, Coordinate pos) {
		if (!nodes.contains(node)) {
			throw new RuntimeException("Invalid node moved.");
		}
		
		node.setPos(pos);
		
	}
	
	public void releaseNode(Node node) {
		if (!nodes.contains(node)) {
			throw new RuntimeException("Invalid node released.");
		}
		
		node.setGrabbed(false);
		tryToMergeNode(node);
		
	}
	
	public void grabComponent(Component component) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid component grabbed.");
		}
		
		Node input = component.getInput();
		Node output = component.getOutput();
		
		input.setMerge(true);
		output.setMerge(true);
		component.setGrabbed(true);
		
		if (input.getNoOfIncoming() > 0 || input.getNoOfOutgoing() > 1) {
			//Duplicate node;
			Node temp = new Node();
			temp.setMerge(true);
			temp.setPos(input.getPos());
			temp.addOutgoing(component);
			component.setInput(temp);
			nodes.add(temp);			
			updateAll();
		}
		if (output.getNoOfIncoming() > 1 || output.getNoOfOutgoing() > 0) {
			//Duplicate node;
			Node temp = new Node();
			temp.setMerge(true);
			temp.setPos(output.getPos());
			temp.addIncoming(component);
			component.setOutput(temp);
			nodes.add(temp);			
			updateAll();
		}		
		
	}
	
	public void moveComponent(Component component, Coordinate pos) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid component moved.");
		}
		
		Coordinate Offset = new Coordinate(MyMath.subtrackt(component.getOutput().getPos(), component.getInput().getPos()));
		
		component.getInput().setPos(pos);
		component.getOutput().setPos(MyMath.add(pos, Offset));
		
		
	}

	public void releaseComponent(Component component) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid component released.");
		}
		
		component.setGrabbed(false);

	}
	
	
	//---------------------------------------------------------------
	
	public void updateAll() {
		updateGraph = true;
		updateVoltage = true;
		updateResistance = true;
		updateCurrent = true;	
	}
	
	public boolean tryToMergeNode(Node node) {
		for (Node iter : nodes) {
			if (iter != node) {
				if (mergeProximity > MyMath.Magnitude(MyMath.subtrackt(node.getPos(), iter.getPos()))) { //Merge needed
					for (Component incoming : node.getIncoming()) {
						incoming.setOutput(iter);
						iter.addIncoming(incoming);
					}
					for (Component outgoing : node.getOutgoing()) {
						outgoing.setInput(iter);
						iter.addOutgoing(outgoing);
					}
					nodes.remove(node);
					updateAll();
				
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}

}



