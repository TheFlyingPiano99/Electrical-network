package network;

import java.util.ArrayList;

import math.Coordinate;

public class Node {
	private ArrayList<Edge> incoming;
	private ArrayList<Edge> outgoing;	

	Node () {
		incoming = new ArrayList<Edge>();
		outgoing = new ArrayList<Edge>();
	}

	public ArrayList<Edge> getIncoming() {
		return incoming;
	}

	public ArrayList<Edge> getOutgoing() {
		return outgoing;
	}
	
	public void addIncoming(Edge incoming) {
		this.incoming.add(incoming);
	}
	
	public void removeIncoming(Edge incoming) {
		this.incoming.remove(incoming);
	}
	
	public void addOutgoing(Edge outgoing) {
		this.outgoing.add(outgoing);
	}

	public void removeOutgoing(Edge outgoing) {
		this.outgoing.remove(outgoing);
	}
	
	public int getNoOfIncoming() {
		return incoming.size();
	}
	
	public int getNoOfOutgoing() {
		return outgoing.size();
	}
	
}
