package network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import math.*;

public class Network {
	private ArrayList<Vertex> vertices;
	private ArrayList<Edge> edges;
	
	private ArrayList<ComponentNode> componentNodes;
	private ArrayList<Component> components;
	
	private LinearSystemForCurrent linSystem;
	boolean updateGraph = true;
	boolean updateVoltage = true;
	boolean updateResistance = true;
	boolean updateNetwork = true;
	
	int mergeProximity = 8;
	
	public Network() {
		vertices = new ArrayList<Vertex>();
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
	
	public void simulate (Duration duration) {
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
				updateNetwork = false;				
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
	    if (updateNetwork) {
			updateNetwork = false;
			Vector current = CalculateCurrent();		
			for (int i = 0; i < edges.size(); i++) {
				edges.get(i).setCurrent(current.at(i));

			}
			
			for (Component component : components) {
				component.update(duration);
			}
			
	    }

	}
	
	public void DFS (Matrix incidence, Matrix cycle) {
		if (vertices.isEmpty()) {
			throw new RuntimeException("No nodes to work with.");
		}
		
	    Vertex s = vertices.iterator().next();  //Starting vertex

	    Map<Vertex, Integer> depth = new HashMap<Vertex, Integer>();
	    Map<Vertex, Integer> finish = new HashMap<Vertex, Integer>();
	    Map<Vertex, Vertex> previous = new HashMap<Vertex, Vertex>();
	    Vertex current;
	    int GreatestDepth;
	    int GreatestFinish;

	    ///Initialization:
	    depth.put(s, 1);
        previous.put(s, null);
        finish.put(s, -1);

	    ///Using -1 as undefined value:
	    previous.put(s, null);
	    for (Vertex iter : vertices) {
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
	        Vertex v = current;	 
	        for (Vertex iter : current.getOutgoing().keySet()) {
	            if (depth.get(iter) != null && -1 == depth.get(iter)) {
	                v = iter;
	        		break;
	            }
	        }
			if (current == v) {
				//Also search in reversed edges:
		        for (Vertex iter : current.getIncoming().keySet()) {
		            if (depth.get(iter) != null && -1 == depth.get(iter)) {
		                v = iter;
		        		break;
		            }
		        }
			}
	        if (current != v) {		//Found adjacent vertex with (*) depth
	            GreatestDepth++;
	            depth.put(v, GreatestDepth);
	            previous.put(v, current);	            		            	
	            current = v;
	        } else {
	            GreatestFinish++;
	            finish.put(current, GreatestFinish);
	            if (null != previous.get(current)) {	//Backtracking
	                current = previous.get(current);
	            } else {
	                ///Finding vertex with (*) depth:
	                v = current;
	                for (Vertex iter : vertices) {
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
	    
	    incidence.copyWithResize(new Matrix(edges.size(), vertices.size()));
	    incidence.fill(0);
	    int noOfCycles = 0;             //First count the cycles:
	    for (int i = 0; i < edges.size(); i++) {
	    	Edge edge = edges.get(i);
            incidence.setAt(i, vertices.indexOf(edge.getInput()), 1);
            incidence.setAt(i, vertices.indexOf(edge.getOutput()), -1);
	        if (edge.getOutput() != previous.get(edge.getInput()) &&
	        		edge.getInput() != previous.get(edge.getOutput())) {
            	noOfCycles++;       	
	        }
	    }
	    
	    
	    cycle.copyWithResize(new Matrix(edges.size(), noOfCycles));
	    cycle.fill(0);
	    int currentCycle = 0;
	    for (int i = 0; i < edges.size() && currentCycle < noOfCycles; i++) {

	    	Edge edge = edges.get(i);
	        if (edge.getOutput() != previous.get(edge.getInput()) &&
	        		edge.getInput() != previous.get(edge.getOutput())) {
            	Vertex in = edge.getInput();
            	Vertex out = edge.getOutput();
            	int dIn = depth.get(in);
	        	int dOut = depth.get(out);
	        	if (dIn > dOut) {
	        		//Backward edge
        			cycle.setAt(i, currentCycle, 1);
	        		Vertex step = in;
	        		while (step != out) {
	        			if (previous.get(step) == null) {
	        				System.out.println("Null.");
	        			}
	        			Edge e = step.getIncoming().get(previous.get(step));
	        			if (e != null) {
		        			cycle.setAt(edges.indexOf(e), currentCycle, 1);	        				
	        			}
	        			else {
	        				e = step.getOutgoing().get(previous.get(step));
		        			cycle.setAt(edges.indexOf(e), currentCycle, -1);	        					        				
	        			}
	        				        			
        				step = previous.get(step);
	        		}
	        	}
	        	else if (dIn < dOut) {
	        		//Forward edge
        			cycle.setAt(i, currentCycle, -1);
	        		Vertex step = out;
	        		while (step != in) {
	        			Edge e = step.getIncoming().get(previous.get(step));
	        			if (e != null) {
		        			cycle.setAt(edges.indexOf(e), currentCycle, 1);	        				
	        			}
	        			else {
	        				e = step.getOutgoing().get(previous.get(step));
		        			cycle.setAt(edges.indexOf(e), currentCycle, -1);	        					        				
	        			}

	        			step = previous.get(step);
	        		}
	        	}
	        	else {
	        		//Cross edge
	        		throw new RuntimeException("Cross found despite of DFS on undirected graph!");
	        		
	        		/*
	        		cycle.setAt(i, currentCycle, 1);
	        		Vertex step1 = in;
	        		Vertex step2 = out;
	        		while (step1 != step2) {
	        			cycle.setAt(edges.indexOf(previous.get(step1)), currentCycle, 1);
	        			cycle.setAt(edges.indexOf(previous.get(step2)), currentCycle, -1);
        				step1 = previous.get(step1);
        				step2 = previous.get(step2);
	        		}
	        		*/	        		
	        	}
	        	currentCycle++;
	        }
	    }     	    
	}

	
	//Access edges and nodes:-----------------------------------------------------------------------------------
	//No other method allowed to manipulate edges nor nodes from outside the network. 
	
	public void addEdge(Edge edge) {
		Vertex input = new Vertex();
		Vertex output = new Vertex();
		
		edge.setInput(input);
		edge.setOutput(output);
		
		input.addOutgoing(output, edge);
		output.addIncoming(input, edge);
		
		edges.add(edge);
		vertices.add(input);
		vertices.add(output);
		
	}
	
	public void removeEdge(Edge edge) {
		if (edge.getInput().getNoOfIncoming() == 0 && edge.getInput().getNoOfOutgoing() == 1) {
			vertices.remove(edge.getInput());
		}
		else {
			edge.getInput().removeOutgoing(edge.getOutput());
		}
		if (edge.getOutput().getNoOfIncoming() == 1 && edge.getOutput().getNoOfOutgoing() == 0) {
			vertices.remove(edge.getOutput());
		}
		else {
			edge.getOutput().removeIncoming(edge.getInput());
		}
		
		setUpdateAll();
		
		edges.remove(edge);
	}
		
	public void mergeVertices(Vertex persistant, Vertex merge)  {
		if (persistant != merge) {
			for (Map.Entry<Vertex, Edge> incoming : merge.getIncoming().entrySet()) {
				incoming.getKey().removeOutgoing(merge);
				incoming.getKey().addOutgoing(persistant, incoming.getValue());				
				
				incoming.getValue().setOutput(persistant);
				persistant.addIncoming(incoming.getKey(), incoming.getValue());
			}
			for (Map.Entry<Vertex, Edge> outgoing : merge.getOutgoing().entrySet()) {
				outgoing.getKey().removeIncoming(merge);
				outgoing.getKey().addIncoming(persistant, outgoing.getValue());
				
				outgoing.getValue().setInput(persistant);
				persistant.addOutgoing(outgoing.getKey(), outgoing.getValue());
			}
			vertices.remove(merge);
			setUpdateAll();
		}
	}

	//-------------------------------------------------------------------------------------------------
	//Manipulating components:---------------------------------------

	public void addComponent (Component component) {
		component.setParent(this);
		component.build();
		components.add(component);
	}

	public void removeComponent (Component component) {
		component.destroy();
		components.remove(component);
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

	//---------------------------------------------------------------
	//ToDo
	
	//---------------------------------------------------------------
	
	/**
	 * When everything need's to be updated.   
	 */
	public void setUpdateAll() {
		updateGraph = true;
		updateVoltage = true;
		updateResistance = true;
		updateNetwork = true;	
	}
	
	
	
	public boolean tryToMergeComponentNode(ComponentNode componentNode) {
		for (ComponentNode iter : componentNodes) {
			if (iter != componentNode) {
				if (mergeProximity > MyMath.Magnitude(MyMath.subtrackt(componentNode.getPos(), iter.getPos()))) {					
					if (!componentNode.isNeighbouring(iter)) {
						//Merge needed:
						for (Component incoming : componentNode.getIncoming()) {
							incoming.setOutput(iter);
							iter.addIncoming(incoming);
						}
						for (Component outgoing : componentNode.getOutgoing()) {
							outgoing.setInput(iter);
							iter.addOutgoing(outgoing);
						}
						
						if (componentNode.getVertexBinding() != null && iter.getVertexBinding() != null) {
							mergeVertices(iter.getVertexBinding(), componentNode.getVertexBinding());
						}
						else {
							throw new RuntimeException("ComponentNode does not contain reference to actual node.");
						}
						componentNode.destroy();

						componentNodes.remove(componentNode);
						setUpdateAll();
					
						return true;						
					}
					else {
						throw new RuntimeException("Neighbours!");
					}
				}
			}
		}
		return false;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}
	
	public ArrayList<ComponentNode> getComponentNodes() {
		return componentNodes;
	}
	
	public ComponentNode getNodeAtPos(Coordinate pos) {
		for (ComponentNode iter : componentNodes) {
			if (MyMath.Magnitude(MyMath.subtrackt(iter.getPos(), pos)) < 10) {
				return iter;
			}
		}
		
		return null;
	}
	
	public void save(String fileName) {
		try {

			StringBuilder builder = new StringBuilder();			
			for (Component component : components) {
				component.save(builder);
			}
			
			FileOutputStream output = new FileOutputStream(fileName);
			OutputStreamWriter writer = new OutputStreamWriter(output);
			writer.write(builder.toString());
			writer.close();
		
		} catch (Exception e) {
			throw new RuntimeException("Save error!", e);
		}
	
	}
	
	public void load(String fileName) {
		try {
			FileReader input = new FileReader(fileName);
			
			BufferedReader reader = new BufferedReader(input);
			
			Map<String, Class<?>> type = new HashMap<>();
			type.put("VoltageSource", VoltageSource.class);
			type.put("Wire", Wire.class);
			type.put("Resistance", Resistance.class);
			
			String row;
			while (null != (row = reader.readLine())) {
				Scanner scanner = new Scanner(row);
				if (scanner.hasNext("[]+")) {
					String t = "";
					Component comp = (Component) type.get(t).getConstructor().newInstance();				

					this.addComponent(comp);				

					comp.load(scanner);
					
				}
			}
			reader.close();

			setUpdateAll();	
			
		} catch (Exception e) {
			throw new RuntimeException("Load error!", e);
		}
			
	}

}



