package network;

public class Resistance extends Component {

	private Edge e;
	private float resistance = 0;

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
	public float getActualCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getActualVoltage() {
		return e.getVoltage();
	}

	@Override
	public float getActualResistance() {
		e.getResistance();
		return 0;
	}
	
	@Override
	public void save(StringBuilder writer) {
		writer.append(this.getClass().getSimpleName());
		writer.append(" {");				
		writer.append("resistance:");
		writer.append(resistance);

		writer.append(", inputPos:");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append(", outputPos:");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("}\n");
	}


}
