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
	private double sourceVoltagePhaseRad = 0;

	//Constructors:---------------------------------------------------------------------------------------

	public SinusoidalVoltageSource() {
	}

	@Override
	public void updateFrequencyDependentParameters(ArrayList<Double> simulatedAngularFrequencies) {
		Vector current = new Vector(simulatedAngularFrequencies.size());
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		Vector impedance = new Vector(simulatedAngularFrequencies.size());
		impedance.fill(new Complex(0, 0));
		e.setImpedance(impedance);

		Vector source = new Vector(simulatedAngularFrequencies.size());
		source.fill(new Complex(0, 0));
		int frequencyIdx = getParent().getAngularFrequencyIndex(this.sourceVoltageAngularFrequency);
		source.setAt(frequencyIdx,
				Complex.euler(this.sourceVoltageAmplitude, this.sourceVoltagePhaseRad - Math.PI / 2));	// At zero frequency -- constant source
		e.setSourceVoltage(source);

		Vector inputCurrentVector = new Vector(simulatedAngularFrequencies.size());
		inputCurrentVector.fill(new Complex(0, 0));
		e.getInput().setInputCurrent(inputCurrentVector);
		e.getOutput().setInputCurrent(inputCurrentVector);
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

	public double getSourceVoltagePhaseRad() { return sourceVoltagePhaseRad; }

	public void setSourceVoltageAmplitude(double sourceVoltageAmplitude) {
		this.sourceVoltageAmplitude = sourceVoltageAmplitude;
		if (e != null) {
			int frequencyIdx = getParent().getAngularFrequencyIndex(this.sourceVoltageAngularFrequency);
			e.getSourceVoltage().setAt(frequencyIdx, Complex.euler(sourceVoltageAmplitude, sourceVoltagePhaseRad - Math.PI / 2));	// At zero frequency -- constant source
		}
	}

	public void setSourceVoltageAngularFrequency(double omega) {
		if (omega == this.sourceVoltageAngularFrequency) {
			return;
		}
		getParent().releaseAngularFrequency(this.sourceVoltageAngularFrequency);
		this.sourceVoltageAngularFrequency = omega;
		if (e != null) {
			int frequencyIdx = getParent().requestAngularFrequency(this.sourceVoltageAngularFrequency);
			math.Vector source =  Vector.Zeros(getParent().getSimulatedAngularFrequencies().size());
			source.fill(new Complex(0, 0));
			source.setAt(frequencyIdx, Complex.euler(sourceVoltageAmplitude, sourceVoltagePhaseRad - Math.PI / 2));	// At zero frequency -- constant source
			e.setSourceVoltage(source);
		}
	}

	public void setSourceVoltagePhaseRad(double phase) {
		this.sourceVoltagePhaseRad = phase;
		if (e != null) {
			int frequencyIdx = getParent().requestAngularFrequency(this.sourceVoltageAngularFrequency);
			e.getSourceVoltage().setAt(frequencyIdx, Complex.euler(sourceVoltageAmplitude, sourceVoltagePhaseRad - Math.PI / 2));	// At zero frequency -- constant source
		}
	}

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return e.getTimeDomainSourceVoltage(); }

	@Override
	public double getTimeDomainResistance() { return 0; }

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
	public void build() {
		super.generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		getParent().requestAngularFrequency(this.sourceVoltageAngularFrequency);

		this.updateFrequencyDependentParameters(getParent().getSimulatedAngularFrequencies());

		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());


		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás amplitúdó:";
		prop.unit = "V";
		prop.value = String.valueOf(getSourceVoltageAmplitude());
		getProperties().put("amplitude", prop);

		prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás körfrekvencia:";
		prop.unit = "rad/s";
		prop.value = String.valueOf(getSourceVoltageAngularFrequency());
		getProperties().put("angularFrequency", prop);

		prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás fázis:";
		prop.unit = "rad";
		prop.value = String.valueOf(getSourceVoltagePhaseRad());
		getProperties().put("phase", prop);

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
		super.getParent().releaseAngularFrequency(this.sourceVoltageAngularFrequency);
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
		writer.append("; phase: ");
		writer.append(sourceVoltagePhaseRad);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		sourceVoltageAmplitude = Double.valueOf(pairs[1].split(":")[1]);

		setSourceVoltageAngularFrequency(Double.valueOf(pairs[2].split(":")[1]));

		setSourceVoltagePhaseRad(Double.valueOf(pairs[3].split(":")[1]));

		String coordIn[] = pairs[4].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));


		String coordOut[] = pairs[5].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
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
		builder.append("VoltageSource [");
		builder.append("sourceVoltageAmplitude=");
		builder.append(sourceVoltageAmplitude);
		builder.append("sourceVoltageAngularFrequency=");
		builder.append(sourceVoltageAngularFrequency);
		builder.append("sourceVoltagePhase=");
		builder.append(sourceVoltagePhaseRad);
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
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.2f, 0.0f));
		int resolution = 32;
		// Circle:
		for (int i = 0; i < resolution; i++) {
			double angle0 = i / (double)resolution * 2 * Math.PI;
			double angle1 = (i + 1) / (double)resolution * 2 * Math.PI;
			float x0 = (float)Math.cos(angle0) * 0.3f + 0.5f;
			float y0 = (float)Math.sin(angle0) * 0.3f;
			float x1 = (float)Math.cos(angle1) * 0.3f + 0.5f;
			float y1 = (float)Math.sin(angle1) * 0.3f;
			lines.add(new Line(defaultSize * x0, defaultSize * y0, defaultSize * x1, defaultSize * y1));
		}
		// Waveform:
		for (int i = 0; i < resolution; i++) {
			float x0 = 0.2f + i / (float)resolution * 0.6f;
			float y0 = -0.1f * (float)Math.sin(i / (double)resolution * 2 * Math.PI);
			float x1 = 0.2f + (i + 1) / (float)resolution * 0.6f;
			float y1 = -0.1f * (float)Math.sin((i + 1) / (double)resolution * 2 * Math.PI);
			lines.add(new Line(defaultSize * x0, defaultSize * y0, defaultSize * x1, defaultSize * y1));
		}
		lines.add(new Line(defaultSize * 0.8f, 0.0f, defaultSize, 0.0f));

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
		synchronized (getParent().getMutexObj())
		{
			String str = getProperties().get("amplitude").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					setSourceVoltageAmplitude(val);

				} catch (RuntimeException e) {
				}
				//System.out.println("Updated value:" + getSourceVoltage());
				getProperties().get("amplitude").value = String.valueOf(getSourceVoltageAmplitude());
				getParent().evaluate(true);
			}

			str = getProperties().get("angularFrequency").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					setSourceVoltageAngularFrequency(val);

				} catch (RuntimeException e) {
				}
				//System.out.println("Updated value:" + getSourceVoltage());
				getProperties().get("angularFrequency").value = String.valueOf(getSourceVoltageAngularFrequency());
				getParent().evaluate(true);
			}

			str = getProperties().get("phase").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					setSourceVoltagePhaseRad(val);
				} catch (RuntimeException e) {
				}
				//System.out.println("Updated value:" + getSourceVoltage());
				getProperties().get("phase").value = String.valueOf(getSourceVoltagePhaseRad());
				getParent().evaluate(true);
			}
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {

		if (updateEditable) {
			setProperty("amplitude", this::getSourceVoltageAmplitude);
			setProperty("angularFrequency", this::getSourceVoltageAngularFrequency);
			setProperty("phase", this::getSourceVoltagePhaseRad);
		}
		setProperty("current", this::getTimeDomainCurrent);
		setProperty("resistance", this::getTimeDomainResistance);
	}

    @Override
    public SinusoidalVoltageSource clone() {
        try {
            SinusoidalVoltageSource clone = (SinusoidalVoltageSource) super.clone();
            clone.e =  this.e.clone();
			clone.sourceVoltageAmplitude = this.sourceVoltageAmplitude;
			clone.sourceVoltageAngularFrequency = this.sourceVoltageAngularFrequency;
			clone.sourceVoltagePhaseRad = this.sourceVoltagePhaseRad;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
