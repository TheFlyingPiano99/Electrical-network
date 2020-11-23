package network;

public abstract class Component {	
	public Network getParent() {
		return parent;
	}

	public void setParent(Network parent) {
		this.parent = parent;
	}

	Network parent;

	private ComponentNode input;
	private ComponentNode output;

	private boolean grabbed;
	

	public Component() {
	}
	
	public Component(Network parent) {
		this.parent = parent;
	}
	
	public void build () {		
	}
	
	public void destroy () {
		
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

	public Component(float r, float i, float u) {
		;
	}
	
	
}