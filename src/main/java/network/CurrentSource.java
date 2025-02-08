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
 * Current input, with adjustable value.
 * @author Simon Zoltán
 *
 */



public class CurrentSource extends Component {
	private Edge e;
	private double inputCurrent = 1.0;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public CurrentSource() {
	}

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
		inputCurrentVector.fill(new Complex(this.inputCurrent, 0.0));
		e.getInput().setInputCurrent(inputCurrentVector);
		e.getOutput().setInputCurrent(Vector.Zeros(simulatedAngularFrequencies.size()));
	}


	public CurrentSource(double i) {
		inputCurrent = i;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------

	public double getInputCurrent() {
		return inputCurrent;
	}

	public void setInputCurrent(double inputCurrent) {
		this.inputCurrent = inputCurrent;
		if (null != e) {
			e.getInput().getInputCurrent().fill(new Complex(inputCurrent, 0.0));
		}
	}

	@Override
	public double getTimeDomainCurrent() { return e.getTimeDomainCurrent(); }

	@Override
	public double getTimeDomainVoltageDrop() { return 0; }

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
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
				
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás áram:";
		prop.unit = "A";
		prop.value = String.valueOf(getInputCurrent());
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
		writer.append("; current: ");
		writer.append(getInputCurrent());

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		setInputCurrent(Double.valueOf(pairs[1].split(":")[1]));
		
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
		builder.append("CurrentSource [");
		builder.append("inputCurrent=");
		builder.append(inputCurrent);
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

		//Construction:
		float defaultSize = getDEFAULT_SIZE();
		lines.add(new Line(0.0f, 0.0f, defaultSize, 0.0f));
		
		lines.add(new Line(defaultSize * 0.4f, defaultSize * 0.1f, defaultSize * 0.6f, +defaultSize * 0.0f));
		lines.add(new Line(defaultSize * 0.4f, -defaultSize* 0.1f, defaultSize * 0.6f, +defaultSize * 0.0f));


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
			String str = getProperties().get("current").value;
			if (str != null && str.length() > 0) {
				try {
					double val = Double.parseDouble(str);
					setInputCurrent(val);

				} catch (Exception e) {
					e.printStackTrace();
				}
				getProperties().get("current").value = String.valueOf(getInputCurrent());
				getParent().evaluate();
			}
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {		
		if (updateEditable) {
			setProperty("current", e.getInput()::getTimeDomainInputCurrent);
		}
	}

    @Override
    public CurrentSource clone() {
        try {
            CurrentSource clone = (CurrentSource) super.clone();
			clone.e = this.e.clone();
			clone.inputCurrent = this.inputCurrent;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
