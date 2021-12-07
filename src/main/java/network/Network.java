package main.java.network;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.canvas.GraphicsContext;
import main.java.math.Coordinate;
import main.java.math.Gauss;
import main.java.math.GaussException;
import main.java.math.Matrix;
import main.java.math.MyMath;
import main.java.math.Vector;


/**
 * The model of the electric network.
 * HUN: Az elektromos hálózat modellje.
 * @author Simon Zoltán
 *
 */
public class Network {
	
	/**
	 * Vertices of the graph representation.
	 * HUN: A gráf-reprezentáció csúcsai.
	 */
	private ArrayList<Vertex> vertices;
	
	/**
	 * Edges of the grapf representation.
	 * HUN: A gráf-reprezentáció élei.
	 */
	private ArrayList<Edge> edges;
	
	/**
	 * Nodes of the network.
	 * HUN: A hálózat csomópontjai.
	 */
	private ArrayList<ComponentNode> componentNodes;
	
	/**
	 * Components of the network.
	 * HUN: A hálózat komponensei.
	 */
	private ArrayList<Component> components;
	
	/**
	 * Matrix representation of the network.
	 * A hálózat mátrix reprezentációja.
	 */
	private LinearSystemForCurrent linSystem;
	
	//Flags:
	boolean updateGraph = true;
	boolean updateVoltage = true;
	boolean updateResistance = true;
	boolean updateCurrent = true;
	boolean updateInputCurrent = true;
	private Component selected = null;
	private boolean snapToGrid = true;
	private boolean validNetwork = false;
	
	private int gridSize = 30;
	//--------------------------------------------------
	
	/**
	 * Distance of merging and grabbing.
	 * HUN: Az összeolvasztás és megfogás távolsága.
	 */
	int closeProximity = (int)(gridSize * 0.4);
	
	//Constructor:------------------------------------------------------
	
	public Network() {
		vertices = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		
		componentNodes = new ArrayList<ComponentNode>();
		components = new ArrayList<Component>();

		linSystem = null;
		
		//Create ground-node (index 0):
		vertices.add(new Vertex());
	}

	//--------------------------------------------------------------------
	
	/**
	 * Returns the resistance of all the edges.
	 * HUN: Visszaad egy vektort amiben az összes gráf-élhez rendelt ellenállás értékei vannak felsorolva
	 * az élek, "edges" listában szereplő sorrendje szerint. 
	 * @return	Vector of resistances. The order of elements of the vector is the same as the order of the edges in private ArrayList&lt;Edge&gt; edges.
	 */
	private Vector gatherResistances() {
    	Vector resistances = new Vector(edges.size());
    	for (int i = 0; i < edges.size(); i++) {
    		resistances.setAt(i, edges.get(i).getResistance());
    	}		
    	return resistances;
	}
	
	/**
	 * Returns the source voltages of all the edges.
	 * HUN: Visszaad egy vektort amiben az összes gráf-élhez rendelt feszültségforrás értékei vannak felsorolva
	 * az élek, "edges" listában szereplő sorrendje szerint. 
	 * @return Vector of source voltages. The order of elements of the vector is the same as the order of the edges in private ArrayList&lt;Edge&gt; edges.
	 */
	private Vector gatherSourceVoltages() {
    	Vector sourceVoltages = new Vector(edges.size());
    	for (int i = 0; i < edges.size(); i++) {
    		sourceVoltages.setAt(i, edges.get(i).getSourceVoltage());
    	}
    	return sourceVoltages;
	}
	
	/**
	 * 
	 * @return	Vector of currents inputed to individual vertices.
	 */
	private Vector gatherInputCurrent() {
    	Vector inputCurrents = new Vector(vertices.size());
    	for (int i = 0; i < vertices.size(); i++) {
    		inputCurrents.setAt(i, vertices.get(i).getInputCurrent());
    	}
    	return inputCurrents;
	}

	/**
	 * Uses Gaussian elimination, to get the current in all edges.
	 * HUN: Gauss-elimináció segítségével kiszámolja a gráf-élekhez tartozó áramot. 
	 * Visszaad egy vektort amiben az összes gráf-élhez rendelt áram értékei vannak felsorolva
	 * az élek, "edges" listában szereplő sorrendje szerint. 
	 * @return Vector of currents. The order of elements of the vector is the same as the order of the edges in private ArrayList&lt;Edge&gt; edges.
	 */
	private Vector CalculateCurrent() {
		try {
			return Gauss.Eliminate(linSystem);			
		}
		catch (GaussException e) {
			return null;
		}
	}
	
	/**
	 * Implements the physical behavior of the network. Calculates current resistance and voltage levels.
	 * HUN: A hálózat fizikai viselkedését valósítja meg. Kiszámolja az áram, ellenállás és feszültség szinteket.  
	 * @param deltaTime	The time spent since the last call of this method.
	 */
	public void simulate (Duration deltaTime) {
		if (null == deltaTime) {
			deltaTime = new Duration(0);
		}
		
		//ManageLinearSystem:
		
	    if (updateGraph || linSystem == null) {
	    	    	
	    	//Graph representations:
	    	Matrix incidence = new Matrix(0,0);
	    	Matrix cycle = new Matrix(0,0);
	    	try {
		    	DFS(incidence, cycle);
	    	
		    	//Parameters:
		    	Vector resistances = gatherResistances();
			    Vector sourceVoltage = gatherSourceVoltages();
			    Vector inputCurrents = gatherInputCurrent();
			    
		    	//Create system:
		    	linSystem = new LinearSystemForCurrent(incidence, cycle, resistances, sourceVoltage, inputCurrents);

		    	//Disable flags:
		    	updateGraph = false;
		    	updateResistance = false;
		    	updateVoltage = false;
		    	updateInputCurrent = false;
		    	
		    	//Set flag:
		    	updateCurrent = true;
		    	
		    	
			} catch (RuntimeException e) {
				updateCurrent = false;
			}
	    }
	    else {
	    	if (updateResistance) {
	    		updateResistance = false;
		    	linSystem.updateResistances(gatherResistances());
		    	updateCurrent = true;
	    	}
	    	if (updateVoltage) {	    
	    		updateVoltage = false;
		    	linSystem.updateSourceVoltage(gatherSourceVoltages());
		    	updateCurrent = true;
		    }
	    	if (updateInputCurrent) {
	    		updateInputCurrent = false;
	    		linSystem.updateInputCurrents(gatherInputCurrent());
	    		updateCurrent = true;
	    	}
	    }
	    
	    //Calculate-current:
	    
	    if (updateCurrent) {
			Vector current = CalculateCurrent();
			if (current != null) {
				updateCurrent = false;
		    	validNetwork = true;
				for (int i = 0; i < edges.size(); i++) {
					edges.get(i).setCurrent(current.at(i));
				}
			}
			else {
				validNetwork = false;
				for (int i = 0; i < edges.size(); i++) {
					edges.get(i).setCurrent(0.0f);
				}
			}
				
	    }
		for (Component component : components) {
			component.update(deltaTime);
		}

	}
	
	
	/**
	 * Depth First Search algorithm.
	 * HUN: Mélységi keresés.
	 * A kapott mátrixokat feltölti a gráf-reprezentáció illeszkedési és alapkör mátrixával.
	 * @param incidence	Incidence matrix to fill up. Will be filled with incidence matrix of the network as a graph.
	 * @param cycle	Cycle matrix to fill up. Will be filled with base cycle matrix  of the network as a graph.
	 */
	private void DFS (Matrix incidence, Matrix cycle) {
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
		        finish.put(iter, -1);
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
                        GreatestDepth = 1;
                        depth.put(v, GreatestDepth);
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
	
	/**
	 * Adds a new Edge to the network's graph representation. Generates two vertices to the new edge.
	 * HUN: Hozzáad egy élet a hálózat gráf-reprezentációjához. Létrehozza az él végpontjait is. 
	 * @param edge	Edge to be added.
	 */
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

	
	public void addEdgeWithGroundedOutput(Edge edge) {
		Vertex input = new Vertex();
		edge.setInput(input);
		edge.setOutput(this.vertices.get(0));
		
		input.addOutgoing(this.vertices.get(0), edge);
		this.vertices.get(0).addIncoming(input, edge);
		
		edges.add(edge);
		vertices.add(input);
	}
	
	/**
	 * Removes an edge from the network's graph representation. Removes two vertices of this edge, if the vertices connect only to the removed edge.
	 * HUN: Kitöröl egy élet a hálózat gráf-reprezentációjából. Az él két végpontját is törli, ha azokra nem illeszkedik más él.
	 * @param edge	Edge to be removed.
	 */
	public void removeEdge(Edge edge) {
		if (edge.getInput().getNoOfIncoming() == 0 && edge.getInput().getNoOfOutgoing() == 1 && vertices.indexOf(edge.getInput()) != 0) {
			vertices.remove(edge.getInput());
		}
		else {
			edge.getInput().removeOutgoing(edge.getOutput());
		}
		if (edge.getOutput().getNoOfIncoming() == 1 && edge.getOutput().getNoOfOutgoing() == 0 && vertices.indexOf(edge.getOutput()) != 0) {
			vertices.remove(edge.getOutput());
		}
		else {
			edge.getOutput().removeIncoming(edge.getInput());
		}
		
		setUpdateAll();
		
		edges.remove(edge);
	}
	
	
	/**
	 * Disconnects an edge's particular end point.
	 * HUN: Szétkapcsol egy élet egy adott végpontja szerint.
	 * @param edge		The edge to be disconnected by end point.
	 * @param vertex	The end point to disconnect by.
	 */
	protected void disconnectEndOfEdge(Edge edge, Vertex vertex) {
		if (vertex.equals(edge.getInput())) {
			if (vertices.indexOf(vertex) != 0 && (edge.getInput().getNoOfOutgoing() > 1 || edge.getInput().getNoOfIncoming() > 0)) {
				//Clone input vertex:
				Vertex prevIn = edge.getInput();
				Vertex prevOut = edge.getOutput();
				
				Vertex newIn = new Vertex();
				getVertices().add(newIn);
				
				newIn.addOutgoing(prevOut, edge);
				edge.setInput(newIn);
				prevOut.removeIncoming(prevIn);
				prevOut.addIncoming(newIn, edge);
				
				prevIn.removeOutgoing(prevOut);
				edge.setInput(newIn);
			}
		}
		
		else if (vertex.equals(edge.getOutput())) {
			if (edge.getOutput().getNoOfOutgoing() > 0 || edge.getOutput().getNoOfIncoming() > 1) {
				//Clone output vertex:
				Vertex prevIn = edge.getInput();
				Vertex prevOut = edge.getOutput();
				
				Vertex newOut = new Vertex();
				getVertices().add(newOut);
				
				newOut.addIncoming(prevIn, edge);
				edge.setOutput(newOut);
				prevIn.removeOutgoing(prevOut);
				prevIn.addOutgoing(newOut, edge);
				
				prevOut.removeIncoming(prevIn);			
				edge.setOutput(newOut);
			}
		}
		
	}
	
	/**
	 * Merges two vertices. After this only one vertex will remain. This, persistent vertex obtains the information held in the now obsolete vertex, such as the incoming and outgoing edges.
	 * HUN: Összeolvaszt két gráf-csúcsot. A perzisztens csúcs megkapja a beolvadó csúcs kapcsolatait.
	 * @param persistent	The vertex, which obtains the other's role.
	 * @param merge			The vertex, which is merged into the other. 
	 */
	protected void mergeVertices(Vertex persistent, Vertex merge)  {
		System.out.println("Merged vertices.");
		if (persistent != merge) {
			for (Map.Entry<Vertex, Edge> incoming : merge.getIncoming().entrySet()) {
				incoming.getKey().removeOutgoing(merge);
				incoming.getKey().addOutgoing(persistent, incoming.getValue());				
				
				incoming.getValue().setOutput(persistent);
				persistent.addIncoming(incoming.getKey(), incoming.getValue());
			}
			for (Map.Entry<Vertex, Edge> outgoing : merge.getOutgoing().entrySet()) {
				outgoing.getKey().removeIncoming(merge);
				outgoing.getKey().addIncoming(persistent, outgoing.getValue());
				
				outgoing.getValue().setInput(persistent);
				persistent.addOutgoing(outgoing.getKey(), outgoing.getValue());
			}
			vertices.remove(merge);
			setUpdateAll();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	//Manipulating components:---------------------------------------

	/**
	 * Adds a new component to the network.
	 * HUN: Új komponenst ad a gráfhoz.
	 * @param component The component to be added.
	 */
	public void addComponent (Component component) {
		component.setParent(this);
		component.build();
		components.add(component);
	}

	/**
	 * Removes a component from the network.
	 * HUN: 
	 * @param component	The component to be removed.
	 */
	public void removeComponent (Component component) {
		component.destroy();
		components.remove(component);
		
		setUpdateAll();
	}
	
	//Move ComponentNode:--------------------------------------------------------------
	
	/**
	 * Grab a component's end node to move it.
	 * HUN: Megfog egy adott komponenst. 
	 * @param componentNode	The node to grab.
 	 * @param cursorPos The position of the cursor;
	 */
	public void grabComponentNode(ComponentNode componentNode, Coordinate cursorPos) {
		if (!componentNodes.contains(componentNode)) {
			throw new RuntimeException("Invalid node grabbed.");
		}
		componentNode.grab(cursorPos);
		setUpdateAll();
	}
	
	/**
	 * Move a component's end node.
	 * HUN: Mozgat egy adott komponenst.
	 * @param componentNode	The node to be moved.
 	 * @param cursorPos The new position of the cursor;
	 */	
	public void dragComponentNode(ComponentNode componentNode, Coordinate cursorPos) {
		if (!componentNodes.contains(componentNode)) {
			throw new RuntimeException("Invalid node moved.");
		}
		componentNode.drag(cursorPos);
	}
	
	/**
	 * Release a component's end node. (When the node is already grabbed.)
	 * HUN: Elenged egy adott komponenst.
	 * @param componentNode The node to release.
	 */
	public void releaseComponentNode(ComponentNode componentNode) {
		if (!componentNodes.contains(componentNode)) {
			throw new RuntimeException("Invalid node released.");
		}
		componentNode.release();
		setUpdateAll();
	}

	//---------------------------------------------------------------
	//Move Component:
	
	/**
	 * Drops in a new component at the given position in network.
	 * HUN: Bedob egy új komponenst az adott pozícióra a hálózatban.
	 * @param component	The new component.
	 * @param cursorPos	The position to be dropped at. Input node is going to have x =-30 and output x = +30 offset on position.
	 */
	public void dropComponent(Component component, Coordinate cursorPos) {
		addComponent(component);
		selected = component;
		if (snapToGrid) {
			component.getInput().setPos(Coordinate.snapToGrid(MyMath.subtrackt(cursorPos, new Coordinate(30, 0)), gridSize));
			component.getOutput().setPos(Coordinate.snapToGrid(MyMath.add(cursorPos, new Coordinate(30, 0)), gridSize));					
		}
		else {
			component.getInput().setPos(MyMath.subtrackt(cursorPos, new Coordinate(30, 0)));
			component.getOutput().setPos(MyMath.add(cursorPos, new Coordinate(30, 0)));					
		}
		component.release();
		setUpdateAll();

	}
	
	/**
	 * Grab a component.
	 * HUN: Megfog egy adott komponenst.
	 * @param component The component to be grabbed.
	 * @param cursorPos The position of the cursor;
	 */
	public void grabComponent(Component component, Coordinate cursorPos) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid node grabbed.");
		}
		selected = component;
		component.grab(cursorPos);
		setUpdateAll();

	}
	
	/**
	 * Move a component.
	 * HUN: Mozgat egy adott komponenst.
	 * @param component	The component to be moved.
	 * @param cursorPos The new position of the cursor;
	 */	
	public void dragComponent(Component component, Coordinate cursorPos) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid component moved.");
		}
		component.drag(cursorPos);
	}
	
	public boolean isSnapToGrid() {
		return snapToGrid;
	}

	public void setSnapToGrid(boolean snapToGrid) {
		this.snapToGrid = snapToGrid;
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	/**
	 * Release a component. (When the component is already grabbed.)
	 * HUN: Elenged egy adott komponenst.
	 * @param component The component to release.
	 */
	public void releaseComponent(Component component) {
		if (!components.contains(component)) {
			throw new RuntimeException("Invalid component released.");
		}
		component.release();
		setUpdateAll();

	}
	//---------------------------------------------------------------
	
	/**
	 * When everything needs to be updated in the network. Sets all update flags:
	 * updateGraph,
	 * updateVoltage,
	 * updateResistance,
	 * updateCurrent
	 * HUN: Beállít minden frissítési jelzőt.
	 */
	protected void setUpdateAll() {
		updateGraph = true;
		updateVoltage = true;
		updateResistance = true;
		updateInputCurrent = true;
		updateCurrent = true;
	}
	
	/**
	 * Disconnects a the given component from the network. This means, that the end nodes of the component will be disconnected from other components.
	 * HUN: Letkapcsol egy komponenst a többi komponensről. Ez a komponenst két végpontjának lecsatlakoztatásával valósul meg.
	 * @param component {@link Component} to be cut out.
	 */
	protected void disconnectComponent(Component component) {
		if (component.getInput().getNoOfOutgoing() > 1 || component.getInput().getNoOfIncoming() > 0) {
			//Clone input:
			ComponentNode prevInput = component.getInput();
			ComponentNode newInput = new ComponentNode(prevInput.getParent());
			componentNodes.add(newInput);
			
			newInput.setPos(prevInput.getPos());
			newInput.setMerge(true);
			newInput.setVertexBinding(prevInput.getVertexBinding());
			
			prevInput.removeOutgoing(component);
			newInput.addOutgoing(component);
			component.setInput(newInput);			
		}
		if (component.getOutput().getNoOfIncoming() > 1 || component.getOutput().getNoOfOutgoing() > 0) {
			//Clone output:
			ComponentNode prevOutput = component.getOutput();
			ComponentNode newOutput = new ComponentNode(prevOutput.getParent());
			componentNodes.add(newOutput);
			
			newOutput.setPos(prevOutput.getPos());
			newOutput.setMerge(true);
			newOutput.setVertexBinding(prevOutput.getVertexBinding());
			
			prevOutput.removeIncoming(component);
			newOutput.addIncoming(component);
			component.setOutput(newOutput);			
		}
	}
	
	/**
	 * Tries to merge a given node to any of the other nodes.
	 * Conditions of a successful merge are, that the other node must be in the close proximity of this node and
	 * they can not be neighbor of each other.
	 * HUN: Megpróbál találni egy adott csomóponthoz egy másik csomópontot, amellyel összeolvaszthatja az adott csomópontot.
	 * Az összeolvasztás feltételei, hogy a másik csomópontnak "közel kell lennie" ( closeProximity )
	 * és nem lehetnek szomszédosak.
	 * 
	 * @param componentNode	The node, that is tried to be merged with other nodes.
	 * @return	True, when the merging attempt was successful.
	 */
	protected boolean tryToMergeComponentNode(ComponentNode componentNode) {
		for (ComponentNode iter : componentNodes) {
			if (iter != componentNode) {
				if (closeProximity > MyMath.magnitude(MyMath.subtrackt(componentNode.getPos(), iter.getPos()))) {					
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
	
	/**
	 * Gives a ComponentNode, in close proximity to the given Coordinate.
	 * HUN: Visszaad egy csomópontot, ami "közel van" (closeProximity) az adott pozícióhoz.
	 * @param pos	The position.
	 * @return	ComponentNode, in close proximity to the given Coordinate or null, if there is no ComponentNode in close proximity.
	 */
	public ComponentNode getNodeAtPos(Coordinate pos) {
		for (ComponentNode iter : componentNodes) {
			if (MyMath.magnitude(MyMath.subtrackt(iter.getPos(), pos)) < 10) {
				return iter;
			}
		}
		
		return null;
	}
	
	/**
	 * Gives a Component, in close proximity to the given Coordinate.
	 * HUN: Visszaad egy komponenst, ami "közel van" (closeProximity) az adott pozícióhoz.
	 * @param cursorPos The position.
	 * @return ComponentNode, in close proximity to the given Coordinate or null, if there is no ComponentNode in close proximity.
	 */
	public Component getComponentAtPos(Coordinate cursorPos) {
		
		for (Component component : components) {
			Vector inPos = MyMath.coordToVector(component.getInput().getPos());
			Vector outPos = MyMath.coordToVector(component.getOutput().getPos());
			Vector cursor = MyMath.coordToVector(cursorPos);
			
			Vector fromInToCursor = MyMath.subtract(cursor, inPos);
			Vector fromOutToCursor = MyMath.subtract(cursor, outPos);

			Vector fromInToOut = MyMath.subtract(outPos, inPos);			
			
			if (MyMath.dot(fromInToCursor, fromInToOut) > 0 && MyMath.dot(fromOutToCursor, fromInToOut) < 0) {
				float distance = MyMath.magnitude(MyMath.reject(fromInToCursor, fromInToOut)); 
				if (distance < closeProximity) {
					return component;
				}
			}
		}		
		
		return null;
	}
	
	/**
	 * Saves the current layout of the network. The method overwrites content of the file!
	 * HUN: Elmenti a hálózatot.
	 * @param fileName	The name of file, where the persistent information gets saved. 
	 */
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
	
	/**
	 * Loads network layout from the given file. Discards previous layout.
	 * HUN: Betölti a hálózatot.
	 * @param fileName {@link String} The name of file, from which the persistent information gets loaded.
	 */
	public void load(String fileName) {
		try {
			clear();			//Clear current state.
			setUpdateAll();	

			FileReader input = new FileReader(fileName);
			
			BufferedReader reader = new BufferedReader(input);
			
			Map<String, Class<?>> type = new HashMap<>();
			type.put("VoltageSource", VoltageSource.class);
			type.put("Wire", Wire.class);
			type.put("Resistance", Resistance.class);
			
			String row;
			while (null != (row = reader.readLine())) {
				row = row.replaceAll(" ", "");
				String pairs[] = row.split(";");
				
				if (pairs.length > 0) {
					String t[] = pairs[0].split(":");
					
					Class c = Class.forName(t[1]);
				
					Component comp = (Component) c.getConstructor().newInstance();
					
					this.addComponent(comp);				

					comp.load(pairs);
					comp.getInput().setMerge(true);
					comp.getOutput().setMerge(true);
					tryToMergeComponentNode(comp.getInput());
					tryToMergeComponentNode(comp.getOutput());
				}
				
			}
			reader.close();
			
		} catch (Exception e) {
			throw new RuntimeException("Load error!", e);
		}
			
	}
	
	/**
	 * Clears network layout. The model will be lost!
	 * HUN: Kötörli a hálózat tartalmát. A hálózat mentetlen állása el fog veszni! 
	 */
	public void clear() {
		components.clear();
		componentNodes.clear();
		edges.clear();
		vertices.clear();
		
		vertices.add(new Vertex());
		
		setUpdateAll();
	}

	/**
	 * Calls the draw method on each component.
	 * HUN: Minden komponensen meghívja a draw metódust.
	 * @param ctx	{@link GraphicsContext}, where the network should be drawn.
	 */
	public void draw(GraphicsContext ctx) {
		for (Component component : components) {
			component.draw(ctx);
		}
	}
	
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}
	
	/**
	 * Resets all components to initial state.
	 * HUN: Minden komponenst visszaállít a kiindulási állapotába.
	 */
	public void reset() {
		for (Component component : components) {
			component.reset();
		}
		setUpdateAll();
	}
	
	/**
	 * There can be only one selected component at a time.
	 * HUN: Csak egyetlen kiválasztott komponens lehet.
	 * @param component The component in question.
	 * @return	Whether the given component is selected or not.
	 */
	public boolean isThisSelected(Component component) {
		return selected != null && component == selected;
	}
	
	/**
	 * 
	 * @return The selected component. 
	 */
	public Component getSelected() {
		return selected;
	}
	
	/**
	 * Disable selection of component.
	 * HUN: Megszünteti egy komponens kiválasztását.
	 */
	public void cancelSelection() {
		selected = null;
	}
	
	/**
	 * Whether the network is valid or not.
	 * HUN: Helyes-e a hálózat?
	 * @return boolean
	 */
	public boolean isValid () {
		return validNetwork;
	}
	
}



