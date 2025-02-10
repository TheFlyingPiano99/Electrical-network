package network;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.canvas.GraphicsContext;
import math.*;


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
	private Component selected = null;
	private boolean snapToGrid = true;
	private boolean validNetwork = false;
	private boolean changedSetOfAngularFrequencies = false;
	private boolean needRecalculation = true;
	private int gridSize = 30;
	private final Object accessMutexObj = new Object();
	private final double angularFrequencyComparisonEpsilon = 0.001;
	//--------------------------------------------------

	public Object getMutexObj()
	{
		return accessMutexObj;
	}

	/**
	 * Distance of merging and grabbing.
	 * HUN: Az összeolvasztás és megfogás távolsága.
	 */
	int closeProximity = (int)(gridSize * 0.4);

	private final ArrayList<Double> simulatedAngularFrequencies = new ArrayList<Double>();
	private final ArrayList<Integer> angularFrequencyReferenceCounter = new ArrayList<Integer>();

	public ArrayList<Double> getSimulatedAngularFrequencies()
	{
		synchronized (accessMutexObj)
		{
			return simulatedAngularFrequencies;
		}
	}
	
	//Constructor:------------------------------------------------------
	
	public Network() {
		vertices = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		
		componentNodes = new ArrayList<ComponentNode>();
		components = new ArrayList<Component>();

		linSystem = null;
		
		//Create ground-node (index 0):
		vertices.add(new Vertex());

		simulatedAngularFrequencies.add(0.0);	// DC component is always simulated
		angularFrequencyReferenceCounter.add(1);	// Reference to the DC component
	}

	/**
	 * Adds the requested frequency to the simulated frequencies if not already there.
	 * @return The index of the new frequency
	 */
	public int requestAngularFrequency(double omega)
	{
		synchronized (accessMutexObj)
		{
			for (int i = 0; i < simulatedAngularFrequencies.size(); i++) {
				if (Math.abs(simulatedAngularFrequencies.get(i) - omega) < angularFrequencyComparisonEpsilon) {	// Already among simulated frequencies
					angularFrequencyReferenceCounter.set(i, angularFrequencyReferenceCounter.get(i) + 1);
					return i;
				}
				else if (simulatedAngularFrequencies.get(i) > omega)
				{
					simulatedAngularFrequencies.add(i, omega);	// Insert before the first larger frequency
					angularFrequencyReferenceCounter.add(i, 1);
					changedSetOfAngularFrequencies = true;
					needRecalculation = true;
					return i;
				}
			}
			simulatedAngularFrequencies.add(omega);		// Append to the end
			angularFrequencyReferenceCounter.add(1);
			changedSetOfAngularFrequencies = true;
			needRecalculation = true;
			return simulatedAngularFrequencies.size() - 1;
		}
	}

	public void releaseAngularFrequency(double omega)
	{
		synchronized (accessMutexObj)
		{
			for (int i = 0; i < simulatedAngularFrequencies.size(); i++) {
				if (Math.abs(simulatedAngularFrequencies.get(i) - omega) < angularFrequencyComparisonEpsilon) {	//  Among simulated frequencies
					int refCount = angularFrequencyReferenceCounter.get(i);
					if (refCount > 1) {		// Other components also require the same frequency
						angularFrequencyReferenceCounter.set(i, refCount - 1);
					}
					else {					// No other components require this frequency
						angularFrequencyReferenceCounter.remove(i);
						simulatedAngularFrequencies.remove(i);
						changedSetOfAngularFrequencies = true;
						needRecalculation = true;
					}
					return;
				}
			}
		}
	}

	//--------------------------------------------------------------------
	
	/**
	 * Returns the impedance of all the edges.
	 * HUN: Visszaad egy vektort amiben az összes gráf-élhez rendelt ellenállás értékei vannak felsorolva
	 * az élek, "edges" listában szereplő sorrendje szerint. 
	 * @return	Vector of resistances. The order of elements of the vector is the same as the order of the edges in private ArrayList&lt;Edge&gt; edges.
	 */
	private Vector gatherImpedance(int k) {
    	Vector impedances = new Vector(edges.size());
    	for (int i = 0; i < edges.size(); i++) {
			impedances.setAt(i, edges.get(i).getImpedance().at(k));
    	}		
    	return impedances;
	}
	
	/**
	 * Returns the source voltages of all the edges.
	 * HUN: Visszaad egy vektort amiben az összes gráf-élhez rendelt feszültségforrás értékei vannak felsorolva
	 * az élek, "edges" listában szereplő sorrendje szerint. 
	 * @return Vector of source voltages. The order of elements of the vector is the same as the order of the edges in private ArrayList&lt;Edge&gt; edges.
	 */
	private Vector gatherSourceVoltages(int k) {
    	Vector sourceVoltages = new Vector(edges.size());
    	for (int i = 0; i < edges.size(); i++) {
    		sourceVoltages.setAt(i, edges.get(i).getSourceVoltage().at(k));
    	}
    	return sourceVoltages;
	}
	
	/**
	 * 
	 * @return	Vector of currents inputed to individual vertices.
	 */
	private Vector gatherInputCurrent(int k) {
    	Vector inputCurrents = new Vector(vertices.size());
    	for (int i = 0; i < vertices.size(); i++) {
    		inputCurrents.setAt(i, vertices.get(i).getInputCurrent().at(k));
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
	 *
	 * @param omega the angular frequency to search for
	 * @return The index of the angularFrequency or -1 if the specific angular frequency
	 * is not among simulated frequencies
	 */
	public int getAngularFrequencyIndex(double omega) {
		for (int i = 0; i < simulatedAngularFrequencies.size(); i++) {
			if (Math.abs(simulatedAngularFrequencies.get(i) - omega) < angularFrequencyComparisonEpsilon) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Implements the physical behavior of the network. Calculates current resistance and voltage levels.
	 * HUN: A hálózat fizikai viselkedését valósítja meg. Kiszámolja az áram, ellenállás és feszültség szinteket.  
	 */
	public void evaluate(boolean forceEval) {
		synchronized (accessMutexObj) {
			if (!needRecalculation && !forceEval) {		// Early termination
				return;
			}
			needRecalculation = false;
			System.out.println("\nCalculating system");
			if (changedSetOfAngularFrequencies || forceEval) {
				changedSetOfAngularFrequencies = false;
				// Make sure that the ground vertex is always updated:
				vertices.get(0).setInputCurrent(Vector.Zeros(simulatedAngularFrequencies.size()));
				for (Component c : components) {
					c.updateFrequencyDependentParameters(simulatedAngularFrequencies);
				}
			}
			for (int k = 0; k < simulatedAngularFrequencies.size(); k++) {	// Finer time resolution

				if (!edges.isEmpty()) {
					//Graph representations:
					Matrix incidence = new Matrix(0,0);
					Matrix cycle = new Matrix(0,0);
					DFS(incidence, cycle);

					//Parameters:
					Vector impedance = gatherImpedance(k);
					Vector sourceVoltage = gatherSourceVoltages(k);
					Vector inputCurrent = gatherInputCurrent(k);

					//Create system:
					linSystem = new LinearSystemForCurrent(incidence, cycle, impedance, sourceVoltage, inputCurrent);

					//Calculate-current:
					Vector current = CalculateCurrent();
					if (current != null) {
						validNetwork = true;
						for (int i = 0; i < edges.size(); i++) {
							edges.get(i).getCurrent().setAt(k, current.at(i));
						}
					}
					else {
						validNetwork = false;
						for (int i = 0; i < edges.size(); i++) {
							edges.get(i).getCurrent().setAt(k, new Complex(0, 0));
						}
					}
				}
				else {		// If no edges in the system
					validNetwork = false;
				}
			}
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
	    incidence.fill(new Complex(0, 0));
	    int noOfCycles = 0;             //First count the cycles:
	    for (int i = 0; i < edges.size(); i++) {
	    	Edge edge = edges.get(i);
            incidence.setAt(i, vertices.indexOf(edge.getInput()), new Complex(1, 0));
            incidence.setAt(i, vertices.indexOf(edge.getOutput()), new Complex(-1, 0));
	        if (edge.getOutput() != previous.get(edge.getInput()) &&
	        		edge.getInput() != previous.get(edge.getOutput())) {
            	noOfCycles++;       	
	        }
	    }
	    
	    
	    cycle.copyWithResize(new Matrix(edges.size(), noOfCycles));
	    cycle.fill(new Complex(0, 0));
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
        			cycle.setAt(i, currentCycle, new Complex(1, 0));
	        		Vertex step = in;
	        		while (step != out) {
	        			if (previous.get(step) == null) {
	        				//System.out.println("Null.");
	        			}
	        			Edge e = step.getIncoming().get(previous.get(step));
	        			if (e != null) {
		        			cycle.setAt(edges.indexOf(e), currentCycle, new Complex(1, 0));
	        			}
	        			else {
	        				e = step.getOutgoing().get(previous.get(step));
		        			cycle.setAt(edges.indexOf(e), currentCycle, new Complex(-1, 0));
	        			}
	        				        			
        				step = previous.get(step);
	        		}
	        	}
	        	else if (dIn < dOut) {
	        		//Forward edge
        			cycle.setAt(i, currentCycle, new Complex(-1, 0));
	        		Vertex step = out;
	        		while (step != in) {
	        			Edge e = step.getIncoming().get(previous.get(step));
	        			if (e != null) {
		        			cycle.setAt(edges.indexOf(e), currentCycle, new Complex(1, 0));
	        			}
	        			else {
	        				e = step.getOutgoing().get(previous.get(step));
		        			cycle.setAt(edges.indexOf(e), currentCycle, new Complex(-1, 0));
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

	private void offsetAndNormalizePotentialsToZeroMinimum(ArrayList<Double> potentials, List<List<Vertex>> islands) {
		for (var island : islands) {
			int i = vertices.indexOf(island.get(0));
			double min = potentials.get(i);
			double max = potentials.get(i);
			for (var vertex : island) {
				int j = vertices.indexOf(vertex);
				if (min > potentials.get(j)) {
					min = potentials.get(j);
				}
				if (max < potentials.get(j)) {
					max = potentials.get(j);
				}
			}
			for (var vertex : island) {
				int j = vertices.indexOf(vertex);
				double pot = (potentials.get(j) - min) / (max - min);
				potentials.set(j, pot);
			}
		}
	}
		
	/*
	 * Return vector of potentials of vertices 
	 */
	private ArrayList<Double> discoverPotential_BFS() {
		if (vertices.isEmpty()) {
			throw new RuntimeException("No nodes to work with.");
		}
		ArrayList<Double> potentials = new ArrayList<>(vertices.size());
		for (int i = 0; i < vertices.size(); i++)
		{
			potentials.add(0.5);
		}

	    Vertex s = vertices.iterator().next();  //Starting vertex

	    Map<Vertex, Boolean> finished = new HashMap<Vertex, Boolean>();
	    Map<Vertex, Vertex> previous = new HashMap<Vertex, Vertex>();
	    List<Vertex> traversed = new ArrayList<Vertex>();
	    Vertex current;

	    List<List<Vertex>> islands = new ArrayList<>();
	    islands.add(new ArrayList<Vertex>());
	    
	    ///Initialization:
        previous.put(s, null);
        finished.put(s, false);        
        traversed.add(s);
		islands.get(0).add(s);

        for (Vertex iter : vertices) {
	        if (iter != s) {
		        previous.put(iter, null);
		        finished.put(iter, false);
	        }
	    }

	    current = s;

	    ///Cycle:
	    boolean run = true;
	    while (run) {
	    	//System.out.println("Begin BFS loop.");
	        ///Finding undiscovered adjacent vertex:
	    	boolean foundChild = false;
	        for (Vertex child : current.getOutgoing().keySet()) {
	            if (!traversed.contains(child)) {
	    	    	//System.out.println("Found outgoing: " + vertices.indexOf(child));
	    	    	traversed.add(child);
	            	previous.put(child, current);
	                current = child;
	                foundChild = true;
					break;
	            }
	        }
			if (!foundChild) {
				//Also search in reversed edges:
		        for (Vertex child : current.getIncoming().keySet()) {
		            if (!traversed.contains(child)) {
		    	    	//System.out.println("Found incoming: " + vertices.indexOf(child));
		            	traversed.add(child);
		            	previous.put(child, current);
		                current = child;
		                foundChild = true;
						break;
		            }
		        }
			}
	        if (!foundChild) {
				//System.out.println("Vertex finished: " + vertices.indexOf(current));
    	    	finished.put(current, true);	// No untraversed children left.
	            if (null != previous.get(current)) {	//Backtracking
	    	    	//System.out.println("Backtracking from " + vertices.indexOf(current));
	                current = previous.get(current);
				} else {
					//System.out.println("No edges from vertex: " + vertices.indexOf(current));
	                ///Finding untraversed vertex:
	                boolean foundUntraversed = false;
	                for (Vertex iter : vertices) {
	                    if (!traversed.contains(iter)) {
	                    	foundUntraversed = true;
			            	traversed.add(iter);
	                        current = iter;
							islands.add(new ArrayList<>());	// New island
							islands.get(islands.size() - 1).add(current);
							break;
	                    }
	                }
	                if (!foundUntraversed) {
	                    run = false;
	                }
	            }
	        }
	        else {
				islands.get(islands.size() - 1).add(current);
				double voltageDrop = 0;
				if (current.getIncoming().containsKey(previous.get(current))) {
					voltageDrop = current.getIncoming().get(previous.get(current)).getTimeDomainVoltageDrop();
				}
				else if (current.getOutgoing().containsKey(previous.get(current))) {
					voltageDrop = -current.getOutgoing().get(previous.get(current)).getTimeDomainVoltageDrop();
				}
				else {
					throw new RuntimeException("Wrong previous vertex!");
				}
				double potential = potentials.get(vertices.indexOf(previous.get(current))) - voltageDrop;
				//System.out.println("Potential: " + potential);
				potentials.set(vertices.indexOf(current), potential);
	        }
	    }
		
		offsetAndNormalizePotentialsToZeroMinimum(potentials, islands);


		return potentials;
	}

	
	//Access edges and nodes:-----------------------------------------------------------------------------------
	//No other method allowed to manipulate edges nor nodes from outside the network. 
	
	/**
	 * Adds a new Edge to the network's graph representation. Generates two vertices to the new edge.
	 * HUN: Hozzáad egy élet a hálózat gráf-reprezentációjához. Létrehozza az él végpontjait is. 
	 * @param edge	Edge to be added.
	 */
	public void addEdge(Edge edge) {
		synchronized (accessMutexObj)
		{
			Vertex input = new Vertex();
			Vertex output = new Vertex();

			edge.setInput(input);
			edge.setOutput(output);

			input.addOutgoing(output, edge);
			output.addIncoming(input, edge);

			edges.add(edge);
			vertices.add(input);
			vertices.add(output);
			needRecalculation = true;
		}
	}

	
	public void addEdgeWithGroundedOutput(Edge edge) {
		synchronized (accessMutexObj)
		{
			Vertex input = new Vertex();
			edge.setInput(input);
			edge.setOutput(this.vertices.get(0));

			input.addOutgoing(this.vertices.get(0), edge);
			this.vertices.get(0).addIncoming(input, edge);

			edges.add(edge);
			vertices.add(input);
			needRecalculation = true;
		}
	}
	
	/**
	 * Removes an edge from the network's graph representation. Removes two vertices of this edge, if the vertices connect only to the removed edge.
	 * HUN: Kitöröl egy élet a hálózat gráf-reprezentációjából. Az él két végpontját is törli, ha azokra nem illeszkedik más él.
	 * @param edge	Edge to be removed.
	 */
	public void removeEdge(Edge edge) {
		synchronized (accessMutexObj)
		{
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

			edges.remove(edge);
			needRecalculation = true;
		}
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
		//System.out.println("Merged vertices.");
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
		synchronized (accessMutexObj)
		{
			component.setParent(this);
			component.build();
			components.add(component);
			needRecalculation = true;
		}
	}

	/**
	 * Removes a component from the network.
	 * HUN: 
	 * @param component	The component to be removed.
	 */
	public void removeComponent (Component component) {
		synchronized (accessMutexObj)
		{
			component.destroy();
			components.remove(component);
			needRecalculation = true;
		}
	}
	
	//Move ComponentNode:--------------------------------------------------------------
	
	/**
	 * Grab a component's end node to move it.
	 * HUN: Megfog egy adott komponenst. 
	 * @param componentNode	The node to grab.
 	 * @param cursorPos The position of the cursor;
	 */
	public void grabComponentNode(ComponentNode componentNode, Coordinate cursorPos) {
		synchronized (accessMutexObj)
		{
			if (!componentNodes.contains(componentNode)) {
				throw new RuntimeException("Invalid node grabbed.");
			}
			componentNode.grab(cursorPos);
		}
	}
	
	/**
	 * Move a component's end node.
	 * HUN: Mozgat egy adott komponenst.
	 * @param componentNode	The node to be moved.
 	 * @param cursorPos The new position of the cursor;
	 */	
	public void dragComponentNode(ComponentNode componentNode, Coordinate cursorPos) {
		synchronized (accessMutexObj)
		{
			if (!componentNodes.contains(componentNode)) {
				throw new RuntimeException("Invalid node moved.");
			}
			componentNode.drag(cursorPos);
		}
	}
	
	/**
	 * Release a component's end node. (When the node is already grabbed.)
	 * HUN: Elenged egy adott komponenst.
	 * @param componentNode The node to release.
	 */
	public void releaseComponentNode(ComponentNode componentNode) {
		synchronized (accessMutexObj)
		{
			if (!componentNodes.contains(componentNode)) {
				throw new RuntimeException("Invalid node released.");
			}
			componentNode.release();
			needRecalculation = true;
		}
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
		synchronized (accessMutexObj)
		{
			addComponent(component);
			selected = component;
			if (snapToGrid) {
				component.getInput().setPos(Coordinate.snapToGrid(MyMath.subtract(cursorPos, new Coordinate(30, 0)), gridSize));
				component.getOutput().setPos(Coordinate.snapToGrid(MyMath.add(cursorPos, new Coordinate(30, 0)), gridSize));
			}
			else {
				component.getInput().setPos(MyMath.subtract(cursorPos, new Coordinate(30, 0)));
				component.getOutput().setPos(MyMath.add(cursorPos, new Coordinate(30, 0)));
			}
			component.release();
			needRecalculation = true;
		}
	}
	
	/**
	 * Grab a component.
	 * HUN: Megfog egy adott komponenst.
	 * @param component The component to be grabbed.
	 * @param cursorPos The position of the cursor;
	 */
	public void grabComponent(Component component, Coordinate cursorPos) {
		synchronized (accessMutexObj)
		{
			if (!components.contains(component)) {
				throw new RuntimeException("Invalid node grabbed.");
			}
			selected = component;
			component.grab(cursorPos);
		}
	}
	
	/**
	 * Move a component.
	 * HUN: Mozgat egy adott komponenst.
	 * @param component	The component to be moved.
	 * @param cursorPos The new position of the cursor;
	 */	
	public void dragComponent(Component component, Coordinate cursorPos) {
		synchronized (accessMutexObj)
		{
			if (!components.contains(component)) {
				throw new RuntimeException("Invalid component moved.");
			}
			component.drag(cursorPos);
		}
	}
	
	public boolean isSnapToGrid() {
		synchronized (accessMutexObj)
		{
			return snapToGrid;
		}
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
		synchronized (accessMutexObj)
		{
			if (!components.contains(component)) {
				return;
			}
			component.release();
			needRecalculation = true;
		}
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
				if (closeProximity > MyMath.magnitude(MyMath.subtract(componentNode.getPos(), iter.getPos()))) {
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
		synchronized (accessMutexObj)
		{
			return components;
		}
	}
	
	public ArrayList<ComponentNode> getComponentNodes() {
		synchronized (accessMutexObj)
		{
			return componentNodes;
		}
	}
	
	/**
	 * Gives a ComponentNode, in close proximity to the given Coordinate.
	 * HUN: Visszaad egy csomópontot, ami "közel van" (closeProximity) az adott pozícióhoz.
	 * @param pos	The position.
	 * @return	ComponentNode, in close proximity to the given Coordinate or null, if there is no ComponentNode in close proximity.
	 */
	public ComponentNode getNodeAtPos(Coordinate pos) {
		synchronized (accessMutexObj)
		{
			for (ComponentNode iter : componentNodes) {
				if (MyMath.magnitude(MyMath.subtract(iter.getPos(), pos)) < 10) {
					return iter;
				}
			}
			return null;
		}
	}
	
	/**
	 * Gives a Component, in close proximity to the given Coordinate.
	 * HUN: Visszaad egy komponenst, ami "közel van" (closeProximity) az adott pozícióhoz.
	 * @param cursorPos The position.
	 * @return ComponentNode, in close proximity to the given Coordinate or null, if there is no ComponentNode in close proximity.
	 */
	public Component getComponentAtPos(Coordinate cursorPos) {
		synchronized (accessMutexObj)
		{
			for (Component component : components) {
				Vector inPos = MyMath.coordToVector(component.getInput().getPos());
				Vector outPos = MyMath.coordToVector(component.getOutput().getPos());
				Vector cursor = MyMath.coordToVector(cursorPos);

				Vector fromInToCursor = MyMath.subtract(cursor, inPos);
				Vector fromOutToCursor = MyMath.subtract(cursor, outPos);

				Vector fromInToOut = MyMath.subtract(outPos, inPos);

				if (MyMath.dot(fromInToCursor, fromInToOut) > 0 && MyMath.dot(fromOutToCursor, fromInToOut) < 0) {
					double distance = MyMath.magnitude(MyMath.reject(fromInToCursor, fromInToOut));
					if (distance < closeProximity) {
						return component;
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * Saves the current layout of the network. The method overwrites content of the file!
	 * HUN: Elmenti a hálózatot.
	 * @param fileName	The name of file, where the persistent information gets saved. 
	 */
	public void save(String fileName) {
		synchronized (accessMutexObj)
		{
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
	}
	
	/**
	 * Loads network layout from the given file. Discards previous layout.
	 * HUN: Betölti a hálózatot.
	 * @param fileName {@link String} The name of file, from which the persistent information gets loaded.
	 */
	public void load(String fileName) {
		synchronized (accessMutexObj)
		{
			try {
				this.clear();			//Clear current state.

				FileReader input = new FileReader(fileName);

				BufferedReader reader = new BufferedReader(input);

				Map<String, Class<?>> type = new HashMap<>();
				type.put("VoltageSource", DCVoltageSource.class);
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
			finally {
				needRecalculation = true;
			}
		}
	}
	
	/**
	 * Clears network layout. The model will be lost!
	 * HUN: Kötörli a hálózat tartalmát. A hálózat mentetlen állása el fog veszni! 
	 */
	public void clear() {
		synchronized (accessMutexObj)
		{
			components.clear();
			componentNodes.clear();
			edges.clear();
			vertices.clear();

			vertices.add(new Vertex());
			needRecalculation = true;
		}
	}

	/**
	 * Calls the draw method on each component.
	 * HUN: Minden komponensen meghívja a draw metódust.
	 * @param ctx	{@link GraphicsContext}, where the network should be drawn.
	 */
	public void draw(GraphicsContext ctx, double totalTimeSec, double deltaTimeSec) {
		synchronized (accessMutexObj)
		{
			if (isValid()) {
				for (Edge e : edges) {
					e.updateTimeDomainParameters(simulatedAngularFrequencies, totalTimeSec);
				}
				for (Vertex v : vertices) {
					v.updateTimeDomainParameters(simulatedAngularFrequencies, totalTimeSec);
				}
				ArrayList<Double> potentials = discoverPotential_BFS();
				for (int i = 0; i < vertices.size(); i++) {
					vertices.get(i).setTimeDomainPotential(potentials.get(i));
				}
			}

			for (Component component : components) {
				if (isValid()) {
					component.updateCurrentVisualisationOffset(deltaTimeSec);
				}
				component.draw(ctx);
			}
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
		synchronized (accessMutexObj)
		{
			for (Component component : components) {
				component.reset();
			}
			needRecalculation = true;
		}
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
		synchronized (accessMutexObj)
		{
			return selected;
		}
	}
	
	/**
	 * Disable selection of component.
	 * HUN: Megszünteti egy komponens kiválasztását.
	 */
	public void cancelSelection() {
		synchronized (accessMutexObj)
		{
			selected = null;
		}
	}
	
	/**
	 * Whether the network is valid or not.
	 * HUN: Helyes-e a hálózat?
	 * @return boolean
	 */
	public boolean isValid () {
		synchronized (accessMutexObj)
		{
			return validNetwork;
		}
	}
	
}



