package main.java.gui;

import javafx.scene.canvas.Canvas;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import main.java.math.Coordinate;
import main.java.math.Line;
import main.java.math.MyMath;
import main.java.math.Vector;
import main.java.network.Component;
import main.java.network.ComponentNode;
import main.java.network.Resistance;
import main.java.network.VoltageSource;
import main.java.network.Wire;

public class DrawingHelper {
	
	private static final javafx.scene.paint.Color COLOR_NORMAL = javafx.scene.paint.Color.BLACK;
	private static final javafx.scene.paint.Color COLOR_SELECT = javafx.scene.paint.Color.GREEN;
	private static final StrokeLineCap LINE_CAP_NORMAL = StrokeLineCap.ROUND;
	private static final double LINE_WIDTH_NORMAL = 2.0;
	private static final double LINE_WIDTH_SELECT = 0.6;
	private static final double DASHES_NORMAL = 0.0;
	private static final double[] DASHES_SELECT = new double[] {10.0, 3.0};
	private static final double DASH_OFFSET_SELECT = 5.0;
	
	static List<Component> components = new ArrayList<Component>();
	
	public static void setNormalDrawingAttributes(GraphicsContext ctx) {
		ctx.setStroke(COLOR_NORMAL);
		ctx.setFill(COLOR_NORMAL);
		ctx.setLineCap(LINE_CAP_NORMAL);
		ctx.setLineWidth(LINE_WIDTH_NORMAL);
		ctx.setLineDashes(DASHES_NORMAL);
	}

	public static void setSelDrawingAttributes(GraphicsContext ctx) {
		ctx.setStroke(COLOR_SELECT);
		ctx.setFill(COLOR_SELECT);
		ctx.setLineWidth(LINE_WIDTH_SELECT);
		ctx.setLineCap(LINE_CAP_NORMAL);
		ctx.setLineDashes(DASHES_SELECT);
		ctx.setLineDashOffset(DASH_OFFSET_SELECT);		
	}

	private static void drawConnectors(GraphicsContext ctx, List<Line> lines) {
		if (lines != null && !lines.isEmpty()) {
			setNormalDrawingAttributes(ctx);
			ctx.fillOval(lines.get(0).a.at(0)-3, lines.get(0).a.at(1)-3, 6, 6);

			int last = 0;
			if ((last = lines.size()-1) > 0) {
				ctx.fillOval(lines.get(last).b.at(0)-3, lines.get(last).b.at(1)-3, 6, 6);
			}
		}
	}
	
	public static void updateCanvasContent(Canvas canvas) {
		GraphicsContext ctx;
		if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
			ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			for (Component component : components) {
				component.draw(ctx);
			}
		}
	}
	
	public static void drawWire(GraphicsContext ctx, List<Line> lines) {

		if (lines != null && !lines.isEmpty()) {

			setNormalDrawingAttributes(ctx);

			for (Line line : lines) {
				ctx.strokeLine(
						line.a.at(0), line.a.at(1),
						line.b.at(0), line.b.at(1));
			}

			drawConnectors(ctx, lines);
		}
	}
	
	public static void drawShape(GraphicsContext ctx, Coordinate inputPos, Coordinate outputPos, List<Line> lines, float default_size, boolean grabbed) {

		Vector vInput  = MyMath.coordToVector(inputPos); 
		Vector vOutput = MyMath.coordToVector(outputPos);
		
		Vector orientation = MyMath.subtract(vOutput, vInput);
		float scale = MyMath.magnitude(orientation) / default_size;
		float angle = (float)Math.atan2(orientation.at(1), orientation.at(0));

		//------------------------------------------------------
		if (lines != null && !lines.isEmpty()) {

			Float minX = null;
			Float maxX = null;
			Float minY = null;
			Float maxY = null;

			setNormalDrawingAttributes(ctx);

			for (Line line : lines) {
				Line.transform(line, scale, angle, vInput);
				float aX = line.a.at(0);
				float aY = line.a.at(1);
				float bX = line.b.at(0);
				float bY = line.b.at(1);
				
				minX = getNewMin(minX, aX, bX);
				minY = getNewMin(minY, aY, bY);
				maxX = getNewMax(maxX, aX, bX);
				maxY = getNewMax(maxY, aY, bY);

				ctx.strokeLine(aX, aY, bX, bY);
			}

			drawConnectors(ctx, lines);

			if (grabbed) {
				setSelDrawingAttributes(ctx);
				ctx.strokeRect(
						minX - 10.0f, minY - 10.0f,
						maxX - minX + 20.0f, maxY - minY + 20.0f);
			}
		}

	}
	
	private static Float getNewMax(Float maxX, float aX, float bX) {
		float temp = Math.max(aX, bX);
		return (maxX == null || maxX < temp) ? temp : maxX;
	}

	private static Float getNewMin(Float minX, float aX, float bX) {
		float temp = Math.min(aX, bX);
		return (minX == null || minX > temp) ? temp : minX;
	}

	public Component grabComponent(Canvas canvas, int x, int y) {
		Component result = null;
		for (Component c : components) {
			
			int iX = c.getInput().getPos().x;
			int iY = c.getInput().getPos().y;
			int oX = c.getOutput().getPos().x;
			int oY = c.getOutput().getPos().y;

			int minX = Math.min(iX, oX) - 10;
			int maxX = Math.max(iX, oX) + 10;
			int minY = Math.min(iY, oY) - 10;
			int maxY = Math.max(iY, oY) + 10;
			
			c.setGrabbed(false);
			if (minX <= x && maxX >= x && minY <= y && maxY >= y) {
				c.setGrabbed(!c.isGrabbed());
				result = c;
			}
		}

		updateCanvasContent(canvas);

		return result;
	}
	
	public void updateComponentPos(GraphicsContext ctx, int x, int y, Component c) {
		if (c != null) {
			int oldX = c.getInput().getPos().x;
			int oldY = c.getInput().getPos().y;
			int dX = x - oldX;
			int dY = y - oldY;
			c.getInput().getPos().x += dX;  
			c.getInput().getPos().y += dY;  
			c.getOutput().getPos().x += dX;  
			c.getOutput().getPos().y += dY;  
		}
	}
	
	public void test1(GraphicsContext ctx) {

		Resistance r = new Resistance(50);
		ComponentNode input = new ComponentNode();
		input.getPos().x = 100;
		input.getPos().y = 100;
		r.setInput(input);
		
		ComponentNode output = new ComponentNode();
		output.getPos().x = 200;
		output.getPos().y = 300;
		r.setOutput(output);
		
		r.draw(ctx);
		components.add(r);

		//---------------------------------------
		
		r = new Resistance(50);
		input = new ComponentNode();
		input.getPos().x = 200;
		input.getPos().y = 200;
		r.setInput(input);
		
		output = new ComponentNode();
		output.getPos().x = 280;
		output.getPos().y = 200;
		r.setOutput(output);
		
		r.setGrabbed(true);
		r.draw(ctx);
		components.add(r);
	
	}

	public void test2(GraphicsContext ctx) {
		VoltageSource v = new VoltageSource(9.0f);
		ComponentNode input = new ComponentNode();
		input.getPos().x = 200;
		input.getPos().y = 100;
		v.setInput(input);
		
		ComponentNode output = new ComponentNode();
		output.getPos().x = 250;
		output.getPos().y = 150;
		v.setOutput(output);

		v.draw(ctx);
		components.add(v);

		v = new VoltageSource(24.0f);
		input = new ComponentNode();
		input.getPos().x = 280;
		input.getPos().y = 100;
		v.setInput(input);
		
		output = new ComponentNode();
		output.getPos().x = 350;
		output.getPos().y = 100;
		v.setOutput(output);
		
		v.setGrabbed(true);
		v.draw(ctx);
		components.add(v);

	}

	public void test3(GraphicsContext ctx) {
		Wire w = new Wire();
		ComponentNode input = new ComponentNode();
		input.getPos().x = 220;
		input.getPos().y = 250;
		w.setInput(input);
		
		ComponentNode output = new ComponentNode();
		output.getPos().x = 300;
		output.getPos().y = 300;
		w.setOutput(output);

		w.draw(ctx);
		components.add(w);
	}
	
}
