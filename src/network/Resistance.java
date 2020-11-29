package network;

import java.time.Duration;
import java.util.Scanner;

public class Resistance extends Component {

	private Edge e;
	private float resistance = 0;

	//Constructors:---------------------------------------------------------------------------------------

	public Resistance() {
	}

	public Resistance(float r) {
		resistance = r;
	}
	
	//Getters/Setters:------------------------------------------------------------------------------------
	
	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getVoltage() {
		return e.getVoltage();
	}

	@Override
	public float getResistance() {
		e.getResistance();
		return 0;
	}
	
	public float setResistance(float resistance) {
		this.resistance = resistance;
		e.setResistance(resistance);
		return 0;
	}
	
	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
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

	//Update:-------------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		// TODO Auto-generated method stub
		
	}

	//Persistence:-----------------------------------------------------------------------------------

	@Override
	public void save(StringBuilder writer) {
		writer.append(this.getClass().getSimpleName());
		writer.append(": {");				
		writer.append("resistance:");
		writer.append(resistance);

		writer.append(", inputPos:");
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
