package gui;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import math.Coordinate;
import math.Line;
import math.MyMath;
import math.Vector;
import network.Network;

/**
 * Auxiliary methods for drawing components.
 * HUN: Segéd metódsok komponensek rajzolásához.
 * @author simon
 *
 */
public class DrawingHelper {

	private static final Color COLOR_NORMAL = Color.BLACK;
	private static final Color COLOR_SELECT = Color.GREEN;
	private static final StrokeLineCap LINE_CAP_NORMAL = StrokeLineCap.ROUND;
	private static final double LINE_WIDTH_NORMAL = 2.0;
	private static final double LINE_WIDTH_SELECT = 0.6;
	private static final double DASHES_NORMAL = 0.0;
	private static final double[] DASHES_SELECT = new double[] {10.0, 3.0};
	private static final double DASH_OFFSET_SELECT = 5.0;
	private static ArrayList<Double> scopeVoltageBuffer = new ArrayList<>();
	private static ArrayList<Double> scopeCurrentBuffer = new ArrayList<>();
	private static ArrayList<Double> scopeResistanceBuffer = new ArrayList<>();
	private static int maxScopeDataPoints = 256;
	private static boolean scopeInTimeDomain = true;

	/**
	 * Sets drawing attributes for normal drawing. (Uses the predefined, static variables.)
	 * HUN: Beállítja a rajzolás paramétereit normál rajzoláshoz. (Statikus adattagokat használ.)
	 * @param ctx
	 */
	public static void setNormalDrawingAttributes(GraphicsContext ctx) {
		ctx.setStroke(COLOR_NORMAL);
		ctx.setFill(COLOR_NORMAL);
		ctx.setLineCap(LINE_CAP_NORMAL);
		ctx.setLineWidth(LINE_WIDTH_NORMAL);
		ctx.setLineDashes(DASHES_NORMAL);
	}

	/**
	 * Sets drawing attributes for selection drawing. (Uses the predefined, static variables.)
	 * HUN: Beállítja a rajzolás paramétereit szelekció rajzoláshoz. (Statikus adattagokat használ.)
	 * @param ctx
	 */
	public static void setSelDrawingAttributes(GraphicsContext ctx) {
		ctx.setStroke(COLOR_SELECT);
		ctx.setFill(COLOR_SELECT);
		ctx.setLineWidth(LINE_WIDTH_SELECT);
		ctx.setLineCap(LINE_CAP_NORMAL);
		ctx.setLineDashes(DASHES_SELECT);
		ctx.setLineDashOffset(DASH_OFFSET_SELECT);		
	}

	/**
	 * Draws end nodes of components.
	 * HUN: A komponens végpontjait jeleníti meg.
	 * @param ctx {@link GraphicsContext}
	 * @param inputPos {@link Coordinate}
	 * @param outputPos {@link Coordinate}
	 */
	private static void drawEndNodes(GraphicsContext ctx, Coordinate inputPos, Coordinate outputPos) {
		ctx.fillOval(inputPos.x-3, inputPos.y-3, 6, 6);
		ctx.fillOval(outputPos.x-3, outputPos.y-3, 6, 6);
	}
	
	/**
	 * Updates the image visible on canvas.
	 * HUN: Frissíti a "vásznon" megjelelített képet.
	 * @param canvas to draw on
	 * @param network {@link Network} that provides objects to visualise.
	 */
	protected static void updateCanvasContent(Canvas canvas, Network network, double totalTimeSec) {
		GraphicsContext ctx;
		if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
			ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			if (network.isSnapToGrid()) {
				drawGrid(canvas, network.getGridSize());				
			}
			network.draw(ctx, totalTimeSec);
		}
	}

	protected static void resetScope(Canvas canvas) {
		GraphicsContext ctx;
		if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
			double W = canvas.getWidth();
			double H = canvas.getHeight();
			ctx.setFill(Color.GREY);
			ctx.fillRect(0, 0, W, H);
		}
		scopeCurrentBuffer.clear();
		scopeVoltageBuffer.clear();
		scopeResistanceBuffer.clear();
	}

	private static void drawFrequencyDomainVoltage(GraphicsContext ctx, Vector voltage, double W, double H)
	{
		ctx.setStroke(Color.GREEN);
		ctx.strokeText("U(omega) [V]", 10,20);

		double valOffset = H * 0.8;
		double valScale = H / 5.0;

		for (int k = 0; k < voltage.dimension - 1; k++) {
			ctx.strokeLine(
					W * (k + 1) / (double)(voltage.dimension + 2),
					valOffset - valScale * voltage.at(k).getAbs(),
					W * (k + 2) / (double)(voltage.dimension + 2),
					valOffset - valScale * voltage.at(k + 1).getAbs()
			);
		}
	}

	private static void drawFrequencyDomainCurrent(GraphicsContext ctx, Vector current, double W, double H)
	{
		ctx.setStroke(Color.YELLOW);
		ctx.strokeText("I(omega) [A]", 10,40);

		double valOffset = H * 0.8;
		double valScale = H / 5.0;

		for (int k = 0; k < current.dimension - 1; k++) {
			ctx.strokeLine(
					W * (k + 1) / (double)(current.dimension + 2),
					valOffset - valScale * current.at(k).getAbs(),
					W * (k + 2) / (double)(current.dimension + 2),
					valOffset - valScale * current.at(k + 1).getAbs()
			);
		}
	}

	private static void drawTimeDomainVoltage(GraphicsContext ctx, double U, double W, double H) {
		ctx.setStroke(Color.GREEN);
		ctx.strokeText("U = " + String.format("%,.5f", U) + " V", 10,20);
		double prevVal = 0;
		double valOffset = H / 2.0;
		double valScale = H / 2.0 / 5.0;
		boolean firstElement = true;
		int t = 0;
		double stepSize = W / maxScopeDataPoints;
		for (Double u : scopeVoltageBuffer) {
			if (firstElement) {
				firstElement = false;
			}
			else {
				ctx.strokeLine(
						(t - 1) * stepSize,
						Math.min(Math.max(0.0, valOffset - valScale * prevVal), H),
						t * stepSize,
						Math.min(Math.max(0.0, valOffset - valScale * u), H)
				);
			}
			prevVal = u;
			t++;
		}
	}

	private static void drawTimeDomainCurrent(GraphicsContext ctx, double I, double W, double H) {
		ctx.setStroke(Color.YELLOW);
		ctx.strokeText("I = " + String.format("%,.5f", I) + " A", 10,40);

		double prevVal = 0;
		double valOffset = H / 2.0;
		double valScale = H / 2.0 / 5.0;
		boolean firstElement = true;
		int t = 0;
		double stepSize = W / maxScopeDataPoints;
		for (Double i : scopeCurrentBuffer) {
			if (firstElement) {
				firstElement = false;
			}
			else {
				ctx.strokeLine(
						(t - 1) * stepSize,
						Math.min(Math.max(0.0, valOffset - valScale * prevVal), H),
						t * stepSize,
						Math.min(Math.max(0.0, valOffset - valScale * i), H)
				);
			}
			prevVal = i;
			t++;
		}
	}

	private static void drawResistance(GraphicsContext ctx, double R, double W, double H) {
		ctx.setStroke(Color.PURPLE);
		ctx.strokeText("R = " + String.format("%,.5f", R) + " Ohm", 10,60);

		double prevVal = 0;
		double valOffset = H / 2.0;
		double valScale = H / 2.0 / 5.0;
		boolean firstElement = true;
		int t = 0;
		double stepSize = W / maxScopeDataPoints;
		for (Double r : scopeResistanceBuffer) {
			if (firstElement) {
				firstElement = false;
			}
			else {
				ctx.strokeLine(
						(t - 1) * stepSize,
						Math.min(Math.max(0.0, valOffset - valScale * prevVal), H),
						t * stepSize,
						Math.min(Math.max(0.0, valOffset - valScale * r), H)
				);
			}
			prevVal = r;
			t++;
		}
	}

	protected static void updateScopeImage(Canvas canvas, Network network, double totalTimeSec, boolean running) {
		GraphicsContext ctx;
		if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
			double W = canvas.getWidth();
			double H = canvas.getHeight();
			ctx.setFill(Color.GREY);
			ctx.fillRect(0, 0, W, H);

			network.Component selected = network.getSelected();
			if (selected != null) {
				double I = selected.getTimeDomainCurrent();
				double U = selected.getTimeDomainVoltageDrop();
				double R = selected.getTimeDomainResistance();
				if (running) {
					scopeCurrentBuffer.add(I);
					scopeVoltageBuffer.add(U);
					scopeResistanceBuffer.add(R);
					if (scopeCurrentBuffer.size() > maxScopeDataPoints) {	// Pop first element
						scopeCurrentBuffer.remove(0);
					}
					if (scopeVoltageBuffer.size() > maxScopeDataPoints) {	// Pop first element
						scopeVoltageBuffer.remove(0);
					}
					if (scopeResistanceBuffer.size() > maxScopeDataPoints) {	// Pop first element
						scopeResistanceBuffer.remove(0);
					}
				}

				if (scopeInTimeDomain) {
					setNormalDrawingAttributes(ctx);
					ctx.setLineWidth(0.8);
					ctx.strokeText("t = " + String.format("%,.2f", totalTimeSec) + " s", 10,H - 20);

					drawTimeDomainVoltage(ctx, U, W, H);
					drawTimeDomainCurrent(ctx, I, W, H);
					drawResistance(ctx, R, W, H);
				}
				else {	// scope mode == frequency domain
					Vector voltage = selected.getFrequencyDomainVoltageDrop();
					Vector current = selected.getFrequencyDomainCurrent();

					setNormalDrawingAttributes(ctx);
					ctx.setLineWidth(0.8);
					ctx.strokeText("t = " + String.format("%,.2f", totalTimeSec) + " s", 10,H - 20);
					drawFrequencyDomainVoltage(ctx, voltage, W, H);
					drawFrequencyDomainCurrent(ctx, current, W, H);
				}
			}
			else {
				scopeCurrentBuffer.clear();
				scopeVoltageBuffer.clear();
				scopeResistanceBuffer.clear();
			}
		}
	}

	public static void toggleScopeMode()
	{
		scopeInTimeDomain = !scopeInTimeDomain;
	}
	
	private static Color getInterpolatedColor(Color color1, Color color2, float t) {
		//t = MyMath.max(MyMath.min(t, 1.0f), 0.0f);	//Trust domain of t.
		double r= color1.getRed() * t + color2.getRed() * (1 - t);
		double g= color1.getGreen() * t + color2.getGreen() * (1 - t);
		double b= color1.getBlue() * t + color2.getBlue() * (1 - t);
		return new Color(r, g, b, 1.0);
	}
	
	/**
	 * Draw shape of a component. Called by components.
	 * HUN: Komponens alakzatának kirajzolása. A komponensek hívják.
	 * @param ctx	{@link GraphicsContext}	
	 * @param inputPos	Position of the input node of the component.
	 * @param outputPos	Position of the output node of the component.
	 * @param lines		List of lines describing the shape of the component.
	 * @param default_length	The default / normal length of the component. Used to calculate scaling of the drawing.
	 * @param selected		Whether the component is selected or not.
	 * @param currentVisualisationOffset TODO
	 * @param visualiseCurrent TODO
	 */
	public static void drawShape(GraphicsContext ctx,
			Coordinate inputPos,
			Coordinate outputPos,
			List<Line> lines,
			float default_length,
			boolean selected,
			double currentVisualisationOffset,
			boolean visualiseCurrent,
			float inputNormalizedPotential,
			float outputNormalizedPotential) {

		Vector vInput  = MyMath.coordToVector(inputPos); 
		Vector vOutput = MyMath.coordToVector(outputPos);
		
		
		Vector orientation = MyMath.subtract(vOutput, vInput);
		float scale = (float)MyMath.magnitude(orientation) / default_length;
		float angle = (float)Math.atan2(orientation.at(1).getRe(), orientation.at(0).getRe());
		
		//------------------------------------------------------
		if (lines != null && !lines.isEmpty()) {

		
			Float minX = null;
			Float maxX = null;
			Float minY = null;
			Float maxY = null;

			setNormalDrawingAttributes(ctx);

			for (Line line : lines) {
				Line.transform(line, scale, angle, vInput);

				float aX = (float)line.a.at(0).getRe();
				float aY = (float)line.a.at(1).getRe();
				float bX = (float)line.b.at(0).getRe();
				float bY = (float)line.b.at(1).getRe();
				
				minX = getNewMin(minX, aX, bX);
				minY = getNewMin(minY, aY, bY);
				maxX = getNewMax(maxX, aX, bX);
				maxY = getNewMax(maxY, aY, bY);

				ctx.strokeLine(aX, aY, bX, bY);
			}
			
			if (visualiseCurrent) {
		        Stop[] stops = new Stop[] {
		        			new Stop(0, getInterpolatedColor(Color.RED, Color.BLUE, inputNormalizedPotential)), new Stop(1,
		        						getInterpolatedColor(Color.RED, Color.BLUE, outputNormalizedPotential))};
		        LinearGradient lgt = new LinearGradient(
		        		(inputPos.x <= outputPos.x)? 0 : 1,
        				(inputPos.y <= outputPos.y)? 0 : 1,
		        		(inputPos.x < outputPos.x)? 1 : 0,
        				(inputPos.y < outputPos.y)? 1 : 0,
        				true,
        				CycleMethod.NO_CYCLE,
        				stops);
				ctx.setStroke(lgt);
				ctx.setLineWidth(5);
				ctx.setLineDashes(default_length*0.2, default_length*0.8);
				ctx.setLineDashOffset(-currentVisualisationOffset);
				ctx.strokeLine(vInput.at(0).getRe(), vInput.at(1).getRe(), vOutput.at(0).getRe(), vOutput.at(1).getRe());
				ctx.setLineDashOffset(0);
			}

			drawEndNodes(ctx, inputPos, outputPos);

			if (selected) {
				setSelDrawingAttributes(ctx);
				ctx.strokeRect(
						minX - 10.0f, minY - 10.0f,
						maxX - minX + 20.0f, maxY - minY + 20.0f);
				
				
			}
		}

	}
	
	/**
	 * Returns the greatest value among args.
	 * HUN: Visszaadja a legnagyobb értéket a paraméterek közül.
	 * @param prevMax	Previous max value.
	 * @param a			New	max candidate no1
	 * @param b			New max candidate no2
	 * @return
	 */
	private static Float getNewMax(Float prevMax, float a, float b) {
		float temp = Math.max(a, b);
		return (prevMax == null || prevMax < temp) ? temp : prevMax;
	}

	/**
	 * Returns the smallest value among args.
	 * HUN: Visszaadja a legkisebb értéket a paraméterek közül.
	 * @param prevMin	Previous min value.
	 * @param a			New min candidate no1
	 * @param b 		New min candidate no2
	 * @return
	 */
	private static Float getNewMin(Float prevMin, float a, float b) {
		float temp = Math.min(a, b);
		return (prevMin == null || prevMin > temp) ? temp : prevMin;
	}
	
	private static void drawGrid(Canvas canvas, int gridSize) {
		GraphicsContext ctx = canvas.getGraphicsContext2D();
		ctx.setStroke(Color.GREY);
		ctx.setLineWidth(0.3);
		ctx.setLineDashes(null);
		
		for (int x = 0; x < canvas.getWidth(); x += gridSize) {	// Vertical lines
			ctx.strokeLine(x, 0, x, canvas.getHeight());
		}
		for (int y = 0; y < canvas.getHeight(); y += gridSize) { // Horizontal lines
			ctx.strokeLine(0, y, canvas.getWidth(), y);
		}	
	}
	
}
