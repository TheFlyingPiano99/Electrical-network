package main.java.network;

import javafx.util.Duration;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.collections.SetChangeListener;
import javafx.scene.canvas.GraphicsContext;
import main.java.gui.DrawingHelper;
import main.java.math.Coordinate;
import main.java.math.Line;
import main.java.math.Vector;

/**
 * Ideal inductor, with adjustable value and zero resistance.
 * @author Simon Zoltán
 *
 */
public class Inductor extends Component {
	private Edge e;
	private float inductance = 1;
	private ArrayList<Float> prevCurrent = new ArrayList<Float>();
	private float derivativeOfCurrent = 0;
	private final float DEFAULT_SIZE = 60.0f;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public Inductor() {
	}
	
	
	public Inductor(float l) {
		inductance = l;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------
	
	public float getSourceVoltage() {
		return -inductance * derivativeOfCurrent;
	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getVoltage() {
		return getSourceVoltage();
	}

	@Override
	public float getResistance() {
		return 0;
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
		
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());		

				
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = false;
		prop.name = "forrás feszültség:";
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

	//Update:----------------------------------------------------------------------------------------
	
	@Override
	public void update(Duration duration) {
		float h = (float)duration.toSeconds();
		float avarage = 0;
		for (Float float1 : prevCurrent) {
			avarage += float1;
		}
		if (prevCurrent.size() > 0) {
			avarage /= prevCurrent.size();			
		}
		if (5 < prevCurrent.size()) {
			prevCurrent.remove(0);
		}
		
		derivativeOfCurrent = (e.getCurrent() - avarage) / h;

		e.setSourceVoltage(this.getSourceVoltage());
		
		increaseCurrentVisualisationOffset();
		updatePropertyView(false);
		getParent().setUpdateAll();	
		prevCurrent.add(e.getCurrent());
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
		setInductance(Float.valueOf(pairs[1].split(":")[1]));
		
		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		
		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
		updatePropertyView(true);
	}


	public float getInductance() {
		return inductance;
	}


	public void setInductance(float inductance) {
		this.inductance = inductance;
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
		DrawingHelper.drawShape(ctx, getInput().getPos(), getOutput().getPos(), lines, DEFAULT_SIZE, getParent().isThisSelected(this), getCurrentVisualisationOffset(), true);
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
		e.setSourceVoltage(0.0F);
		prevCurrent.clear();
		updatePropertyView(false);
	}


	@Override
	public void updatePropertyModel() {
		String str = getProperties().get("inductance").value;
		if (str != null && str.length() > 0) {
			try {
				float val = Float.parseFloat(str);
				setInductance(val);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			getParent().setUpdateAll();
			//System.out.println("Updated value:" + getInductance());
			getProperties().get("inductance").value = String.valueOf(getInductance());
		}
	}


	@Override
	public void updatePropertyView(boolean updateEditable) {
		setProperty("voltage", this::getVoltage);
		setProperty("current", this::getCurrent);
		if (updateEditable) {
			setProperty("inductance", this::getInductance);
		}		
	}

	
}
