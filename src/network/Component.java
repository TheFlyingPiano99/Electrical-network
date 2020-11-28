package network;

public abstract class Component {	

	//persistent:
	float resistance = 1000;
	
	float sourceVoltage = 0;
	float sourceCurrent = 0;

	
	private boolean grabbed;

	private Network parent;

	private ComponentNode input;
	private ComponentNode output;

	public Component() {
	}
	
	public Component(Network parent) {
		this.parent = parent;
	}

	public Network getParent() {
		return parent;
	}

	public void setParent(Network parent) {
		this.parent = parent;
	}
			
	public boolean isGrabbed() {
		return grabbed;
	}

	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}
	
	public ComponentNode getInput() {
		return input;
	}

	public void setInput(ComponentNode input) {
		this.input = input;
	}

	public ComponentNode getOutput() {
		return output;
	}

	public void setOutput(ComponentNode output) {
		this.output = output;
	}
	
	protected void generateEndNodes() {
		this.setInput(new ComponentNode(parent));
		this.setOutput(new ComponentNode(parent));
		
		getInput().addOutgoing(this);
		getOutput().addIncoming(this);
		
		parent.getComponentNodes().add(this.getInput());
		parent.getComponentNodes().add(this.getOutput());
	}
	
	protected void removeEndNodes() {
		ComponentNode input = getInput();
		ComponentNode output = getOutput();
		if (input.getNoOfIncoming() == 0 && input.getNoOfOutgoing() == 1) {
			parent.getComponentNodes().remove(input);
		}
		else {
			input.getOutgoing().remove(this);
		}
		if (output.getNoOfIncoming() == 1 && output.getNoOfOutgoing() == 0) {
			parent.getComponentNodes().remove(output);
		}
		else {
			output.getIncoming().remove(this);			
		}
	}
		
	//To override:
	
	abstract public void create ();
	
	abstract public void destroy ();
	
	abstract public float getCurrent();
}