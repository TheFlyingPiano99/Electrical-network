package network;


/**
 *	Ideal wire, with 0 resistance.
 * @author simon
 * 
 */
public class Wire extends Component {
	private Edge e;
	
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

	@Override
	public float getActualVoltage() {
		return e.getVoltage();
	}

	@Override
	public float getActualResistance() {
		return e.getResistance();
	}
	
	@Override
	public float getActualCurrent() {
		return e.getCurrent();
	}
	
	@Override
	public void save(StringBuilder writer) {
		writer.append(this.getClass().getSimpleName());
		writer.append(" {");				
		
		writer.append("inputPos:");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append(", outputPos:");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("}\n");
	}

}