package network;

import java.util.HashMap;

public class Node {
	private HashMap<Node, Edge> incoming;	//Key - The node on the other end of the edge.
	private HashMap<Node, Edge> outgoing;	

	float sourceVoltage = 0;
	float sourceCurrent = 0;
	
	static int gen = 0;
	private int id;
	
	Node () {
		gen++;
		id = gen;
		incoming = new HashMap<Node, Edge>();
		outgoing = new HashMap<Node, Edge>();
	}

	public HashMap<Node, Edge> getIncoming() {
		return incoming;
	}

	public HashMap<Node, Edge> getOutgoing() {
		return outgoing;
	}
	
	public void addIncoming(Node ohterEnd, Edge incoming) {
		this.incoming.put(ohterEnd, incoming);
	}
	
	public void removeIncoming(Node otherEnd) {
		this.incoming.remove(otherEnd);
	}
	
	public void addOutgoing(Node otherEnd, Edge outgoing) {
		this.outgoing.put(otherEnd, outgoing);
	}

	public void removeOutgoing(Node otherEnd) {
		this.outgoing.remove(otherEnd);
	}
	
	public int getNoOfIncoming() {
		return incoming.size();
	}
	
	public int getNoOfOutgoing() {
		return outgoing.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		if (id != other.getId())
			return false;
		return true;
	}

	
	
	public int getId() {
		return id;
	}

	public float getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(float sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	public float getSourceCurrent() {
		return sourceCurrent;
	}

	public void setSourceCurrent(float sourceCurrent) {
		this.sourceCurrent = sourceCurrent;
	}
	
}
