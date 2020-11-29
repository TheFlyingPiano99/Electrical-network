package network;

import java.time.Duration;
import java.util.Scanner;

/**
 * Ideal voltage source, with 0 resistance.
 * @author simon
 *
 */
public class VoltageSource extends Component {
	private Edge e;
	private float sourceVoltage = 0;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public VoltageSource() {
	}
	
	
	public VoltageSource(float u) {
		sourceVoltage = u;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------
	
	public float getSourceVoltage() {
		return sourceVoltage;
	}

	public void setSourceVoltage(float sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getVoltage() {
		return getSourceVoltage();
	}

	@Override
	public float getResistance() {
		return sourceVoltage / e.getCurrent();
	}

	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
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

	//Update:----------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		
	}


	//Persistence:-----------------------------------------------------------------------------------
	
	@Override
	public void save(StringBuilder writer) {
		writer.append(this.getClass().getCanonicalName());
		writer.append(": {");				
		writer.append("voltage:");
		writer.append(sourceVoltage);

		writer.append(", inputPos:");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append(", outputPos:");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("}\n");
	}

	@Override
	public void load(Scanner scanner) {
		;
	}
	
}
