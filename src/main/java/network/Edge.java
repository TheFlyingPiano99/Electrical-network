package main.java.network;

public class Edge {
	
	static int gen = 0;
	private int id;
	
	private Vertex input;
	private Vertex output;
	
	float resistance = 0;
	float current = 0;
	float sourceVoltage = 0;
	
	boolean grabbed = false;

	//Getters/Setters:----------------------------------------------------------
	
	public boolean isGrabbed() {
		return grabbed;
	}


	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}


	public Edge() {
		gen++;
		id = gen;
	}
	
	public Edge(float r, float i) {
		gen++;
		id = gen;
		resistance = r;
		current = i;
	}

	public int getId() {
		return id;
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
		Edge other = (Edge) obj;
		if (id != other.getId())
			return false;
		return true;
	}


	public float getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(float sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	public float getVoltage() {
		return current * resistance;
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

	public float getResistance() {
		return resistance;
	}

	public void setResistance(float resistance) {
		this.resistance = resistance;			
	}

	public float getCurrent() {
		return current;
	}

	public void setCurrent(float current) {
		this.current = current;
	}
	
	
	
}
