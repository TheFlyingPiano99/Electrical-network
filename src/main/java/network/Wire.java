package main.java.network;

import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import main.java.gui.DrawingHelper;
import main.java.math.Coordinate;
import main.java.math.Line;

/**
 *	Ideal wire, with zero resistance.
 *	HUN: Ideális vezeték, nulla ellenállással.
 * @author Simon Zoltán
 * 
 */
public class Wire extends Component {
	private Edge e;

	//Getters/Setters:------------------------------------------------------------------------------------

	@Override
	public float getVoltage() {
		return e.getVoltage();
	}

	@Override
	public float getResistance() {
		return e.getResistance();
	}
	
	@Override
	public float getCurrent() {
		return e.getCurrent();
	}
	
	//Build/Destroy:------------------------------------------------------------------------------------

	@Override
	public void build() {
		generateEndNodes();

		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
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
		prop.editable = false;
		prop.name = "ellenállás:";
		prop.unit = "Ohm";
		prop.value = String.valueOf(0.0);
		getProperties().put("resistance", prop);
	}

	@Override
	public void destroy() {
		removeEndNodes();
		super.getParent().removeEdge(e);
	}
	
	//Update:---------------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		updatePropertyView();
	}

	//Persistence:-----------------------------------------------------------------------------------

	@Override
	public void save(StringBuilder writer) {
		writer.append("class: ");				
		writer.append(this.getClass().getCanonicalName());		
		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		String coordIn[] = pairs[1].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		String coordOut[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
		updatePropertyView();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Wire [");
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
		Coordinate inputPos  = getInput().getPos(); 
		Coordinate outputPos = getOutput().getPos();

		//boolean eqX = (outputPos.x == inputPos.x);
		//boolean eqY = (outputPos.y == inputPos.y);
		
		lines.add(new Line(0, 0, 100, 0));
		
		DrawingHelper.drawShape(ctx, inputPos, outputPos, lines, 100, getParent().isThisSelected(this));

/*
		//Construction:
		if (eqX || eqY) {
			lines.add(new Line(inputPos.x, inputPos.y, outputPos.x, outputPos.y));
		} else {

			// 2 segments: [input -> breaking point] and [breaking point -> output]
			int brX = outputPos.x;
			int brY = outputPos.y;
			if (outputPos.x != inputPos.x) {
				brX = inputPos.x;
			} else if (outputPos.y != inputPos.y) {
				brY = inputPos.y;
			}
			lines.add(new Line(inputPos.x, inputPos.y, brX, brY));
			lines.add(new Line(brX, brY, outputPos.x, outputPos.y));

		}

		//call drawWire
		DrawingHelper.drawWire(ctx, lines);
*/
	}

	@Override
	public
	void disconnectGraphRepresentation() {
		getParent().disconnectEndOfEdge(e, e.getInput());
		getInput().setVertexBinding(e.getInput());
		
		getParent().disconnectEndOfEdge(e, e.getOutput());
		getOutput().setVertexBinding(e.getOutput());
	}

	@Override
	public
	void reset() {		
		e.setCurrent(0.0F);
		updatePropertyView();

	}

	@Override
	public
	void updatePropertyModel() {
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