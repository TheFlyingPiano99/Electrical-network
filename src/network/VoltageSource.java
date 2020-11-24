package network;

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
		parent.addEdge(e);

		e.setCurrent(0);
		e.setResistance(20);
		
		
		getInput().setNode(e.getInput());
		getOutput().setNode(e.getOutput());
		
		e.getOutput().setSourceVoltage(sourceVoltage);	//!		

	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	
	@Override
	public void destroy() {		
		super.removeEndNodes();
		
		parent.removeEdge(e);
	}
	
}
