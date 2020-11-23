package network;

public class Simulator {
	private Network network;
	boolean simulating = true;
	
	public Simulator() {
		network = new Network();
		
	}

	
	public void run () {
		network.addEdge(new Wire());
		
		if (simulating) {
			network.simulate();
		}
		
	}

	
}
