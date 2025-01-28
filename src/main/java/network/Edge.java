package network;
import math.*;

/**
 * The edge of the graph representation of the network.
 * HUN: A hálózat gráf-reprezentációjának élei. 
 * @author Simon Zoltán
 *
 */
public class Edge {
	
	static int gen = 0;
	private int id;
	
	private Vertex input;
	private Vertex output;
	
	Complex impedance = new Complex(0, 0);
	Complex current = new Complex(0, 0);
	Complex sourceVoltage = new Complex(0, 0);
	
	boolean grabbed = false;

	//Constructor:----------------------------------------------------------
	
	public Edge() {
		gen++;
		id = gen;
	}

	public Edge(Complex impedance, Complex current) {
		gen++;
		id = gen;
		this.impedance = impedance;
		this.current = current;
	}
	
	//Getters/Setters:----------------------------------------------------------
	
	public boolean isGrabbed() {
		return grabbed;
	}


	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}
	
	public int getId() {
		return id;
	}

	public Complex getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(Complex sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	public Complex getVoltageDrop() {
		return (sourceVoltage.equals(new Complex(0, 0)))? Complex.multiply(current, impedance) : (sourceVoltage).negate();
	}
	
	public Vertex getInput() {
		return input;
	}

	public void setInput(Vertex input) {
		this.input = input;
	}

	public Vertex getOutput() {
		return output;
	}

	public void setOutput(Vertex output) {
		this.output = output;
	}

	public math.Complex getImpedance() {
		return impedance;
	}

	public void setImpedance(Complex impedance) {
		this.impedance = impedance;
	}

	public Complex getCurrent() {
		return current;
	}

	public void setCurrent(Complex current) {
		this.current = current;
	}

	//HashCode/Equals:---------------------------------------------------
	
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
		Edge other = (Edge) obj;
		if (id != other.getId())
			return false;
		return true;
	}
	
}
