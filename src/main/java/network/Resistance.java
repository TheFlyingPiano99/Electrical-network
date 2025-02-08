package network;

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
 * Resistance with adjustable value.
 * 
 * @author Simon Zoltán
 *
 */
public class Resistance extends Component {

	private Edge e;
	private double resistance = 1000;

	// Constructors:---------------------------------------------------------------------------------------

	public Resistance() {
	}

	@Override
	public void updateFrequencyDependentParameters(ArrayList<Double> simulatedAngularFrequencies) {
		Vector current = new Vector(simulatedAngularFrequencies.size());
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		Vector impedance = new Vector(simulatedAngularFrequencies.size());
		impedance.fill(new Complex(resistance, 0));
		e.setImpedance(impedance);
		Vector sourceVoltage = new Vector(simulatedAngularFrequencies.size());
		sourceVoltage.fill(new Complex(0, 0));
		e.setSourceVoltage(sourceVoltage);

		Vector inputCurrentVector = new Vector(simulatedAngularFrequencies.size());
		inputCurrentVector.fill(new Complex(0, 0));
		e.getInput().setInputCurrent(inputCurrentVector);
		e.getOutput().setInputCurrent(inputCurrentVector);
	}

	public Resistance(double r) {
		this.resistance = r;
		e.getImpedance().fill(new Complex(resistance, 0));
	}

	// Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return e.getTimeDomainVoltageDrop(); }

	@Override
	public double getTimeDomainResistance() { return resistance; }

	public void setResistance(double resistance) {
		this.resistance = resistance;
		Vector imp = new Vector(e.getImpedance().dimension);
		imp.fill(new Complex(resistance, 0));
		e.setImpedance(imp);
	}

	@Override
	public Vector getFrequencyDomainCurrent() {
		return e.getCurrent();
	}

	@Override
	public Vector getFrequencyDomainVoltageDrop() {
		return e.getVoltageDrop();
	}
	// Build/Destroy:------------------------------------------------------------------------------------

	@Override
	public void build() {
		generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		this.updateFrequencyDependentParameters(getParent().getSimulatedAngularFrequencies());

		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());

		// Properties:
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
		prop.editable = true;
		prop.name = "ellenállás:";
		prop.unit = "Ohm";
		prop.value = String.valueOf(getTimeDomainResistance());
		getProperties().put("resistance", prop);

	}

	@Override
	public void destroy() {
		removeEndNodes();
		super.getParent().removeEdge(e);
	}

	// Persistence:-----------------------------------------------------------------------------------

	@Override
	public void save(StringBuilder writer) {

		writer.append("class: ");
		writer.append(this.getClass().getCanonicalName());
		writer.append("; resistance: ");
		writer.append(resistance);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");

	}

	@Override
	public void load(String[] pairs) {
		setResistance(Double.valueOf(pairs[1].split(":")[1]));

		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));

		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));

		updatePropertyView(true);
	}

	@Override
	public void updateTimeDomainParameters(double totalTimeSec, ArrayList<Double> omegas) {
		e.updateTimeDomainParameters(omegas, totalTimeSec);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Resistance [resistance=");
		builder.append(resistance);
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

		// Construction:
		float defaultSize = getDEFAULT_SIZE();
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.25f, 0.0f));
		lines.add(new Line(defaultSize * 0.25f, +defaultSize * 0.1f, defaultSize * 0.25f, -defaultSize * 0.1f));
		
		lines.add(new Line(defaultSize * 0.25f, +defaultSize * 0.1f, defaultSize * 0.75f, +defaultSize * 0.1f));
		lines.add(new Line(defaultSize * 0.25f, -defaultSize * 0.1f, defaultSize * 0.75f, -defaultSize * 0.1f));

		lines.add(new Line(defaultSize* 0.75f, +defaultSize * 0.1f, defaultSize* 0.75f, -defaultSize * 0.1f));
		lines.add(new Line(defaultSize* 0.75f, 0.0f, defaultSize, 0.0f));

		// call drawShape
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

		// System.out.println("Resistance draw!");
	}

	@Override
	public void disconnectGraphRepresentation() {

		getParent().disconnectEndOfEdge(e, e.getInput());
		getInput().setVertexBinding(e.getInput());

		getParent().disconnectEndOfEdge(e, e.getOutput());
		getOutput().setVertexBinding(e.getOutput());
	}

	public void reset() {
		e.getCurrent().fill(new Complex(0, 0));
		updatePropertyView(false);
	}

	@Override
	public void updatePropertyModel() {
		synchronized (getParent().getMutexObj())
		{
			String str = getProperties().get("resistance").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					setResistance(val);

				} catch (Exception e) {
					e.printStackTrace();
				}
				getProperties().get("resistance").value = String.valueOf(getTimeDomainResistance());
				getParent().evaluate();
			}
		}
	}

	@Override
	public void updatePropertyView(boolean updateEditable) {
		setProperty("voltage", this::getTimeDomainVoltageDrop);
		setProperty("current", this::getTimeDomainCurrent);
		if (updateEditable) {
			setProperty("resistance", this::getTimeDomainResistance);
		}
	}


    @Override
    public Resistance clone() {
        try {
            Resistance clone = (Resistance) super.clone();
            clone.e = this.e.clone();
			clone.resistance = this.resistance;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
