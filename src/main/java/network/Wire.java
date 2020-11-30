package main.java.network;

import javafx.util.Duration;

import javafx.scene.canvas.GraphicsContext;
import main.java.math.Coordinate;

/**
 *	Ideal wire, with zero resistance.
 * @author Simon Zoltán
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
		writer.append("class: ");				
		writer.append(this.getClass().getCanonicalName());		
		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		String coordIn[] = pairs[1].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		String coordOut[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Wire [");
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
		throw new RuntimeException("Not implemented!");
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
		}
	}

}