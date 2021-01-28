package main.java.network;

import javafx.util.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.collections.SetChangeListener;
import javafx.scene.canvas.GraphicsContext;
import main.java.gui.DrawingHelper;
import main.java.math.Coordinate;
import main.java.math.Line;

/**
 * Current input, with adjustable value.
 * @author Simon Zoltán
 *
 */



public class CurrentSource extends Component {
	private Edge e;
	private float inputCurrent = 1.0f;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public CurrentSource() {
	}
	
	
	public CurrentSource(float i) {
		inputCurrent = i;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------
	
	public float getInputCurrent() {
		return inputCurrent;
	}

	public void setInputCurrent(float inputCurrent) {
		this.inputCurrent = inputCurrent;
		if (e != null) {
			e.getInput().setInputCurrent(inputCurrent);
		}
	}


	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
		super.generateEndNodes();
		
		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
		e.setSourceVoltage(0);
		e.getInput().setInputCurrent(getInputCurrent()); //!

		
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

	//Update:----------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		increaseCurrentVisualisationOffset();
		updatePropertyView(false);
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
		setInputCurrent(Float.valueOf(pairs[1].split(":")[1]));
		
		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		
		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
		updatePropertyView(true);
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
		lines.add(new Line(0.0f, 0.0f, defaultSize * 0.4f, 0.0f));
		lines.add(new Line(defaultSize * 0.4f, -defaultSize * 0.08f, defaultSize * 0.4f, +defaultSize * 0.08f));
		lines.add(new Line(defaultSize * 0.6f, -defaultSize* 0.2f, defaultSize * 0.6f, +defaultSize * 0.2f));
		lines.add(new Line(defaultSize * 0.6f, 0.0f, defaultSize, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx, getInput().getPos(), getOutput().getPos(), lines, defaultSize, getParent().isThisSelected(this), getCurrentVisualisationOffset(), true);
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
		updatePropertyView(false);
	}


	@Override
	public void updatePropertyModel() {
		String str = getProperties().get("current").value;
		if (str != null && str.length() > 0) {
			try {
				float val = Float.parseFloat(str);
				setInputCurrent(val);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			getParent().setUpdateAll();
			getProperties().get("current").value = String.valueOf(getInputCurrent());
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {		
		if (updateEditable) {
			setProperty("current", this::getInputCurrent);
		}
	}


	@Override
	public float getCurrent() {
		return e.getCurrent();
	}


	@Override
	public float getVoltage() {
		return 0;
	}


	@Override
	public float getResistance() {
		return 0;
	}



	
}
