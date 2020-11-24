package network;

public class Edge {
	static int gen = 0;
	private int id;
	
	private Node input;
	private Node output;
	
	float resistance = 1000;
	float current = 0;
	
	boolean grabbed = false;

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
		return input.getSourceVoltage();
	}


	public void setSourceVoltage(float sourceVoltage) {
		input.setSourceVoltage(sourceVoltage);
	}

	public float getVoltage() {
		return current * resistance;
	}
	
	public Node getInput() {
		return input;
	}

	public void setInput(Node input) {
		this.input = input;
	}

	public Node getOutput() {
		return output;
	}

	public void setOutput(Node output) {
		this.output = output;
	}

	public float getResistance() {
		return resistance;
	}

	public void setResistance(float resistance) {
		if (resistance > 0) {
			this.resistance = resistance;			
		}		
	}

	public float getCurrent() {
		return current;
	}

	public void setCurrent(float current) {
		this.current = current;
	}
	
	
	
}
