package network;

import java.util.ArrayList;

import math.Coordinate;

public class ComponentNode {
	private Network parent;
	
	Vertex vertexBinding = null;	//Bound node of graph.
	
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


	public Coordinate getGrabOffset() {
		return grabOffset;
	}


	public void setGrabOffset(Coordinate grabOffset) {
		this.grabOffset = grabOffset;
	}

	private ArrayList<Component> incoming;
	private ArrayList<Component> outgoing;
	
	//Position on the board:
	Coordinate pos;
	Coordinate grabOffset;
	
	
	boolean merge = false;		//Weather it should merge with other nodes, if in close proximity.
	boolean grabbed = false;	//Weather the node is held by user.

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
	
	public void destroy () {
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
	
	public void grab() {
		this.setMerge(true);
		this.setGrabbed(true);	
	}
	
	public void move(Coordinate pos) {
		setPos(pos);
	}
	
	public void release() {
		setGrabbed(false);
	}
		
}
