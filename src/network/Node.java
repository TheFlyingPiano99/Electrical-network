package network;

import java.util.ArrayList;

import math.Coordinate;

public class Node {
	private ArrayList<Component> incoming;
	private ArrayList<Component> outgoing;
	
	//Position on the board:
	Coordinate pos;
	Coordinate grabOffset;
	
	
	boolean merge = false;		//Weather it should merge with other nodes, if in close proximity.
	boolean grabbed = false;	//Weather the node is held by user.
	
	public boolean isGrabbed() {
		return grabbed;
	}

	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}

	public Coordinate getPos() {
		return pos;
	}

	public void setPos(Coordinate pos) {
		this.pos = pos;
	}

	Node () {
		incoming = new ArrayList<Component>();
		outgoing = new ArrayList<Component>();
		pos = new Coordinate(0,0);
		grabOffset = new Coordinate(0,0);
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
	
}
