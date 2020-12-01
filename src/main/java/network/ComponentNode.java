package main.java.network;

import java.util.ArrayList;

import main.java.math.Coordinate;
import main.java.math.MyMath;

/**
 * The end node of components ({@link Component}). Helps establishing connection between components ({@link Component}).
 * HUN: A komponensek ({@link Component}) végpontja. Segít kialakítani a kapcsolatot a komponensek ({@link Component}) közt.
 * @author Simon Zoltán
 *
 */
public class ComponentNode {
	private Network parent;
	private ArrayList<Component> incoming;
	private ArrayList<Component> outgoing;
	
	/**
	 *Position on the board
	 *HUN: Elhelyezkedés a táblán. 
	 */
	Coordinate pos;
	
	/**
	 * When the node is grabbed, the actual position of the cursor and the position of the node may not match.
	 * HUN: Amikor a csomópont megfogódik, akkor a csomópont és a kurzor tényleges pozíciója eltérhet.
	 */
	Coordinate grabCursorOffset;
	
	/**
	 * Weather it should merge with other nodes, if in close proximity.
	 * HUN: Össze kell-e olvadnia a közelében lévő csomópontokkal.
	 */
	boolean merge = false;
	
	/**
	 * Weather the node is held by user.
	 * HUN: A felhasználó fogja-e a csomópontot.
	 */
	boolean grabbed = false;

	/**
	 * Bound vertex of graph.
	 * HUN: A gráf reprezetáció ehhez a csomóponthoz kötött csúcsa.
	 * 
	 */
	Vertex vertexBinding = null;
	
	//Constructors:------------------------------------------------------
	
	public ComponentNode() {
		this.pos = new Coordinate(10,10);
		incoming = new ArrayList<Component>();
		outgoing = new ArrayList<Component>();
	}
	
	public ComponentNode(Network parent) {
		this.parent = parent;
		this.pos = new Coordinate(10,10);
		incoming = new ArrayList<Component>();
		outgoing = new ArrayList<Component>();
	}

	//Getters/Setters:--------------------------------------------------
	
	public Network getParent() {
		return parent;
	}


	public void setParent(Network parent) {
		this.parent = parent;
	}


	public Vertex getVertexBinding() {
		return vertexBinding;
	}


	public void setVertexBinding(Vertex vertex) {
		this.vertexBinding = vertex;
	}


	public Coordinate getPos() {
		return pos;
	}


	public void setPos(Coordinate pos) {
		this.pos = pos;
	}
	
	public boolean isGrabbed() {
		return grabbed;
	}

	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public ArrayList<Component> getIncoming() {
		return incoming;
	}

	public ArrayList<Component> getOutgoing() {
		return outgoing;
	}
	
	public void addIncoming(Component incoming) {
		this.incoming.add(incoming);
	}
	
	public void removeIncoming(Component incoming) {
		this.incoming.remove(incoming);
	}
	
	public void addOutgoing(Component outgoing) {
		this.outgoing.add(outgoing);
	}

	public void removeOutgoing(Component outgoing) {
		this.outgoing.remove(outgoing);
	}
	
	/**
	 * 
	 * @return Number of incoming components.
	 */
	public int getNoOfIncoming() {
		return incoming.size();
	}
	
	/**
	 * 
	 * @return Number of outgoing components.
	 */
	public int getNoOfOutgoing() {
		return outgoing.size();
	}
	
	//Manipulation:-----------------------------------------------------------
	
	/**
	 * Grab node. (Before move.);
	 * HUN: Megfogja a csomópontot mozgatás előtt. 
	 * @param cursorPos TODO
	 */
	public void grab(Coordinate cursorPos) {
		this.setMerge(true);
		this.setGrabbed(true);
		grabCursorOffset = MyMath.subtrackt(cursorPos, pos);
	}
	
	/**
	 * Move node to new location.
	 * HUN: Átmozgatja a csomópontot új pozícióba.
	 * @param CursorPos {@link Coordinate} of the new position.
	 */
	public void drag(Coordinate CursorPos) {
		setPos(MyMath.subtrackt(CursorPos, grabCursorOffset));
	}
	
	/**
	 * Release node. (After grabbed.)
	 * HUN: Elengedi a csomópontot, ha az meg volt fogva.
	 */
	public void release() {
		setGrabbed(false);
		grabCursorOffset = null;
		parent.tryToMergeComponentNode(this);

	}
	
	/**
	 * Whether this and the given node is node of the same {@link Component}.
	 * Ez és a paraméterben kapott csomópont végpontja-e ugyanannak a komponensnek? (Szomszédosak-e?)
	 * @param n	The {@link ComponentNode} examined node.
	 * @return <code>true</code> when this and the given node is node of the same {@link Component}.
	 */
	public boolean isNeighbouring (ComponentNode n) {
		return this.vertexBinding.isNeighbouring(n.getVertexBinding());
	}
	
	
}
