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
 * Voltage meter.
 * 
 * @author Simon Zoltán
 *
 */
public class AnalogVoltmeter extends Component {

	private Edge e;
	private double resistance = 1000000000;
	
	private double scale = 1;
	private float needlePrevAngle = 1.57f;
	
	// Constructors:---------------------------------------------------------------------------------------

	public AnalogVoltmeter() {
	}

	public AnalogVoltmeter(double r) {
		resistance = r;
	}

	// Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return e.getTimeDomainVoltageDrop(); }

	@Override
	public double getTimeDomainResistance() { return resistance; }

	public void setResistance(double r) {
		this.resistance = r;
		e.getImpedance().fill(new Complex(resistance, 0));
	}

	@Override
	public Vector getFrequencyDomainCurrent() {
		return e.getCurrent();
	}

	@Override
	public Vector getFrequencyDomainVoltageDrop() {
		return e.getVoltageDrop();
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

		prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "skála:";
		prop.unit = "deg/V";
		prop.value = String.valueOf(scale);
		getProperties().put("scale", prop);
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
		writer.append(getTimeDomainResistance());
		writer.append("; scale: ");
		writer.append(scale);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");

	}

	@Override
	public void load(String[] pairs) {
		setResistance(Double.valueOf(pairs[1].split(":")[1]));
		scale = Double.valueOf(pairs[2].split(":")[1]);
		String coordIn[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));

		String coordOut[] = pairs[4].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
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
		builder.append("Resistance [resistance=");
		builder.append(getTimeDomainResistance());
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

		// Construction:
		float defaultSize = getDEFAULT_SIZE();
		
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.06f, 0.0f));
		lines.add(new Line(defaultSize * 0.06f, +defaultSize / 3.0f, defaultSize * 0.06f, -defaultSize / 3.0f));
		
		lines.add(new Line(defaultSize * 0.06f, +defaultSize / 3.0f, defaultSize * 0.94f, +defaultSize / 3.0f));
		lines.add(new Line(defaultSize * 0.06f, -defaultSize / 3.0f, defaultSize * 0.94f, -defaultSize / 3.0f));

		lines.add(new Line(defaultSize* 0.94f, +defaultSize / 3.0f, defaultSize* 0.94f, -defaultSize / 3.0f));
		lines.add(new Line(defaultSize* 0.94f, 0.0f, defaultSize, 0.0f));

		//Letter "V":
		lines.add(new Line(
				defaultSize * 0.2f,
				-defaultSize * 0.2f,
				defaultSize * 0.25f,
				-defaultSize * 0.05f
				));
		lines.add(new Line(
				defaultSize * 0.25f,
				-defaultSize * 0.05f,
				defaultSize * 0.3f,
				-defaultSize * 0.2f
				));
		
		//Dial:
		float angle = 0.3927f;
		lines.add(new Line(defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.4f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.4f,
				defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.45f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.45f));
		
		angle = 1.5708f;
		lines.add(new Line(defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.4f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.4f,
				defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.45f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.45f));

		angle = 2.7489f;
		lines.add(new Line(defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.4f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.4f,
				defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.45f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.45f));

		angle = 1.5708f + (float)(getTimeDomainVoltageDrop() * scale) * 0.017453f;
		angle = (angle + needlePrevAngle) / 2.0f;
		if (2.7489f < angle) {
			angle = 2.7489f; 
		}
		else if (0.3927f > angle) {
			angle = 0.3927f;
		}
		needlePrevAngle = angle;
		
		//Needle:
		lines.add(new Line(defaultSize* 0.5f, defaultSize / 3.0f,
				defaultSize* 0.5f - (float)Math.cos(angle) * defaultSize * 0.38f,
				defaultSize / 3.0f - (float)Math.sin(angle) * defaultSize * 0.38f));

		
		// call drawShape
		DrawingHelper.drawShape(ctx,
				getInput().getPos(),
				getOutput().getPos(),
				lines,
				defaultSize,
				getParent().isThisSelected(this),
				0,
				false,
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

	@Override
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

				} catch (RuntimeException e) {
				}
				getProperties().get("resistance").value = String.valueOf(getTimeDomainResistance());
			}

			str = getProperties().get("scale").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					scale = val;

				} catch (RuntimeException e) {
				}
				getProperties().get("scale").value = String.valueOf(scale);
			}
		}
	}

	@Override
	public void updatePropertyView(boolean updateEditable) {
		setProperty("voltage", this::getTimeDomainVoltageDrop);
		setProperty("current", this::getTimeDomainCurrent);
		if (updateEditable) {
			setProperty("resistance", this::getTimeDomainResistance);
			setProperty("scale", this::getScale);
		}
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	@Override
	public AnalogVoltmeter clone() {
		try {
			AnalogVoltmeter clone = (AnalogVoltmeter) super.clone();
			clone.e = this.e.clone();
			clone.resistance = this.resistance;
			clone.scale = this.scale;
			clone.needlePrevAngle = this.needlePrevAngle;
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}

}
