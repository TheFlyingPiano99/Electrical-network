package network;

import gui.DrawingHelper;
import javafx.scene.canvas.GraphicsContext;
import math.Complex;
import math.Coordinate;
import math.Line;
import math.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Ideal sinusoidal voltage source, with adjustable amplitude and frequency zero resistance.
 * @author Simon Zoltán
 *
 */
public class SinusoidalVoltageSource extends Component {
	private Edge e;
	private double sourceVoltageAmplitude = 1.0;
	private double sourceVoltageAngularFrequency = 2.0 * Math.PI;
	private int frequencyIdx = 0;

	//Constructors:---------------------------------------------------------------------------------------

	public SinusoidalVoltageSource() {
	}


	public SinusoidalVoltageSource(double u, double omega) {
		sourceVoltageAmplitude = u;
		sourceVoltageAngularFrequency = omega;
	}

	//Getters/Setters:------------------------------------------------------------------------------------

	public double getSourceVoltageAmplitude() {
		return sourceVoltageAmplitude;
	}

	public double getSourceVoltageAngularFrequency() { return sourceVoltageAngularFrequency; }

	public void setSourceVoltageAmplitude(double sourceVoltageAmplitude) {
		this.sourceVoltageAmplitude = sourceVoltageAmplitude;
		if (e != null) {
			Vector source = new Vector(e.getImpedance().dimension);
			source.fill(new Complex(0, 0));
			source.setAt(frequencyIdx, new Complex(sourceVoltageAmplitude, 0));	// At zero frequency -- constant source
			e.setSourceVoltage(source);
		}
	}

	public void setSourceVoltageAngularFrequency(double omega) {
		Vector representedAngularFrequencies = getParent().getAngularFrequencies();
		boolean foundSimilar = false;
		for (int i = 0; i < representedAngularFrequencies.dimension; i++)
		{
			if (Math.abs(representedAngularFrequencies.at(i).getRe() - omega) < getParent().getAngularFrequencyStep() / 2) {
				frequencyIdx = i;
				sourceVoltageAngularFrequency = representedAngularFrequencies.at(i).getRe();
				foundSimilar = true;
				break;
			}
		}
		if (!foundSimilar) {	// Set to the highest available
			frequencyIdx = representedAngularFrequencies.dimension - 1;
			sourceVoltageAngularFrequency = representedAngularFrequencies.at(frequencyIdx).getRe();
		}

		setSourceVoltageAmplitude(this.sourceVoltageAmplitude);		// Move amplitude to the correct vector component
	}

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return -e.getTimeDomainSourceVoltage(); }

	@Override
	public double getTimeDomainResistance() { return sourceVoltageAmplitude / e.getTimeDomainCurrent(); }

	//Build/Destroy:------------------------------------------------------------------------------------

	@Override
	public void build() {
		super.generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		Vector omega = getParent().getAngularFrequencies();
		Vector current = new Vector(omega.dimension);
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		Vector impedance = new Vector(omega.dimension);
		impedance.fill(new Complex(0, 0));
		e.setImpedance(impedance);
		this.setSourceVoltageAngularFrequency(this.sourceVoltageAngularFrequency);

		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());



		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás feszültség amplitúdó:";
		prop.unit = "V";
		prop.value = String.valueOf(getSourceVoltageAmplitude());
		getProperties().put("amplitude", prop);

		prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás feszültség körfrekvencia:";
		prop.unit = "";
		prop.value = String.valueOf(getSourceVoltageAngularFrequency());
		getProperties().put("angularFrequency", prop);

		prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "áramerősség:";
		prop.unit = "A";
		prop.value = String.valueOf(0.0);
		getProperties().put("current", prop);

		prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "(ellenállás):";
		prop.unit = "Ohm";
		prop.value = String.valueOf(0.0);
		getProperties().put("resistance", prop);
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
		writer.append("; voltage: ");
		writer.append(sourceVoltageAmplitude);
		writer.append("; frequency: ");
		writer.append(sourceVoltageAngularFrequency);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		setSourceVoltageAmplitude(Double.valueOf(pairs[1].split(":")[1]));

		setSourceVoltageAngularFrequency(Double.valueOf(pairs[2].split(":")[1]));

		String coordIn[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));


		String coordOut[] = pairs[4].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));

		updatePropertyView(true);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VoltageSource [");
		builder.append("sourceVoltageAmplitude=");
		builder.append(sourceVoltageAmplitude);
		builder.append("sourceVoltageAngularFrequency=");
		builder.append(sourceVoltageAngularFrequency);
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

		//Construction:
		float defaultSize = getDEFAULT_SIZE();
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.4f, 0.0f));
		lines.add(new Line(defaultSize * 0.4f, -defaultSize * 0.08f, defaultSize * 0.4f, +defaultSize * 0.08f));
		lines.add(new Line(defaultSize * 0.6f, -defaultSize* 0.2f, defaultSize * 0.6f, +defaultSize * 0.2f));
		lines.add(new Line(defaultSize * 0.6f, 0.0f, defaultSize, 0.0f));

		// [+] sign
		lines.add(new Line(
				(5.0f/6.0f * defaultSize) - (1.0f/15.0f * defaultSize),
				-1.0f/4.0f * defaultSize,
				(5.0f/6.0f * defaultSize) + (1.0f/15.0f * defaultSize),
				-1.0f/4.0f * defaultSize));
		lines.add(new Line(
				(5.0f/6.0f * defaultSize),
				-1.0f/4.0f * defaultSize - (1.0f/15.0f * defaultSize),
				(5.0f/6.0f * defaultSize),
				-1.0f/4.0f * defaultSize + (1.0f/15.0f * defaultSize)));

		// [-] sign
		lines.add(new Line(
				(1.0f/6.0f * defaultSize) - (1.0f/15.0f * defaultSize),
				-1.0f/4.0f * defaultSize,
				(1.0f/6.0f * defaultSize) + (1.0f/15.0f * defaultSize),
				-1.0f/4.0f * defaultSize));


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
		String str = getProperties().get("amplitude").value;
		if (str != null && str.length() > 0) {
			try {
				double val = Double.parseDouble(str);
				setSourceVoltageAmplitude(val);

			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.println("Updated value:" + getSourceVoltage());
			getProperties().get("amplitude").value = String.valueOf(getSourceVoltageAmplitude());
			getParent().simulate();
		}

		str = getProperties().get("angularFrequency").value;
		if (str != null && str.length() > 0) {
			try {
				double val = Double.parseDouble(str);
				setSourceVoltageAngularFrequency(val);

			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.println("Updated value:" + getSourceVoltage());
			getProperties().get("angularFrequency").value = String.valueOf(getSourceVoltageAngularFrequency());
			getParent().simulate();
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {

		if (updateEditable) {
			setProperty("amplitude", this::getSourceVoltageAmplitude);
			setProperty("angularFrequency", this::getSourceVoltageAngularFrequency);
		}
		setProperty("current", this::getTimeDomainCurrent);
		setProperty("resistance", this::getTimeDomainResistance);
	}

	public void updateCurrentVisualisationOffset(double totalTimeSec) {
		double pres = currentVisualisationOffset;
		currentVisualisationOffset = (totalTimeSec * e.getTimeDomainCurrent() * currentVisualisationSpeed) % DEFAULT_SIZE;

		Double test = Double.valueOf(currentVisualisationOffset);
		if (test.isNaN()) {
			currentVisualisationOffset = pres;
		}
	}

}
