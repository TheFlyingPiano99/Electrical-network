package network;

import java.io.IOException;

/**
 * Ideal voltage source, with 0 resistance.
 * @author simon
 *
 */
public class VoltageSource extends Component {
	private Edge e;
	private float sourceVoltage = 0;
	
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
	public void destroy() {		
		super.removeEndNodes();
		
		super.getParent().removeEdge(e);
	}

	public float getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(float sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	
	@Override
	public float getActualCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getActualVoltage() {
		return getSourceVoltage();
	}

	@Override
	public float getActualResistance() {
		return sourceVoltage / e.getCurrent();
	}


	@Override
	public void save(StringBuilder writer) {
		writer.append(this.getClass().getSimpleName());
		writer.append(" {");				
		writer.append("voltage:");
		writer.append(sourceVoltage);

		writer.append(", inputPos:");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append(", outputPos:");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("}\n");
	}


	@Override
	public void load(String row) {
		;
	}
	
}
