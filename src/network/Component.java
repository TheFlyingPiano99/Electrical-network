package network;

public abstract class Component {
	private Node input;
	private Node output;
	
	float resistance;
	float current;
	
	public Component() {
		resistance = 1;
		current = 0;
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
		this.resistance = resistance;
	}

	public float getCurrent() {
		return current;
	}

	public void setCurrent(float current) {
		this.current = current;
	}
	
	
}
