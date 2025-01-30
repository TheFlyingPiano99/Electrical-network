package network;
import math.*;


/**
 * The edge of the graph representation of the network.
 * HUN: A hálózat gráf-reprezentációjának élei. 
 * @author Simon Zoltán
 *
 */

public class Edge {
	public static int defaultPhasorSpaceResolution = 1024;

	static int gen = 0;
	private int id;
	
	private Vertex input;
	private Vertex output;
	
	Vector impedance = Vector.Zeros(defaultPhasorSpaceResolution);
	Vector current = Vector.Zeros(defaultPhasorSpaceResolution);
	Vector sourceVoltage = Vector.Zeros(defaultPhasorSpaceResolution);

	double timeDomainCurrent = 0.0;
	double timeDomainSourceVoltage = 0.0;
	double timeDomainVoltageDrop = 0.0;

	boolean grabbed = false;

	//Constructor:----------------------------------------------------------
	
	public Edge() {
		gen++;
		id = gen;
	}

	public Edge(Vector impedance, Vector current) {
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

	public Vector getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(Vector sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	public Vector getVoltageDrop() {
		return Vector.multiply(current, impedance);
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

	public Vector getImpedance() {
		return impedance;
	}

	public void setImpedance(Vector impedance) {
		this.impedance = impedance;
	}

	public Vector getCurrent() {
		return current;
	}

	public void setCurrent(Vector current) {
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

	public void updateTimeDomainParameters(Vector omega, double totalTimeSec)
	{
		timeDomainCurrent = 0;
		timeDomainSourceVoltage = 0;
		timeDomainVoltageDrop = 0;
		for (int k = 0; k < omega.dimension; k++) {
			Complex e = Complex.euler(1, omega.at(k).getRe() * totalTimeSec);
			timeDomainCurrent += Complex.multiply(current.at(k), e).getRe();
			timeDomainSourceVoltage += Complex.multiply(sourceVoltage.at(k), e).getRe();
			timeDomainVoltageDrop += Complex.multiply(Complex.multiply(current.at(k), impedance.at(k)), e).getRe();
		}
	}

	public final double getTimeDomainCurrent()
	{
		return timeDomainCurrent;
	}

	public final double getTimeDomainSourceVoltage()
	{
		return timeDomainSourceVoltage;
	}

	public final double getTimeDomainVoltageDrop()
	{
		return (timeDomainSourceVoltage == 0.0f)? timeDomainVoltageDrop : -timeDomainSourceVoltage;
	}
}
