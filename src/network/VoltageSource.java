package network;

/**
 * Ideal voltage source, with 0 resistance.
 * @author simon
 *
 */
public class VoltageSource extends Component {
	Edge e;
	float sourceVoltage = 10;
	
	public VoltageSource() {
	}
	
	
	public VoltageSource(float u) {
		sourceVoltage = u;
	}
		
	@Override
	public void create() {
		super.generateEndNodes();
		
		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
		
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
		
		e.setSourceVoltage(sourceVoltage);	//!		

	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	
	@Override
	public void destroy() {		
		super.removeEndNodes();
		
		super.getParent().removeEdge(e);
	}
	
}
