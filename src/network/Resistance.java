package network;

public class Resistance extends Component {
	float resistance = 1000;
	Edge e;
	public Resistance() {
	}

	public Resistance(float r) {
		resistance = r;
	}
	
	
	@Override
	public void create() {
		generateEndNodes();
		
		e = new Edge();
		parent.addEdge(e);

		e.setCurrent(0);
		e.setResistance(resistance);	//!
		e.setSourceVoltage(0);
		
		
		getInput().setNode(e.getInput());
		getOutput().setNode(e.getOutput());
		
	}

	@Override
	public void destroy() {
		removeEndNodes();
		parent.removeEdge(e);
		
		
	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

}
