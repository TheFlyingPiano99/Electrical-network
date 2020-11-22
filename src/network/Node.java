package network;

import java.util.ArrayList;

public class Node {
	private ArrayList<Component> incoming;
	private ArrayList<Component> outgoing;
	
	Node () {
		setIncoming(new ArrayList<Component>());
		setOutgoing(new ArrayList<Component>());
		
	}

	public ArrayList<Component> getIncoming() {
		return incoming;
	}

	public void setIncoming(ArrayList<Component> incoming) {
		this.incoming = incoming;
	}

	public ArrayList<Component> getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(ArrayList<Component> outgoing) {
		this.outgoing = outgoing;
	}
	
}
