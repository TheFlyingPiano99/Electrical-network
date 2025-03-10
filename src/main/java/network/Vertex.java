package network;

import java.util.ArrayList;
import java.util.HashMap;
import math.*;

/**
 * Vertex of the graph representation of the network.
 * HUN: A hálózat gráf-reprezentációjának csúcsai. 
 * @author Simon Zoltán
 *
 */
public class Vertex implements Cloneable {

	/**
	 * Map of incoming edges.
	 * key {@link Vertex} Vertex on the other end of the edge.
	 * value {@link Edge} Incoming edge.
	 * HUN: Bejövő élek.
	 * key {@link Vertex} Csúcs az él másik végén.
	 * value {@link Edge} Bejövő él.
	 */
	private HashMap<Vertex, Edge> incoming;	//
	
	/**
	 * Map of outgoing edges.
	 * key Vertex {@link Vertex} Vertex on the other end of the edge.
	 * value Edge {@link Edge} Outgoing edge.
	 * HUN: Kimenő élek.
	 * key Vertex {@link Vertex} Csúcs az él másik végén.
	 * value Edge {@link Edge} Kimenő él.
	 */
	private HashMap<Vertex, Edge> outgoing;	
	
	static int gen = 0;
	private int id;
	private Vector inputCurrent;

	private double timeDomainInputCurrent = 0;
	private double timeDomainPotential = 0;

	//Constructor:-----------------------------------------------------
	
	Vertex () {
		gen++;
		id = gen;
		incoming = new HashMap<Vertex, Edge>();
		outgoing = new HashMap<Vertex, Edge>();
		inputCurrent = Vector.Zeros(Edge.defaultPhasorSpaceResolution);
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

	public Vector getInputCurrent() {
		return inputCurrent;
	}

	public void setInputCurrent(Vector inputCurrent) {
		this.inputCurrent = inputCurrent;
		
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
	 * Szomszédos-e ez a csúcs a paraméterként kapott csúccsal?
	 * @param v	The {@link Vertex} examined.
	 * @return <code>true</code> when this and the given vertex is node of the same {@link Edge}.
	 */
	public boolean isNeighbouring (Vertex v) {
		return ((v != null) && 
				(this.getIncoming().containsKey(v) || this.getOutgoing().containsKey(v)));
	}

	public void setTimeDomainInputPotential(double potential) {
		this.timeDomainPotential = potential;
	}

    public double getTimeDomainPotential() {
        return timeDomainPotential;
    }

	public void setTimeDomainPotential(double potential) {
		timeDomainPotential = potential;
	}

    public double getTimeDomainInputCurrent() {
        return timeDomainInputCurrent;
    }

	public void updateTimeDomainParameters(ArrayList<Double> omega, double totalTimeSec)
	{
		timeDomainInputCurrent = 0;
		for (int k = 0; k < omega.size(); k++) {
			timeDomainInputCurrent += Complex.multiply(
					inputCurrent.at(k),
					Complex.euler(1, omega.get(k) * totalTimeSec)
			).getRe();
		}
	}

    @Override
    public Vertex clone() {
        try {
            Vertex clone = (Vertex) super.clone();
			clone.inputCurrent = this.inputCurrent;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
