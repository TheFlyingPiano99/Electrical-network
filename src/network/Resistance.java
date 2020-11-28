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
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(resistance);	//!
		e.setSourceVoltage(0);
		
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
		
	}

	@Override
	public void destroy() {
		removeEndNodes();
		super.getParent().removeEdge(e);
		
		
	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}
	
	

}
