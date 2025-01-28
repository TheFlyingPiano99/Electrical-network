package network;

import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import gui.DrawingHelper;
import math.*;

/**
 *	Ideal wire, with zero resistance.
 *	HUN: Ideális vezeték, nulla ellenállással.
 * @author Simon Zoltán
 * 
 */
public class Wire extends Component {
	private Edge e;

	//Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public Complex getVoltagePhasor() {
		return e.getVoltageDrop();
	}

	@Override
	public Complex getImpedancePhasor() {
		return e.getImpedance();
	}
	
	@Override
	public Complex getCurrentPhasor() {
		return e.getCurrent();
	}
	
	//Build/Destroy:------------------------------------------------------------------------------------

	@Override
	public void build() {
		generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(new Complex(0, 0));
		e.setImpedance(new Complex(0, 0));
		e.setSourceVoltage(new Complex(0, 0));
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
		
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "feszültség esés:";
		prop.unit = "V";
		prop.value = String.valueOf(0.0);
		getProperties().put("voltage", prop);

		prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "áramerősség:";
		prop.unit = "A";
		prop.value = String.valueOf(0.0);
		getProperties().put("current", prop);

		prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "ellenállás:";
		prop.unit = "Ohm";
		prop.value = String.valueOf(0.0);
		getProperties().put("resistance", prop);
	}

	@Override
	public void destroy() {
		removeEndNodes();
		super.getParent().removeEdge(e);
	}
	
	//Update:---------------------------------------------------------------------------------------------
	
	@Override
	public void update(double omega) {

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
		
		updatePropertyView(true);
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
		List<Line> lines = new ArrayList<Line>();		
		Coordinate inputPos  = getInput().getPos(); 
		Coordinate outputPos = getOutput().getPos();

		//boolean eqX = (outputPos.x == inputPos.x);
		//boolean eqY = (outputPos.y == inputPos.y);
		
		lines.add(new Line(0, 0, getDEFAULT_SIZE(), 0));
		
		DrawingHelper.drawShape(ctx,
				inputPos,
				outputPos,
				lines,
				getDEFAULT_SIZE(),
				getParent().isThisSelected(this),
				getCurrentVisualisationOffset(),
				true,
				(float)e.getInput().getPotential().getRe(),
				(float)e.getOutput().getPotential().getRe());

/*
		//Construction:
		if (eqX || eqY) {
			lines.add(new Line(inputPos.x, inputPos.y, outputPos.x, outputPos.y));
		} else {

			// 2 segments: [input -> breaking point] and [breaking point -> output]
			int brX = outputPos.x;
			int brY = outputPos.y;
			if (outputPos.x != inputPos.x) {
				brX = inputPos.x;
			} else if (outputPos.y != inputPos.y) {
				brY = inputPos.y;
			}
			lines.add(new Line(inputPos.x, inputPos.y, brX, brY));
			lines.add(new Line(brX, brY, outputPos.x, outputPos.y));

		}

		//call drawWire
		DrawingHelper.drawWire(ctx, lines);
*/
	}

	@Override
	public
	void disconnectGraphRepresentation() {
		getParent().disconnectEndOfEdge(e, e.getInput());
		getInput().setVertexBinding(e.getInput());
		
		getParent().disconnectEndOfEdge(e, e.getOutput());
		getOutput().setVertexBinding(e.getOutput());
	}

	@Override
	public
	void reset() {		
		e.setCurrent(new Complex(0, 0));
		updatePropertyView(false);

	}

	@Override
	public
	void updatePropertyModel() {
	}

	@Override
	public void updatePropertyView(boolean updateEditable) {
		//setProperty("current", this::getCurrentPhasor);
	}

}