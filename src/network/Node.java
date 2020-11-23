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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((grabOffset == null) ? 0 : grabOffset.hashCode());
		result = prime * result + (grabbed ? 1231 : 1237);
		result = prime * result + ((incoming == null) ? 0 : incoming.hashCode());
		result = prime * result + (merge ? 1231 : 1237);
		result = prime * result + ((outgoing == null) ? 0 : outgoing.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (grabOffset == null) {
			if (other.grabOffset != null)
				return false;
		} else if (!grabOffset.equals(other.grabOffset))
			return false;
		if (grabbed != other.grabbed)
			return false;
		if (incoming == null) {
			if (other.incoming != null)
				return false;
		} else if (!incoming.equals(other.incoming))
			return false;
		if (merge != other.merge)
			return false;
		if (outgoing == null) {
			if (other.outgoing != null)
				return false;
		} else if (!outgoing.equals(other.outgoing))
			return false;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		return true;
	}

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
