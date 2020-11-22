package network;

public abstract class Component {
	private Node input;
	private Node output;
	
	float resistance;
	float current;
	float sourceVoltage;
	
	public Component() {
		resistance = 1;
		current = 0;
		sourceVoltage = 0;
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
