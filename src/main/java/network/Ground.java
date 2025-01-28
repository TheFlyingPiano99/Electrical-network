package network;

import javafx.util.Duration;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import gui.DrawingHelper;
import math.Complex;
import math.Coordinate;
import math.Line;
import math.Vector;

/**
 * Current input, with adjustable value.
 * @author Simon Zoltán
 *
 */

public class Ground extends Component {
	private Edge e;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public Ground() {
	}
			
	//Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return e.getTimeDomainVoltageDrop(); }

	@Override
	public double getTimeDomainResistance() { return e.getTimeDomainResistance(); }

	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
		super.generateEndNodes();
		
		e = new Edge();
		super.getParent().addEdgeWithGroundedOutput(e);

		Vector omega = getParent().getAngularFrequencies();
		Vector current = new Vector(omega.dimension);
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		Vector impedance = new Vector(omega.dimension);
		impedance.fill(new Complex(0, 0));
		e.setImpedance(impedance);
		Vector sourceVoltage = new Vector(omega.dimension);
		sourceVoltage.fill(new Complex(0, 0));
		e.setSourceVoltage(sourceVoltage);

		
		getInput().setVertexBinding(e.getInput());
		//getOutput().setVertexBinding(e.getOutput());
				
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "elnyelt áram:";
		prop.unit = "A";
		prop.value = String.valueOf(getTimeDomainCurrent());
		getProperties().put("current", prop);
	}
	
	@Override
	public void destroy() {		
		super.removeEndNodes();
		
		super.getParent().removeEdge(e);
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
		builder.append("Ground [");
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
		updatePropertyView(false);
		List<Line> lines = new ArrayList<Line>();

		float  defaultSize = getDEFAULT_SIZE();
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.6f, 0.0f));

		lines.add(new Line(defaultSize * 0.6f, -defaultSize * 0.24f, defaultSize * 0.6f, +defaultSize * 0.24f));
		lines.add(new Line(defaultSize * 0.8f, -defaultSize * 0.12f, defaultSize * 0.8f, +defaultSize * 0.12f));
		lines.add(new Line(defaultSize, -defaultSize * 0.06f, defaultSize, +defaultSize * 0.06f));

		//lines.add(new Line(defaultSize * 0.6f, 0.0f, defaultSize, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx,
				getInput().getPos(),
				getOutput().getPos(),
				lines,
				defaultSize,
				getParent().isThisSelected(this),
				getCurrentVisualisationOffset(),
				true,
				(float)e.getInput().getTimeDomainPotential(),
				(float)e.getOutput().getTimeDomainPotential());
	}


	@Override
	public void disconnectGraphRepresentation() {
		
		getParent().disconnectEndOfEdge(e, e.getInput());
		getInput().setVertexBinding(e.getInput());
		
		//getParent().disconnectEndOfEdge(e, e.getOutput());
		//getOutput().setVertexBinding(e.getOutput());
	}


	@Override
	public void reset() {
		e.getCurrent().fill(new Complex(0, 0));
		updatePropertyView(false);
	}


	@Override
	public void updatePropertyModel() {
	}


	@Override
	public void updatePropertyView(boolean updateEditable)
	{
		setProperty("current", this::getTimeDomainCurrent);
	}

	public void increaseCurrentVisualisationOffset(double totalTimeSec) {
		double pres = currentVisualisationOffset;
		currentVisualisationOffset = (totalTimeSec * e.getTimeDomainCurrent() * currentVisualisationSpeed) % DEFAULT_SIZE;

		Double test = Double.valueOf(currentVisualisationOffset);
		if (test.isNaN()) {
			currentVisualisationOffset = pres;
		}
	}

}
