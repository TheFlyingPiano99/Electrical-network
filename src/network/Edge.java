package network;

public class Edge {
	private Node input;
	private Node output;
	
	float resistance;
	float current;
	float sourceVoltage;
	
	boolean grabbed = false;

	public boolean isGrabbed() {
		return grabbed;
	}


	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}


	public Edge() {
		resistance = 1;
		current = 0;
		sourceVoltage = 0;
	}
	
	public Edge(float r, float i, float u) {
		resistance = r;
		current = i;
		sourceVoltage = u;
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
