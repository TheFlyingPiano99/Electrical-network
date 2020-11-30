package main.java.network;

import java.util.ArrayList;

import main.java.math.Coordinate;
import main.java.math.MyMath;

/**
 * The end node of all components. Helps establishing connection between components. 
 * @author Simon Zoltán
 *
 */
public class ComponentNode {
	private Network parent;
	private ArrayList<Component> incoming;
	private ArrayList<Component> outgoing;
	
	//Position on the board:
	Coordinate pos;
	Coordinate grabCursorOffset;	//When the node is grabbed, the actual position of the cursor and the position of the node may not match.
	
	boolean merge = false;		//Weather it should merge with other nodes, if in close proximity.
	boolean grabbed = false;	//Weather the node is held by user.

	Vertex vertexBinding = null;	//Bound node of graph.
	
	//Constructors:------------------------------------------------------
	
	public ComponentNode() {
		this.pos = new Coordinate(10,10);
		incoming = new ArrayList<Component>();
		outgoing = new ArrayList<Component>();
	}
	
	public ComponentNode(Network parent) {
		this.parent = parent;
		this.pos = new Coordinate(10,10);
		incoming = new ArrayList<Component>();
		outgoing = new ArrayList<Component>();
	}

	//Getters/Setters:--------------------------------------------------
	
	public Network getParent() {
		return parent;
	}


	public void setParent(Network parent) {
		this.parent = parent;
	}


	public Vertex getVertexBinding() {
		return vertexBinding;
	}


	public void setVertexBinding(Vertex vertex) {
		this.vertexBinding = vertex;
	}


	public Coordinate getPos() {
		return pos;
	}


	public void setPos(Coordinate pos) {
		this.pos = pos;
	}
	
	public boolean isGrabbed() {
		return grabbed;
	}

	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public ArrayList<Component> getIncoming() {
		return incoming;
	}

	public ArrayList<Component> getOutgoing() {
		return outgoing;
	}
	
	public void addIncoming(Component incoming) {
		this.incoming.add(incoming);
	}
	
	public void removeIncoming(Component incoming) {
		this.incoming.remove(incoming);
	}
	
	public void addOutgoing(Component outgoing) {
		this.outgoing.add(outgoing);
	}

	public void removeOutgoing(Component outgoing) {
		this.outgoing.remove(outgoing);
	}
	
	public int getNoOfIncoming() {
		return incoming.size();
	}
	
	public int getNoOfOutgoing() {
		return outgoing.size();
	}
	
	//Manipulation:-----------------------------------------------------------
	
	/**
	 * Grab node. (Before move.); 
	 * @param cursorPos TODO
	 */
	public void grab(Coordinate cursorPos) {
		this.setMerge(true);
		this.setGrabbed(true);
		grabCursorOffset = MyMath.subtrackt(cursorPos, pos);
	}
	
	/**
	 * Move node to new location.
	 * @param pos {@link Coordinate} of the new position.
	 */
	public void drag(Coordinate CursorPos) {
		setPos(MyMath.subtrackt(CursorPos, grabCursorOffset));
	}
	
	/**
	 * Release node. (After grabbed.)
	 */
	public void release() {
		setGrabbed(false);
		grabCursorOffset = null;
		parent.tryToMergeComponentNode(this);

	}
	
	/**
	 * Whether this and the given node is node of the same {@link Component}.
	 * @param n	The {@link ComponentNode} examined.
	 * @return <code>true</code> when this and the given node is node of the same {@link Component}.
	 */
	public boolean isNeighbouring (ComponentNode n) {
		return this.vertexBinding.isNeighbouring(n.getVertexBinding());
	}
	
}
