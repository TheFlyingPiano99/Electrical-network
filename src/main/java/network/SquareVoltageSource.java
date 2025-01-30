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
public class SquareVoltageSource extends Component {
	private Edge e;
	private double sourceVoltageAmplitude = 1.0;
	private double sourceVoltageAngularFrequency = 2.0 * Math.PI;
	private double sourceVoltagePhase = 0.0;

	//Constructors:---------------------------------------------------------------------------------------

	public SquareVoltageSource() {
	}


	public SquareVoltageSource(double u, double omega) {
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
		setSourceVoltageAngularFrequency(sourceVoltageAngularFrequency);
	}

	public void setSourceVoltageAngularFrequency(double omega) {
		Vector representedAngularFrequencies = getParent().getAngularFrequencies();

		Vector source = new Vector(e.getImpedance().dimension);
		source.fill(new Complex(0, 0));
		int n = 1;
		int maxN = 1000;		// To prevent too long loadings
		int idx_ = getParent().getFrequencyIndex(omega);
		sourceVoltageAngularFrequency = representedAngularFrequencies.at(idx_).getRe();
		for (
				int idx = idx_;
				idx < source.dimension - 1 && n <= maxN;
				idx = getParent().getFrequencyIndex(omega * n)
		) {
			source.setAt(idx, Complex.euler(4.0 * sourceVoltageAmplitude / (double)n / Math.PI, Math.PI / 2 + sourceVoltagePhase));
			n += 2;
		}
		e.setSourceVoltage(source);
	}

	public double getSourceVoltagePhase()
	{
		return sourceVoltagePhase;
	}

	public void setSourceVoltagePhase(double phase)
	{
		this.sourceVoltagePhase = phase;
		setSourceVoltageAngularFrequency(this.sourceVoltageAngularFrequency);
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
		prop.value = String.valueOf(getSourceVoltagePhase());
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
		writer.append(sourceVoltagePhase);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		sourceVoltageAmplitude = Double.valueOf(pairs[1].split(":")[1]);

		sourceVoltageAngularFrequency = Double.valueOf(pairs[2].split(":")[1]);

		setSourceVoltagePhase(Double.valueOf(pairs[3].split(":")[1]));

		String coordIn[] = pairs[4].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));


		String coordOut[] = pairs[5].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
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
		builder.append("sourceVoltagePhase=");
		builder.append(sourceVoltagePhase);
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

		// Circle:
		int resolution = 32;
		for (int i = 0; i < resolution; i++) {
			double angle0 = i / (double)resolution * 2 * Math.PI;
			double angle1 = (i + 1) / (double)resolution * 2 * Math.PI;
			float x0 = (float)Math.cos(angle0) * 0.3f + 0.5f;
			float y0 = (float)Math.sin(angle0) * 0.3f;
			float x1 = (float)Math.cos(angle1) * 0.3f + 0.5f;
			float y1 = (float)Math.sin(angle1) * 0.3f;
			lines.add(new Line(defaultSize * x0, defaultSize * y0, defaultSize * x1, defaultSize * y1));
		}

		// waveform:
		lines.add(new Line(defaultSize * 0.2f, 0.0f, defaultSize * 0.3f, 0.0f));
		lines.add(new Line(defaultSize * 0.3f, 0.0f, defaultSize * 0.3f, -defaultSize * 0.1f));
		lines.add(new Line(defaultSize * 0.3f, -defaultSize * 0.1f, defaultSize * 0.5f, -defaultSize * 0.1f));
		lines.add(new Line(defaultSize * 0.5f, -defaultSize * 0.1f, defaultSize * 0.5f, -defaultSize * -0.1f));
		lines.add(new Line(defaultSize * 0.5f, -defaultSize * -0.1f, defaultSize * 0.7f, -defaultSize * -0.1f));
		lines.add(new Line(defaultSize * 0.7f, -defaultSize * -0.1f, defaultSize * 0.7f, 0.0f));
		lines.add(new Line(defaultSize * 0.7f, 0.0f, defaultSize * 0.8f, 0.0f));


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

		str = getProperties().get("phase").value;
		if (str != null && str.length() > 0) {
			try {
				double val = Double.parseDouble(str);
				setSourceVoltagePhase(val);

			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.println("Updated value:" + getSourceVoltage());
			getProperties().get("phase").value = String.valueOf(getSourceVoltagePhase());
			getParent().simulate();
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {

		if (updateEditable) {
			setProperty("amplitude", this::getSourceVoltageAmplitude);
			setProperty("angularFrequency", this::getSourceVoltageAngularFrequency);
			setProperty("phase", this::getSourceVoltagePhase);
		}
		setProperty("current", this::getTimeDomainCurrent);
		setProperty("resistance", this::getTimeDomainResistance);
	}

}
