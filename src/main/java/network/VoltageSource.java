package main.java.network;

import javafx.util.Duration;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import main.java.gui.DrawingHelper;
import main.java.math.Coordinate;
import main.java.math.Line;

/**
 * Ideal voltage source, with adjustable value and zero resistance.
 * @author Simon Zoltán
 *
 */
public class VoltageSource extends Component {
	private Edge e;
	private float sourceVoltage = 0;
	private final float DEFAULT_SIZE = 60.0f;
	
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
		if (e != null) {
			e.setSourceVoltage(sourceVoltage);
		}
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
		writer.append("class: ");				
		writer.append(this.getClass().getCanonicalName());
		writer.append("; voltage: ");
		writer.append(sourceVoltage);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		setSourceVoltage(Float.valueOf(pairs[1].split(":")[1]));
		
		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		
		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VoltageSource [");
		builder.append("sourceVoltage=");
		builder.append(sourceVoltage);
		builder.append(", inputPos= [");
		builder.append(getInput().getPos().x);
		builder.append(",");		
		builder.append(getInput().getPos().y);
		builder.append("]");		
		builder.append(", outputPos= [");
		builder.append(getOutput().getPos().x);
		builder.append(",");		
		builder.append(getOutput().getPos().y);
		builder.append("]");		
		builder.append("]");
		return builder.toString();
	}


	@Override
	public void draw(GraphicsContext ctx) {
		List<Line> lines = new ArrayList<Line>();

		//Construction:
		lines.add(new Line(0.0f, 0.0f, DEFAULT_SIZE/3.0f, 0.0f));
		lines.add(new Line(DEFAULT_SIZE/3.0f, -DEFAULT_SIZE/4.0f, DEFAULT_SIZE/3.0f, +DEFAULT_SIZE/4.0f));
		lines.add(new Line(2.0f/3.0f * DEFAULT_SIZE, -DEFAULT_SIZE/2.0f, 2.0f/3.0f * DEFAULT_SIZE, +DEFAULT_SIZE/2.0f));

		// [+] sign
		lines.add(new Line(
				(5.0f/6.0f * DEFAULT_SIZE) - (1.0f/15.0f * DEFAULT_SIZE),  
				-1.0f/4.0f * DEFAULT_SIZE,
				(5.0f/6.0f * DEFAULT_SIZE) + (1.0f/15.0f * DEFAULT_SIZE),
				-1.0f/4.0f * DEFAULT_SIZE));
		lines.add(new Line(
				(5.0f/6.0f * DEFAULT_SIZE),  
				-1.0f/4.0f * DEFAULT_SIZE - (1.0f/15.0f * DEFAULT_SIZE),
				(5.0f/6.0f * DEFAULT_SIZE),
				-1.0f/4.0f * DEFAULT_SIZE + (1.0f/15.0f * DEFAULT_SIZE)));

		// [-] sign
		lines.add(new Line(
				(1.0f/6.0f * DEFAULT_SIZE) - (1.0f/15.0f * DEFAULT_SIZE),  
				-1.0f/4.0f * DEFAULT_SIZE,
				(1.0f/6.0f * DEFAULT_SIZE) + (1.0f/15.0f * DEFAULT_SIZE),
				-1.0f/4.0f * DEFAULT_SIZE));

		lines.add(new Line(2.0f/3.0f * DEFAULT_SIZE, 0.0f, DEFAULT_SIZE, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx, getInput().getPos(), getOutput().getPos(), lines, DEFAULT_SIZE, isGrabbed());

		System.out.println("VoltageSource draw!");		
	}


	@Override
	void disconnectGraphRepresentation() {
		
		if (getInput().getVertexBinding().getNoOfOutgoing() > 1 || getInput().getVertexBinding().getNoOfIncoming() > 0) {
			//Clone input vertex:
			Vertex prevIn = getInput().getVertexBinding();
			Vertex prevOut = getOutput().getVertexBinding();
			
			Vertex newIn = new Vertex();
			getParent().getVertices().add(newIn);
			
			newIn.addOutgoing(prevOut, e);
			e.setInput(newIn);
			prevOut.removeIncoming(prevIn);
			prevOut.addIncoming(newIn, e);
			
			prevIn.removeOutgoing(prevOut);
			getInput().setVertexBinding(newIn);
		}
		
		if (getOutput().getVertexBinding().getNoOfOutgoing() > 0 || getOutput().getVertexBinding().getNoOfIncoming() > 1) {
			//Clone output vertex:
			Vertex prevIn = getInput().getVertexBinding();
			Vertex prevOut = getOutput().getVertexBinding();
			
			Vertex newOut = new Vertex();
			getParent().getVertices().add(newOut);
			
			newOut.addIncoming(prevIn, e);
			e.setOutput(newOut);
			prevIn.removeOutgoing(prevOut);
			prevIn.addOutgoing(newOut, e);
			
			prevOut.removeIncoming(prevIn);			
			getOutput().setVertexBinding(newOut);
	}
	}

	
}
