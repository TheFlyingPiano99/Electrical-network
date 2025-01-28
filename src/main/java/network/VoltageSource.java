package network;

import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;
import gui.DrawingHelper;
import math.Coordinate;
import math.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Ideal voltage source, with adjustable value and zero resistance.
 * @author Simon Zoltán
 *
 */
public class VoltageSource extends network.Component {
	private network.Edge e;
	private double sourceVoltage = 1.0;
	private double fadeIn = 0.0;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public VoltageSource() {
	}
	
	
	public VoltageSource(double u) {
		sourceVoltage = u;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------
	
	public double getSourceVoltage() {
		return fadeIn * sourceVoltage;
	}

	public double getSourceVoltageForView() {
		return sourceVoltage;
	}

	public void setSourceVoltage(double sourceVoltage) {
		this.sourceVoltage = sourceVoltage;
		if (e != null) {
			e.setSourceVoltage(fadeIn * sourceVoltage);
		}
	}

	@Override
	public double getCurrent() {
		return e.getCurrent();
	}

	@Override
	public double getVoltage() {
		return getSourceVoltage();
	}

	@Override
	public double getResistance() {
		return getSourceVoltage() / e.getCurrent();
	}

	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
		super.generateEndNodes();
		
		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
		e.setSourceVoltage(getSourceVoltage());	//!

		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
		

				
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = true;
		prop.name = "forrás feszültség:";
		prop.unit = "V";
		prop.value = String.valueOf(getSourceVoltage());
		getProperties().put("voltage", prop);

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

	//Update:----------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		increaseCurrentVisualisationOffset();
		if (fadeIn < 1.0) {
			fadeIn += duration.toSeconds() * 2.0;
			if (fadeIn > 1.0) {
				fadeIn = 1.0;
			}
			e.setSourceVoltage(getSourceVoltage());	//!
			updatePropertyView(true);
			getParent().setUpdateAll();
		}
		else {
			updatePropertyView(false);
		}
	}


	//Persistence:-----------------------------------------------------------------------------------
	
	@Override
	public void save(StringBuilder writer) {
		writer.append("class: ");				
		writer.append(this.getClass().getCanonicalName());
		writer.append("; voltage: ");
		writer.append(sourceVoltage);

		writer.append("; inputPos: ");
		writer.append(String.format("[%d, %d]", getInput().getPos().x, getInput().getPos().y));

		writer.append("; outputPos: ");
		writer.append(String.format("[%d, %d]", getOutput().getPos().x, getOutput().getPos().y));

		writer.append("\n");
	}

	@Override
	public void load(String[] pairs) {
		setSourceVoltage(Double.valueOf(pairs[1].split(":")[1]));
		
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
		builder.append("sourceVoltage=");
		builder.append(sourceVoltage);
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
				(float)e.getInput().getPotential(),
				(float)e.getOutput().getPotential());
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
		fadeIn = 0.0;
		e.setCurrent(0.0F);
		e.setSourceVoltage(0);
		updatePropertyView(true);
	}


	@Override
	public void updatePropertyModel() {
		String str = getProperties().get("voltage").value;
		if (str != null && str.length() > 0) {
			try {
				double val = Double.parseDouble(str);
				setSourceVoltage(val);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			getParent().setUpdateAll();
			//System.out.println("Updated value:" + getSourceVoltage());
			getProperties().get("voltage").value = String.valueOf(getSourceVoltage());
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {
		
		if (updateEditable) {
			setProperty("voltage", this::getSourceVoltageForView);
		}
		setProperty("current", this::getCurrent);
		setProperty("resistance", this::getResistance);
	}



	
}
