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

/**
 * Capacitor, with adjustable capacity.
 * @author Simon Zoltán
 *
 */
public class Capacitor extends Component {
	private Edge e;
	public double getCharge() {
		return charge;
	}


	public void setCharge(double charge) {
		this.charge = charge;
	}


	public double getCapacity() {
		return capacity;
	}


	private double charge = 0;
	private double capacity = 1;
	private double prevCurrent = 0;
	//Constructors:---------------------------------------------------------------------------------------
	
	public Capacitor() {
	}
	
	
	public Capacitor(double c) {
		capacity = c;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------
	
	//public Complex getSourceVoltage() {
	//	return -(1/capacity);
	//}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	@Override
	public Complex getCurrentPhasor() {
		return e.getCurrent();
	}

	@Override
	public Complex getVoltagePhasor() {
		return e.getVoltageDrop();
	}

	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
		super.generateEndNodes();
		
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
		prop.name = "forrás feszültség:";
		prop.unit = "V";
		prop.value = String.valueOf(getVoltagePhasor().getRe());
		getProperties().put("voltage", prop);

		prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "áramerősség:";
		prop.unit = "A";
		prop.value = String.valueOf(0.0);
		getProperties().put("current", prop);

		prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "kapacitás:";
		prop.unit = "Farad";
		prop.value = String.valueOf(getCapacity());
		getProperties().put("capacity", prop);
	}
	
	@Override
	public void destroy() {		
		super.removeEndNodes();
		
		super.getParent().removeEdge(e);
	}

	//Update:----------------------------------------------------------------------------------------
	
	@Override
	public void update(double omega) {
		if (0.0 != omega) {
			e.setImpedance(new Complex(0.0, omega * capacity).inverse());
		}
		else if (omega > 0.0) {
			e.setImpedance(new Complex(0.0, Double.POSITIVE_INFINITY));
		}
		else {
			e.setImpedance(new Complex(0.0, Double.NEGATIVE_INFINITY));
		}
	}


	//Persistence:-----------------------------------------------------------------------------------
	
	@Override
	public void save(StringBuilder writer) {
		writer.append("class: ");				
		writer.append(this.getClass().getCanonicalName());
		writer.append("; capacity: ");
		writer.append(capacity);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		setCapacity(Double.valueOf(pairs[1].split(":")[1]));
		
		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		
		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
		updatePropertyView(true);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VoltageSource [");
		builder.append("capacity=");
		builder.append(capacity);
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
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.4f, 0.0f));
		lines.add(new Line(defaultSize * 0.4f, -defaultSize * 0.1f, defaultSize * 0.4f, +defaultSize * 0.1f));
		lines.add(new Line(defaultSize * 0.6f, -defaultSize * 0.1f, defaultSize * 0.6f, +defaultSize * 0.1f));

		lines.add(new Line(defaultSize * 0.6f, 0.0f, defaultSize, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx, getInput().getPos(),
				getOutput().getPos(),
				lines,
				defaultSize,
				getParent().isThisSelected(this),
				getCurrentVisualisationOffset(),
				true,
				(float)e.getInput().getPotential().getRe(),
				(float)e.getOutput().getPotential().getRe());
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
		e.setCurrent(new Complex(0, 0));
		e.setSourceVoltage(new Complex(0, 0));
		setCharge(0.0F);
		updatePropertyView(false);
	}


	@Override
	public void updatePropertyModel() {
		String str = getProperties().get("capacity").value;
		if (str != null && str.length() > 0) {
			try {
				double val = Double.parseDouble(str);
				setCapacity(val);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			getParent().setUpdateAll();
			//System.out.println("Updated value:" + getCapacity());
			getProperties().get("capacity").value = String.valueOf(getCapacity());
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {
		//setProperty("voltage", this::getVoltagePhasor);
		//setProperty("current", this::getCurrentPhasor);
		if (updateEditable) {
			setProperty("capacity", this::getCapacity);
		}		
	}


	@Override
	public Complex getImpedancePhasor() {
		return e.getImpedance();
	}

	
}
