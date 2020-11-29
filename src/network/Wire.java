package network;

import java.time.Duration;
import java.util.Scanner;

/**
 *	Ideal wire, with 0 resistance.
 * @author simon
 * 
 */
public class Wire extends Component {
	private Edge e;

	//Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public float getVoltage() {
		return e.getVoltage();
	}

	@Override
	public float getResistance() {
		return e.getResistance();
	}
	
	@Override
	public float getCurrent() {
		return e.getCurrent();
	}
	
	//Build/Destroy:------------------------------------------------------------------------------------

	@Override
	public void build() {
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
	
	//Update:---------------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		;	//Do nothing.
	}

	//Persistence:-----------------------------------------------------------------------------------

	@Override
	public void save(StringBuilder writer) {
		writer.append(this.getClass().getSimpleName());
		writer.append(": {");				
		
		writer.append("inputPos:");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append(", outputPos:");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("}\n");
	}

	@Override
	public void load(Scanner scanner) {
		// TODO Auto-generated method stub
		
	}

}