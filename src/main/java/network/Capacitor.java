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

/**
 * Capacitor, with adjustable capacity.
 * @author Simon Zoltán
 *
 */
public class Capacitor extends Component {
	private Edge e;
	public float getCharge() {
		return charge;
	}


	public void setCharge(float charge) {
		this.charge = charge;
	}


	public float getCapacity() {
		return capacity;
	}


	private float charge = 0;
	private float capacity = 1;
	
	//Constructors:---------------------------------------------------------------------------------------
	
	public Capacitor() {
	}
	
	
	public Capacitor(float c) {
		capacity = c;
	}
		
	//Getters/Setters:------------------------------------------------------------------------------------
	
	public float getSourceVoltage() {
		return -(1/capacity) * charge;
	}

	public void setCapacity(float capacity) {
		this.capacity = capacity;
	}

	@Override
	public float getCurrent() {
		return e.getCurrent();
	}

	@Override
	public float getVoltage() {
		return getSourceVoltage();
	}

	//Build/Destroy:------------------------------------------------------------------------------------
	
	@Override
	public void build() {
		super.generateEndNodes();
		
		e = new Edge();
		super.getParent().addEdge(e);

		e.setCurrent(0);
		e.setResistance(0);
		e.setSourceVoltage(this.getSourceVoltage());
		
		getInput().setVertexBinding(e.getInput());
		getOutput().setVertexBinding(e.getOutput());
		
				
		//Properties:
		setProperties(new HashMap<String, ComponentProperty>());

		ComponentProperty prop = new ComponentProperty();
		prop.editable = false;
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
	public void update(Duration duration) {
		charge += e.getCurrent() * duration.toSeconds();
		e.setSourceVoltage(this.getSourceVoltage());
		System.out.println(e.getSourceVoltage());
		increaseCurrentVisualisationOffset();
		updatePropertyView();
		getParent().setUpdateAll();
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
		setCapacity(Float.valueOf(pairs[1].split(":")[1]));
		
		String coordIn[] = pairs[2].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getInput().setPos(new Coordinate(Integer.valueOf(coordIn[0]), Integer.valueOf(coordIn[1])));
		
		
		String coordOut[] = pairs[3].replaceAll("[\\[\\]]+", "").split(":")[1].split(",");
		getOutput().setPos(new Coordinate(Integer.valueOf(coordOut[0]), Integer.valueOf(coordOut[1])));
		
		updatePropertyView();
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
		lines.add(new Line(0.0f, 0.0f, defaultSize/3.0f, 0.0f));
		lines.add(new Line(defaultSize/3.0f, -defaultSize/4.0f, defaultSize/3.0f, +defaultSize/4.0f));
		lines.add(new Line(2.0f/3.0f * defaultSize, -defaultSize/4.0f, 2.0f/3.0f * defaultSize, +defaultSize/4.0f));

		lines.add(new Line(2.0f/3.0f * defaultSize, 0.0f, defaultSize, 0.0f));

		//call drawShape
		DrawingHelper.drawShape(ctx, getInput().getPos(), getOutput().getPos(), lines, defaultSize, getParent().isThisSelected(this), getCurrentVisualisationOffset());
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
		setCharge(0.0F);
		updatePropertyView();
	}


	@Override
	public void updatePropertyModel() {
		String str = getProperties().get("capacity").value;
		if (str != null && str.length() > 0) {
			try {
				float val = Float.parseFloat(str);
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
	public void updatePropertyView() {
		
		if (getProperties().containsKey("current")) {
			getProperties().get("current").value = String.valueOf(this.getCurrent());
			if (getProperties().get("current").valueN != null) {
				getProperties().get("current").valueN.setText(String.valueOf(this.getCurrent()));				
			}
		}

		if (getProperties().containsKey("voltage")) {
			getProperties().get("voltage").value = String.valueOf(this.getSourceVoltage());
			if (getProperties().get("voltage").valueN != null) {
				getProperties().get("voltage").valueN.setText(String.valueOf(this.getSourceVoltage()));				
			}
		}

		if (getProperties().containsKey("capacity")) {
			getProperties().get("capacity").value = String.valueOf(this.getCapacity());
			if (getProperties().get("capacity").valueN != null) {
				getProperties().get("capacity").valueN.setText(String.valueOf(this.getCapacity()));				
			}
		}
		
	}


	@Override
	public float getResistance() {
		return 0;
	}

	
}
