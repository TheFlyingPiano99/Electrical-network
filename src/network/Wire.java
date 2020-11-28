package network;


/**
 *	Ideal wire, with 0 resistance.
 * @author simon
 * 
 */
public class Wire extends Component {
	Edge e;
	
	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	@Override
	public void create() {
		generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
		e.setSourceVoltage(0);		
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
	}

	@Override
	public void destroy() {
		removeEndNodes();
		super.getParent().removeEdge(e);
	}
	
	
	
}