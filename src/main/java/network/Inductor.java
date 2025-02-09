package network;

import java.util.*;

import javafx.scene.canvas.GraphicsContext;
import gui.DrawingHelper;
import math.Complex;
import math.Coordinate;
import math.Line;
import math.Vector;

/**
 * Ideal inductor, with adjustable value and zero resistance.
 * @author Simon Zoltán
 *
 */
public class Inductor extends Component {
	private Edge e;
	private double inductance = 0.000001;
	private final float DEFAULT_SIZE = 60.0f;
	private double wireResistance = 0.0;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public Inductor() {
	}

	@Override
	public void updateFrequencyDependentParameters(ArrayList<Double> simulatedAngularFrequencies) {
		math.Vector current = new math.Vector(simulatedAngularFrequencies.size());
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		math.Vector impedance = new math.Vector(simulatedAngularFrequencies.size());
		for (int i = 0; i < impedance.dimension; i++) {
			impedance.setAt(
					i,
					new Complex(0, 2 * Math.PI * simulatedAngularFrequencies.get(i) * inductance)
			);
		}
		e.setImpedance(impedance);
		math.Vector sourceVoltage = new Vector(simulatedAngularFrequencies.size());
		sourceVoltage.fill(new Complex(0, 0));
		e.setSourceVoltage(sourceVoltage);

		Vector inputCurrentVector = new Vector(simulatedAngularFrequencies.size());
		inputCurrentVector.fill(new Complex(0, 0));
		e.getInput().setInputCurrent(inputCurrentVector);
		e.getOutput().setInputCurrent(inputCurrentVector);
	}


	public Inductor(double l) {
		inductance = l;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return e.getTimeDomainVoltageDrop(); }

	@Override
	public double getTimeDomainResistance() { return wireResistance; }

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

		prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "Induktivitás:";
		prop.unit = "H";
		prop.value = String.valueOf(inductance);
		getProperties().put("inductance", prop);
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
		writer.append("; inductance: ");
		writer.append(inductance);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		setInductance(Double.valueOf(pairs[1].split(":")[1]));
		
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

	public double getInductance() {
		return inductance;
	}


	public void setInductance(double inductance) {
		this.inductance = inductance;
		ArrayList<Double> omega = getParent().getSimulatedAngularFrequencies();
		for (int i = 0; i < e.getImpedance().dimension; i++) {
			e.getImpedance().setAt(
					i,
					new Complex(0, 2 * Math.PI * omega.get(i) * inductance)
			);
		}
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VoltageSource [");
		builder.append("inductance=");
		builder.append(inductance);
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
		lines.add(new Line(0.0f, 0.0f, DEFAULT_SIZE * 0.2f, 0.0f));
		//Bumps:
		//1.
		lines.add(new Line(
				DEFAULT_SIZE * 0.2f,
				0.0f,
				DEFAULT_SIZE * 0.27f,
				-DEFAULT_SIZE * 0.13f
				));
		lines.add(new Line(
				DEFAULT_SIZE * 0.27f,
				-DEFAULT_SIZE * 0.13f,
				DEFAULT_SIZE * 0.33f,
				-DEFAULT_SIZE * 0.13f
				));
		lines.add(new Line(
				DEFAULT_SIZE * 0.33f,
				-DEFAULT_SIZE * 0.13f,
				DEFAULT_SIZE * 0.4f,
				0.0f
				));
		//2.
		lines.add(new Line(
				DEFAULT_SIZE * 0.4f,
				0.0f,
				DEFAULT_SIZE * 0.47f,
				-DEFAULT_SIZE * 0.13f
				));
		lines.add(new Line(
				DEFAULT_SIZE * 0.47f,
				-DEFAULT_SIZE * 0.13f,
				DEFAULT_SIZE * 0.53f,
				-DEFAULT_SIZE * 0.13f
				));
		lines.add(new Line(
				DEFAULT_SIZE * 0.53f,
				-DEFAULT_SIZE * 0.13f,
				DEFAULT_SIZE * 0.6f,
				0.0f
				));

		//3.
		lines.add(new Line(
				DEFAULT_SIZE * 0.6f,
				0.0f,
				DEFAULT_SIZE * 0.67f,
				-DEFAULT_SIZE * 0.13f
				));
		lines.add(new Line(
				DEFAULT_SIZE * 0.67f,
				-DEFAULT_SIZE * 0.13f,
				DEFAULT_SIZE * 0.73f,
				-DEFAULT_SIZE * 0.13f
				));
		lines.add(new Line(
				DEFAULT_SIZE * 0.73f,
				-DEFAULT_SIZE * 0.13f,
				DEFAULT_SIZE * 0.8f,
				0.0f
				));

		lines.add(new Line(DEFAULT_SIZE * 0.8f, 0.0f, DEFAULT_SIZE, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx,
				getInput().getPos(),
				getOutput().getPos(),
				lines,
				DEFAULT_SIZE,
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
			String str = getProperties().get("inductance").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					setInductance(val);

				} catch (RuntimeException e) {
				}
				//System.out.println("Updated value:" + getInductance());
				getProperties().get("inductance").value = String.valueOf(getInductance());
				getParent().evaluate();
			}
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {
		setProperty("voltage", this::getTimeDomainVoltageDrop);
		setProperty("current", this::getTimeDomainCurrent);
		if (updateEditable) {
			setProperty("inductance", this::getInductance);
		}		
	}

    @Override
    public Inductor clone() {
        try {
            Inductor clone = (Inductor) super.clone();
			clone.e = this.e.clone();
			clone.inductance = this.inductance;
			clone.wireResistance = this.wireResistance;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
