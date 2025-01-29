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

	public void setCapacity(double capacity) {
		this.capacity = capacity;
		Vector omega = getParent().getAngularFrequencies();
		for (int i = 0; i < e.getImpedance().dimension; i++) {
			e.getImpedance().setAt(
					i,
					new Complex(0, 1.0 / (2 * Math.PI * omega.at(i).getRe() * capacity))
			);
		}
	}

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
		super.getParent().addEdge(e);

		math.Vector omega = getParent().getAngularFrequencies();
		math.Vector current = new math.Vector(omega.dimension);
		current.fill(new Complex(0, 0));
		e.setCurrent(current);
		math.Vector impedance = new math.Vector(omega.dimension);
		for (int i = 0; i < impedance.dimension; i++) {
			impedance.setAt(
				i,
				new Complex(0, 1.0 / (2 * Math.PI * omega.at(i).getRe() * capacity))
			);
		}
		e.setImpedance(impedance);
		math.Vector sourceVoltage = new Vector(omega.dimension);
		sourceVoltage.fill(new Complex(0, 0));
		e.setSourceVoltage(sourceVoltage);
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
		
				
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "forrás feszültség:";
		prop.unit = "V";
		prop.value = String.valueOf(e.getTimeDomainVoltageDrop());
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
		updatePropertyView(false);
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
		String str = getProperties().get("capacity").value;
		if (str != null && str.length() > 0) {
			try {
				double val = Double.parseDouble(str);
				setCapacity(val);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.println("Updated value:" + getCapacity());
			getProperties().get("capacity").value = String.valueOf(getCapacity());
			getParent().simulate();
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {
		setProperty("voltage", this::getTimeDomainVoltageDrop);
		setProperty("current", this::getTimeDomainCurrent);
		if (updateEditable) {
			setProperty("capacity", this::getCapacity);
		}
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
