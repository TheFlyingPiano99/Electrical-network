package main.java.network;

import javafx.util.Duration;

import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import main.java.math.Coordinate;
import main.java.math.MyMath;

/**
 * Abstract parent of all network components.
 * HUN: Minden komponens absztrakt ősosztálya.
 * @author Simon Zoltán
 *
 */
public abstract class Component {
		
	private Network parent;

	
	private Map<String, ComponentProperty> properties = null;
	
	private ComponentNode input;
	private ComponentNode output;
	
	//Manipulation related:
	boolean grabbed = false;

	/**
	 * When the component is grabbed, the actual position of the cursor and the position of the components input node may not match.
	 * HUN: Amikor megfogunk egy komponenst, akkor a kurzor és a komponens pozíciója nem egyezik teljesen.
	 */
	Coordinate grabCursorOffset;
	
	/**
	 * When the component is moved, the input and output node must move together.
	 * This coordinate is basically a vector pointing from input's position to output's position.
	 * HUN: Amikor mozgatunk egy komponenst, akkor a két végpontjának szinkronban kell mozognia.
	 * Ez a lényegében az input pozícióból az output pozícióba mutató vektor. 
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
	
	public Map<String, ComponentProperty> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, ComponentProperty> properties) {
		this.properties = properties;
	}

	//Default generators:----------------------------------------------------------
	
	/**
	 * Generates every end node to this component. Should be called in beginning of build method.
	 * HUN: Végpontokat generál a komponenshez. Minden build elején meg kell hívni!
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
	 * HUN: Megszünteti a végpontokat. Minden destroy metódus elején meg kell hívni!
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
	
	/**
	 * Grabs component to move.
	 * HUN: Megfogja a komponenst, hogy aztán mozgatni lehessen.
	 * @param cursorPos position of cursor
	 */
	public void grab(Coordinate cursorPos) {
		grabbed = true;
		grabCursorOffset = MyMath.subtrackt(cursorPos, getInput().getPos());	//CP - P = RP
		fromInputToOutput = MyMath.subtrackt(getOutput().getPos(), getInput().getPos());
		parent.disconnectComponent(this);
		disconnectGraphRepresentation();
	}

	/**
	 * Drag component to new position.
	 * HUN: A komponenst új helyre mozgatja.
	 * @param cursorPos	Position of the cursor.
	 */
	public void drag(Coordinate cursorPos) {
		Coordinate newInputPos = MyMath.subtrackt(cursorPos, grabCursorOffset);
		getInput().setPos(newInputPos);
		getOutput().setPos(MyMath.add(newInputPos, fromInputToOutput));
	}

	/**
	 * Releases component. (If it was grabbed before.)
	 * HUN: Elengedi a komponenst, ha az meg volt fogva.
	 */
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
	 * HUN: A komponens belső struktúráját hozza létre: gráf reprezentáció. A végpontok generálását is el kell végeznie. 
	 */
	abstract public void build ();


	/**
	 * Destroys the inner structure of the component, including elements of the graph representation. Must remove end nodes.
	 * HUN: Megszünteti a komponens belső struktúráját.
	 */
	abstract public void destroy ();
	
	/**
	 * Updates the inner structure of the component, including elements of the graph representation.
	 * In case of nonlinear components this method changes parameters of the graph representation. In this case it must set related flags of the parent network!
	 * HUN: Frissíti a belső struktúrát. Nem lineáris komponensek esetén különösen fontos! 
	 * @param deltaTime	The time spent since the last call of update.
	 */
	abstract public void update(Duration deltaTime);
	
	/**
	 * Adds the persistent content of the component to the given builder.
	 * HUN: Szöveges formátumban, elmenti a komponens perzisztens tartalmát.
	 * @param builder	The StringBuilder, in which the persistent information will be added.
	 */
	abstract public void save(StringBuilder builder);
	
	/**
	 * Gets a array of Strings containing pairs of flags and values. The flag and the value must be separated by colons.
	 * In case of stored Coordinate the value must be stored in the following format: [x,y]
	 * HUN: Már részben feldolgozott stringeket kap, amelyek párban tartalmaznak címkét és értéket kettősponttal elválasztva.
	 * Ha koordinátát is tartalmaz a szöveg, akkor annak értéke [x,y] formátumban kell legyen megadva. 
	 * @param pairs	flags and values.
	 */
	abstract public void load(String[] pairs);
	
	/**
	 * Returns electric current value in ampere.
	 * HUN: Visszaadja az áramszintet amperben.
	 * @return current ampere
	 */
	abstract public float getCurrent();
	
	/**
	 * Returns electric voltage drop in volt.
	 * HUN: Visszaadja az elektromos feszültég esést voltban.  
	 * @return voltage volt
	 */
	abstract public float getVoltage();
	
	/**
	 * Returns electric resistance of component in ohm.
	 * HUN: Visszaadja az elektromos ellenállást ohmban. 
	 * @return resistance ohm
	 */
	abstract public float getResistance();
	
	/**
	 * Draws the component's visual representation to the given GraphicsContext.
	 * HUN: Kirajzolja a komponens grafikus reprezentációját a megadott rajzfelületre. 
	 * @param ctx GraphicsContext, where the component gets drawn.
	 */
	abstract public void draw(GraphicsContext ctx);

	/**
	 * Disconnects graph elements (edges, vertices) from other parts of the graph representation of the network.
	 * HUN: Lekapcsolja a komponenshez tartozó gráf-elemeket a gráf többi részéről.
	 */
	abstract public void disconnectGraphRepresentation();

	/**
	 * Resets values of the component to the initial state.
	 * HUN: Kiindulási állapotba állítja a komponens értékeit.
	 */
	abstract public void reset();
	
	/**
	 * Updates the components inner vaules according to the map of {@link ComponentProperty}.
	 * HUN: Frissíti a komponens belső értékeit a paraméterek mapja szerint.
	 */
	abstract public void updatePropertyModel(); 

	/**
	 * Updates the {@link ComponentProperty} map according to the inner values of the component. 
	 * HUN: Frissíti a {@link ComponentProperty} mapot a belső változók szerint.
	 */
	abstract public void updatePropertyView();
	
}