package network;

public class Wire extends Component {
	Edge e;
	
	@Override
	public float getCurrent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void create() {
		generateEndNodes();

		e = new Edge();
		parent.addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
		e.setSourceVoltage(0);		
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}