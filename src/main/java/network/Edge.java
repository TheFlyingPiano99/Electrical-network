package network;


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
	
	double resistance = 0;
	double current = 0;
	double sourceVoltage = 0;
	
	boolean grabbed = false;

	//Constructor:----------------------------------------------------------
	
	public Edge() {
		gen++;
		id = gen;
	}

	public Edge(double r, double i) {
		gen++;
		id = gen;
		resistance = r;
		current = i;
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

	public double getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(double sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	public double getVoltageDrop() {
		return (sourceVoltage == 0.0f)? current * resistance : -sourceVoltage;
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

	public double getResistance() {
		return resistance;
	}

	public void setResistance(double resistance) {
		this.resistance = resistance;			
	}

	public double getCurrent() {
		return current;
	}

	public void setCurrent(double current) {
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
