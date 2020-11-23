package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import math.*;

public class Network {
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	
	private ArrayList<ComponentNode> componentNodes;
	private ArrayList<Component> components;
	
	private LinearSystemForCurrent linSystem;
	boolean updateGraph = true;
	boolean updateVoltage = true;
	boolean updateResistance = true;
	boolean updateCurrent = true;
	
	int mergeProximity = 10;
	
	public Network() {
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		
		componentNodes = new ArrayList<ComponentNode>();
		components = new ArrayList<Component>();

		linSystem = null;
		
	}

	private Vector gatherResistances() {
    	Vector resistances = new Vector(edges.size());
    	for (int i = 0; i < edges.size(); i++) {
    		resistances.setAt(i, edges.get(i).getResistance());
    	}		
    	return resistances;
	}
	
	public Vector gatherSourceVoltages() {
    	Vector sourceVoltages = new Vector(edges.size());
    	for (int i = 0; i < edges.size(); i++) {
    		sourceVoltages.setAt(i, edges.get(i).getSourceVoltage());
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
			for (int i = 0; i < edges.size(); i++) {
				edges.get(i).setCurrent(current.at(i));
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
	        for (Edge iter : current.getOutgoing()) {
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
	    
	    incidence.copyWithResize(new Matrix(edges.size(), nodes.size()));
	    incidence.fill(0);
	    int noOfCycles = 0;             //First count the cycles:
	    for (int i = 0; i < edges.size(); i++) {
	    	Edge edge = edges.get(i);
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
	    
	    cycle.copyWithResize(new Matrix(edges.size(), noOfCycles));
	    cycle.fill(0);
	    int currentCycle = 0;
	    for (int i = 0; i < edges.size(); i++) {
	    	Edge edge = edges.get(i);
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
	        			for (Edge iter : step.getIncoming()) {
	        				if (iter.getInput() == previous.get(step)) {
			        			cycle.setAt(edges.indexOf(iter), currentCycle, 1);
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
	        			for (Edge iter : step.getIncoming()) {
	        				if (iter.getInput() == previous.get(step)) {
			        			cycle.setAt(edges.indexOf(iter), currentCycle, 1);
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
	        			for (Edge iter : step1.getIncoming()) {
	        				if (iter.getInput() == previous.get(step1)) {
			        			cycle.setAt(edges.indexOf(iter), currentCycle, 1);
	        				}	        				
	        			}
	        			for (Edge iter : step2.getIncoming()) {
	        				if (iter.getInput() == previous.get(step2)) {
			        			cycle.setAt(edges.indexOf(iter), currentCycle, -1);
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
	
	public void addEdge(Edge edge) {
		Node input = new Node();
		Node output = new Node();
		
		edge.setInput(input);
		edge.setOutput(output);
		
		input.addOutgoing(edge);
		output.addIncoming(edge);
		edges.add(edge);
		nodes.add(input);
		nodes.add(output);
		
	}

	
	public void removeEdge(Edge edge) {
		if (edge.getInput().getNoOfIncoming() == 0 && edge.getInput().getNoOfOutgoing() == 1) {
			nodes.remove(edge.getInput());
		}
		if (edge.getOutput().getNoOfIncoming() == 1 && edge.getOutput().getNoOfOutgoing() == 0) {
			nodes.remove(edge.getOutput());
		}
		
		updateAll();
		
		edges.remove(edge);
	}
	
	public void addComponent (Component component) {
		component.setParent(this);
		component.build();
		components.add(component);
	}

	
	
	public void grabComponentNode(ComponentNode componentNode) {
		if (!componentNodes.contains(componentNode)) {
			throw new RuntimeException("Invalid node grabbed.");
		}
		componentNode.grab();
			
	}
	
	public void moveComponentNode(ComponentNode componentNode, Coordinate pos) {
		if (!componentNodes.contains(componentNode)) {
			throw new RuntimeException("Invalid node moved.");
		}
		componentNode.move(pos);
	}
	
	public void releaseComponentNode(ComponentNode componentNode) {
		if (!componentNodes.contains(componentNode)) {
			throw new RuntimeException("Invalid node released.");
		}
		componentNode.release();
		tryToMergeComponentNode(componentNode);
	}
	
	public void grabComponent(Component component) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid component grabbed.");
		}
		
		ComponentNode input = component.getInput();
		ComponentNode output = component.getOutput();
		
		input.setMerge(true);
		output.setMerge(true);
		component.setGrabbed(true);
		
		if (input.getNoOfIncoming() > 0 || input.getNoOfOutgoing() > 1) {
			//Duplicate node;
			ComponentNode temp = new ComponentNode(this);
			temp.setMerge(true);
			temp.setPos(input.getPos());
			temp.addOutgoing(component);
			component.setInput(temp);
			componentNodes.add(temp);			
			updateAll();
		}
		if (output.getNoOfIncoming() > 1 || output.getNoOfOutgoing() > 0) {
			//Duplicate node;
			ComponentNode temp = new ComponentNode(this);
			temp.setMerge(true);
			temp.setPos(output.getPos());
			temp.addIncoming(component);
			component.setOutput(temp);
			componentNodes.add(temp);		
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

	public void releaseComponent(Edge component) {
		if (!edges.contains(component)) {
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
	
	public void mergeNodes(Node persistant, Node merge)  {
		for (Edge incoming : merge.getIncoming()) {
			incoming.setOutput(persistant);
			persistant.addIncoming(incoming);
		}
		for (Edge outgoing : merge.getOutgoing()) {
			outgoing.setInput(persistant);
			persistant.addOutgoing(outgoing);
		}
		nodes.remove(merge);
		updateAll();
	}

	public boolean tryToMergeComponentNode(ComponentNode componentNode) {
		for (ComponentNode iter : componentNodes) {
			if (iter != componentNode) {
				if (mergeProximity > MyMath.Magnitude(MyMath.subtrackt(componentNode.getPos(), iter.getPos()))) { //Merge needed
					for (Component incoming : componentNode.getIncoming()) {
						incoming.setOutput(iter);
						iter.addIncoming(incoming);
					}
					for (Component outgoing : componentNode.getOutgoing()) {
						outgoing.setInput(iter);
						iter.addOutgoing(outgoing);
					}
					
					componentNode.destroy();
					if (componentNode.getNode() != null && iter.getNode() != null) {
						mergeNodes(iter.getNode(), componentNode.getNode());
					}
					componentNodes.remove(componentNode);
					updateAll();
				
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}
	
	public ArrayList<ComponentNode> getComponentNodes() {
		return componentNodes;
	}
}



