package main.java.network;

import java.util.HashMap;

/**
 * Vertex of the graph representation of the network.
 * @author Simon Zoltán
 *
 */
public class Vertex {
	private HashMap<Vertex, Edge> incoming;	//Key - The node on the other end of the edge.
	private HashMap<Vertex, Edge> outgoing;	
	
	static int gen = 0;
	private int id;

	//Constructor:-----------------------------------------------------
	
	Vertex () {
		gen++;
		id = gen;
		incoming = new HashMap<Vertex, Edge>();
		outgoing = new HashMap<Vertex, Edge>();
	}

	//Getters/Setters:-------------------------------------------------
	
	public HashMap<Vertex, Edge> getIncoming() {
		return incoming;
	}

	public HashMap<Vertex, Edge> getOutgoing() {
		return outgoing;
	}
	
	public void addIncoming(Vertex ohterEnd, Edge incoming) {
		this.incoming.put(ohterEnd, incoming);
	}
	
	public void removeIncoming(Vertex otherEnd) {
		this.incoming.remove(otherEnd);
	}
	
	public void addOutgoing(Vertex otherEnd, Edge outgoing) {
		this.outgoing.put(otherEnd, outgoing);
	}

	public void removeOutgoing(Vertex otherEnd) {
		this.outgoing.remove(otherEnd);
	}
	
	public int getNoOfIncoming() {
		return incoming.size();
	}
	
	public int getNoOfOutgoing() {
		return outgoing.size();
	}

	public int getId() {
		return id;
	}

	//HashCode/Equals:--------------------------------------------------------------
	
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
		Vertex other = (Vertex) obj;
		if (id != other.getId())
			return false;
		return true;
	}

	//Other:-----------------------------------------------------------------
	
	/**
	 * Whether this and the given vertex is vertex of the same {@link Edge}.
	 * @param v	The {@link Vertex} examined.
	 * @return <code>true</code> when this and the given vertex is vertex of the same {@link Edge}.
	 */
	public boolean isNeighbouring (Vertex v) {
		return ((v != null) && 
				(this.getIncoming().containsKey(v) || this.getOutgoing().containsKey(v)));
	}

}
