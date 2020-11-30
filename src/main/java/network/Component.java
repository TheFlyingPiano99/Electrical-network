package main.java.network;

import javafx.util.Duration;

import javafx.scene.canvas.GraphicsContext;
import main.java.math.Coordinate;
import main.java.math.MyMath;

/**
 * Abstract parent of all network components. 
 * @author Simon Zoltán
 *
 */
public abstract class Component {	
		
	private Network parent;

	private ComponentNode input;
	private ComponentNode output;
	
	//Manipulation related:
	boolean grabbed = false;

	/**
	 * When the component is grabbed, the actual position of the cursor and the position of the components input node may not match.
	 */
	Coordinate grabCursorOffset;
	
	/**
	 * When the component is moved, the input and output node must move together.
	 * This coordinate is basically a vector pointing from input's position to output's position.
	 */
	Coordinate fromInputToOutput;	
	
	
	//Constructors:-----------------------------------------------------
	
	public Component() {
	}
	
	public Component(Network parent) {
		this.parent = parent;
	}
	
	///Getters/Setters:-------------------------------------------------
	
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
	
	//Default generators:----------------------------------------------------------
	
	/**
	 * Generates every end node to this component. Should be called in beginning of build method.
	 */
	protected void generateEndNodes() {
		this.setInput(new ComponentNode(parent));
		this.setOutput(new ComponentNode(parent));
		
		getInput().addOutgoing(this);
		getOutput().addIncoming(this);
		
		parent.getComponentNodes().add(this.getInput());
		parent.getComponentNodes().add(this.getOutput());
	}
	
	/**
	 * Removes every end node to this component. Should be called in beginning of destroy method. 
	 */
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
	
	//Manipulate:----------------------------------------------------------------
	
	public void grab(Coordinate cursorPos) {
		grabbed = true;
		grabCursorOffset = MyMath.subtrackt(cursorPos, getInput().getPos());	//CP - P = RP
		fromInputToOutput = MyMath.subtrackt(getOutput().getPos(), getInput().getPos());
		parent.disconnectComponent(this);
		disconnectGraphRepresentation();
	}

	public void drag(Coordinate cursorPos) {
		Coordinate newInputPos = MyMath.subtrackt(cursorPos, grabCursorOffset);
		getInput().setPos(newInputPos);
		getOutput().setPos(MyMath.add(newInputPos, fromInputToOutput));
	}

	public void release() {
		grabbed = false;
		grabCursorOffset = null;
		fromInputToOutput = null;
		parent.tryToMergeComponentNode(getInput());
		parent.tryToMergeComponentNode(getOutput());
	}

		
	//To override:---------------------------------------------------------------
	
	/**
	 * Build the inner structure of the component, including elements of the graph representation. Must generate end nodes. 
	 */
	abstract public void build ();
	
	/**
	 * Destroys the inner structure of the component, including elements of the graph representation. Must remove end nodes.
	 */
	abstract public void destroy ();
	
	/**
	 * Updates the inner structure of the component, including elements of the graph representation.
	 * In case of nonlinear components this method changes parameters of the graph representation. In this case it must set related flags of the parent network!
	 * @param deltaTime	The time spent since the last call of update.
	 */
	abstract public void update(Duration deltaTime);
	
	/**
	 * Adds the persistent content of the component to the given builder. 
	 * @param builder	The StringBuilder, in which the persistent information will be added.
	 */
	abstract public void save(StringBuilder builder);
	
	/**
	 * Gets a array of Strings containing pairs of flags and values. The flag and the value must be separated by colons.
	 * In case of stored Coordinate the value must be stored in the following format: [x,y]
	 * @param pairs
	 */
	abstract public void load(String[] pairs);
	
	/**
	 * 
	 * @return current
	 */
	abstract public float getCurrent();
	
	/**
	 * 
	 * @return voltage
	 */
	abstract public float getVoltage();
	
	/**
	 * 
	 * @return resistance
	 */
	abstract public float getResistance();
	
	/**
	 * Draws the component's visual representation to the given GraphicsContext. 
	 * @param ctx GraphicsContext, where the component gets drawn.
	 */
	abstract public void draw(GraphicsContext ctx);

	abstract void disconnectGraphRepresentation();
	
}