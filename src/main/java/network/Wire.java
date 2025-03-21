package network;

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
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() {
		return 0;
	}

	@Override
	public double getTimeDomainResistance() {
		return 0;
	}

	@Override
	public Vector getFrequencyDomainCurrent() {
		return e.getCurrent();
	}

	@Override
	public Vector getFrequencyDomainVoltageDrop() {
		return e.getVoltageDrop();
	}

	//Build/Destroy:------------------------------------------------------------------------------------

	@Override
	public void updateFrequencyDependentParameters(ArrayList<Double> simulatedAngularFrequencies) {
		Vector current = new Vector(simulatedAngularFrequencies.size());
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		Vector impedance = new Vector(simulatedAngularFrequencies.size());
		impedance.fill(new Complex(0, 0));
		e.setImpedance(impedance);
		Vector sourceVoltage = new Vector(simulatedAngularFrequencies.size());
		sourceVoltage.fill(new Complex(0, 0));
		e.setSourceVoltage(sourceVoltage);

		Vector inputCurrentVector = new Vector(simulatedAngularFrequencies.size());
		inputCurrentVector.fill(new Complex(0, 0));
		e.getInput().setInputCurrent(inputCurrentVector);
		e.getOutput().setInputCurrent(inputCurrentVector);
	}

	@Override
	public void build() {
		generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		this.updateFrequencyDependentParameters(getParent().getSimulatedAngularFrequencies());

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
	}

	@Override
	public void destroy() {
		removeEndNodes();
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
	public void updateTimeDomainParameters(double totalTimeSec, ArrayList<Double> omegas) {
		e.updateTimeDomainParameters(omegas, totalTimeSec);
	}

	@Override
	public void updateTimeDomainParametersUsingSpecificFrequencies(double totalTimeSec, ArrayList<Double> omegas, ArrayList<Integer> frequencyIndices) {
		e.updateTimeDomainParametersUsingSpecificFrequencies(omegas, frequencyIndices, totalTimeSec);
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
		updatePropertyView(false);
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
				getParent().isValid(),
				(float)e.getInput().getTimeDomainPotential(),
				(float)e.getOutput().getTimeDomainPotential());

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
		e.getCurrent().fill(new Complex(0, 0));
		updatePropertyView(false);
	}

	@Override
	public
	void updatePropertyModel() {
	}

	@Override
	public void updatePropertyView(boolean updateEditable) {
		setProperty("current", this::getTimeDomainCurrent);
	}

    @Override
    public Wire clone() {
        try {
            Wire clone = (Wire) super.clone();
            clone.e = this.e.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}