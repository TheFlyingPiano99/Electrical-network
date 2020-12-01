package main.java.network;

import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import main.java.gui.DrawingHelper;
import main.java.math.Coordinate;
import main.java.math.Line;
import main.java.math.MyMath;
import main.java.math.Vector;

/**
 * Resistance with adjustable value.
 * @author Simon Zoltán
 *
 */
public class Resistance extends Component {

	private Edge e;
	private float resistance = 1000;
	private final float DEFAULT_WIDTH  = 20.0f;
	private final float DEFAULT_LENGTH = 100.0f;
	
	
	//Constructors:---------------------------------------------------------------------------------------

	public Resistance() {
	}

	public Resistance(float r) {
		resistance = r;
	}
	
	//Getters/Setters:------------------------------------------------------------------------------------
	
	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getVoltage() {
		return e.getVoltage();
	}

	@Override
	public float getResistance() {
		return e.getResistance();
	}
	
	public void setResistance(float resistance) {
		this.resistance = resistance;
		e.setResistance(resistance);
	}
	
	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
		generateEndNodes();
		
		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(resistance);	//!
		e.setSourceVoltage(0);
		
		
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
		prop.name = "ellenállás:";
		prop.unit = "Ohm";
		prop.value = String.valueOf(getResistance());
		getProperties().put("resistance", prop);

	}

	@Override
	public void destroy() {
		removeEndNodes();
		super.getParent().removeEdge(e);		
	}

	//Update:-------------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		updatePropertyView();		
	}

	//Persistence:-----------------------------------------------------------------------------------

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
		setResistance(Float.valueOf(pairs[1].split(":")[1]));
		
		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		
		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
		updatePropertyView();
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
		List<Line> lines = new ArrayList<Line>();
		
		//Construction:
		lines.add(new Line(
				0.0f, 0.0f,
				DEFAULT_WIDTH, 0.0f));
		lines.add(new Line(
				DEFAULT_WIDTH, -DEFAULT_WIDTH/2.0f,
				DEFAULT_WIDTH, +DEFAULT_WIDTH/2.0f));
		lines.add(new Line(
				DEFAULT_WIDTH, -DEFAULT_WIDTH/2.0f,
				DEFAULT_LENGTH - DEFAULT_WIDTH, -DEFAULT_WIDTH/2.0f));
		lines.add(new Line(
				DEFAULT_WIDTH, +DEFAULT_WIDTH/2.0f,
				DEFAULT_LENGTH - DEFAULT_WIDTH, +DEFAULT_WIDTH/2.0f));
		lines.add(new Line(
				DEFAULT_LENGTH - DEFAULT_WIDTH, -DEFAULT_WIDTH/2.0f, 
				DEFAULT_LENGTH - DEFAULT_WIDTH, +DEFAULT_WIDTH/2.0f));
		lines.add(new Line(
				DEFAULT_LENGTH - DEFAULT_WIDTH, 0.0f, 
				DEFAULT_LENGTH, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx, getInput().getPos(), getOutput().getPos(), lines, DEFAULT_LENGTH, getParent().isThisSelected(this));
		
		//System.out.println("Resistance draw!");
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
		e.setCurrent(0.0F);
		updatePropertyView();

	}

	@Override
	public void updatePropertyModel() {
		String str = getProperties().get("resistance").value;
		if (str != null && str.length() > 0) {
			try {
				float val = Float.parseFloat(str);
				setResistance(val);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			getParent().setUpdateAll();
			getProperties().get("resistance").value = String.valueOf(getResistance());
		}
		
	}

	@Override
	public void updatePropertyView() {
		if (getProperties().containsKey("voltage")) {
			getProperties().get("voltage").value = String.valueOf(getVoltage());
			if (getProperties().get("voltage").valueN != null) {
				getProperties().get("voltage").valueN.setText(String.valueOf(getVoltage()));				
			}
		}

		if (getProperties().containsKey("current")) {
			getProperties().get("current").value = String.valueOf(getCurrent());
			if (getProperties().get("current").valueN != null) {
				getProperties().get("current").valueN.setText(String.valueOf(getCurrent()));				
			}
		}

		if (getProperties().containsKey("resistance")) {
			getProperties().get("resistance").value = String.valueOf(getResistance());
			if (getProperties().get("resistance").valueN != null) {
				getProperties().get("resistance").valueN.setText(String.valueOf(getResistance()));				
			}
		}
	}

}
