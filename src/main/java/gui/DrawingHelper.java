package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import network.Component;
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
	private static int maxScopeDataPoints = 256;
	private static boolean scopeInTimeDomain = true;
	private static double scopeStartTimeSec = 0;
	private static double scopeSampleTimeStep = 1.0 / maxScopeDataPoints;
	private static double scopeTimeValueScale = 0.5;
	private static final Object accessMutexObj = new Object();

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
	protected static void updateCanvasContent(Canvas canvas, Network network, double totalTimeSec, double deltaTimeSec) {
		GraphicsContext ctx;
		if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
			ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			if (network.isSnapToGrid()) {
				drawGrid(canvas, network.getGridSize());				
			}
			network.draw(ctx, totalTimeSec, deltaTimeSec);
		}
	}

	protected static void resetScope(Canvas canvas) {
		synchronized (accessMutexObj)
		{
			GraphicsContext ctx;
			if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
				double W = canvas.getWidth();
				double H = canvas.getHeight();
				ctx.setFill(Color.GREY);
				ctx.fillRect(0, 0, W, H);
			}
			scopeCurrentBuffer.clear();
			scopeVoltageBuffer.clear();
			scopeStartTimeSec = 0;
		}
	}

	private static void drawFrequencyDomainVoltage(GraphicsContext ctx, ArrayList<Double> angularFrequencies, Vector voltage, double W, double H)
	{
		ctx.setStroke(Color.GREEN);
		ctx.setLineWidth(0.8);
		ctx.strokeText("U(omega) [V]", 10,20);
		ctx.setLineWidth(2.0);

		double valOffset = H * 0.8;
		double valScale = H / 5.0;

		if (angularFrequencies.isEmpty())
		{
			return;
		}
		double minFrequency = -1.0;
		double maxFrequency = 100 * Math.PI;

		for (int k = 0; k < angularFrequencies.size(); k++) {
			if (angularFrequencies.get(k) > maxFrequency) {
				break;
			}
			ctx.strokeLine(
					W * (angularFrequencies.get(k) - minFrequency) / (double)(maxFrequency - minFrequency),
					valOffset,
					W * (angularFrequencies.get(k) - minFrequency) / (double)(maxFrequency - minFrequency),
					valOffset - valScale * voltage.at(k).getAbs()
			);
		}
	}

	private static void drawFrequencyDomainCurrent(GraphicsContext ctx, ArrayList<Double> angularFrequencies, Vector current, double W, double H)
	{
		ctx.setStroke(Color.YELLOW);
		ctx.setLineWidth(0.8);
		ctx.strokeText("I(omega) [A]", 10,40);
		ctx.setLineWidth(2.0);

		double valOffset = H * 0.8;
		double valScale = H / 5.0;

		if (angularFrequencies.isEmpty())
		{
			return;
		}
		double minFrequency = -1;
		double maxFrequency = 100 * Math.PI;

		for (int k = 0; k < current.dimension; k++) {
			if (angularFrequencies.get(k) > maxFrequency) {
				break;
			}
			ctx.strokeLine(
					W * (angularFrequencies.get(k) - minFrequency) / (double)(maxFrequency - minFrequency),
					valOffset,
					W * (angularFrequencies.get(k) - minFrequency) / (double)(maxFrequency - minFrequency),
					valOffset - valScale * current.at(k).getAbs()
			);
		}
	}

	private static void drawFrequencyDomainLabels(GraphicsContext ctx, double W, double H)
	{
		double minFrequency = -1.0;
		double maxFrequency = 100 * Math.PI;

		double valOffset = H * 0.8;
		double valScale = H / 5.0;

		ctx.setStroke(Color.BLACK);
		ctx.setLineWidth(0.5);
		int labelCount = 5;
		int piStep = (int)((maxFrequency - minFrequency) / Math.PI) / labelCount;
		for (int k = 0; k < labelCount; k++) {
			ctx.strokeText(
					Integer.toString(k * piStep).concat("Pi"),
					W * (k * piStep * Math.PI - minFrequency) / (double)(maxFrequency - minFrequency),
					valOffset + 15
			);
		}
	}

	private static void drawTimeDomainVoltage(GraphicsContext ctx, double U, double W, double H) {
		ctx.setStroke(Color.GREEN);
		ctx.setLineWidth(0.8);
		ctx.strokeText("U = " + String.format("%,.5f", U) + " V", 10,20);
		ctx.setLineWidth(2.0);
		double prevVal = 0;
		double valOffset = H / 2.0;
		double valScale = H / 2.0 * scopeTimeValueScale;
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
		ctx.setLineWidth(0.8);
		ctx.strokeText("I = " + String.format("%,.5f", I) + " A", 10,40);
		ctx.setLineWidth(2.0);

		double prevVal = 0;
		double valOffset = H / 2.0;
		double valScale = H / 2.0 * scopeTimeValueScale;
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

	public static void updateScopeSamples(Component component)
	{
		synchronized (accessMutexObj)
		{
			scopeCurrentBuffer.clear();
			scopeVoltageBuffer.clear();
			if (null == component) {
				return;
			}
			Component clonedSelection;
			try {
				clonedSelection = component.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}

			double virtualTimeSec = scopeStartTimeSec;
			for (int i = 0; i < maxScopeDataPoints; i++)
			{
				component.updateTimeDomainParameters(virtualTimeSec, component.getParent().getSimulatedAngularFrequencies());
				scopeCurrentBuffer.add(component.getTimeDomainCurrent());
				scopeVoltageBuffer.add(component.getTimeDomainVoltageDrop());
				virtualTimeSec += scopeSampleTimeStep;
			}
		}
	}

	public static void setScopeTimeInterval(double timeSec)
	{
		synchronized (accessMutexObj)
		{
			scopeSampleTimeStep = timeSec / maxScopeDataPoints;
		}
	}

	public static double getScopeTimeInterval()
	{
		synchronized (accessMutexObj)
		{
			return scopeSampleTimeStep * maxScopeDataPoints;
		}
	}

	public static void setScopeStartTime(double startSec)
	{
		synchronized (accessMutexObj)
		{
			scopeStartTimeSec = startSec;
		}
	}

	public static double getScopeStartTime()
	{
		synchronized (accessMutexObj)
		{
			return scopeStartTimeSec;
		}
	}

	public static void setScopeTimeValueScale(double scale)
	{
		synchronized (accessMutexObj)
		{
			scopeTimeValueScale = scale;
		}
	}

	public static double getScopeTimeValueScale()
	{
		synchronized (accessMutexObj)
		{
			return scopeTimeValueScale;
		}
	}

	public static void updateScopeImage(Canvas canvas, Network network, double totalTimeSec, boolean running) {
		synchronized (accessMutexObj)
		{
			GraphicsContext ctx;
			if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
				double W = canvas.getWidth();
				double H = canvas.getHeight();
				ctx.setFill(Color.GREY);
				ctx.fillRect(0, 0, W, H);

				network.Component selected = network.getSelected();
				if (selected != null) {
					if (scopeInTimeDomain) {
						double I = selected.getTimeDomainCurrent();
						double U = selected.getTimeDomainVoltageDrop();

						if (running) {
							if (scopeCurrentBuffer.size() > maxScopeDataPoints) {	// Pop first element
								scopeCurrentBuffer.remove(0);
							}
							if (scopeVoltageBuffer.size() > maxScopeDataPoints) {	// Pop first element
								scopeVoltageBuffer.remove(0);
							}
						}

						setNormalDrawingAttributes(ctx);
						ctx.setLineWidth(0.8);
						ctx.strokeText("Intervallum kezdete = " + String.format("%,.2f", scopeStartTimeSec) + " s", 10,H - 25);
						ctx.strokeText("Intervallum hossza  = " + String.format("%,.2f", maxScopeDataPoints * scopeSampleTimeStep) + " s", 10,H - 10);

						double valOffset = H / 2.0;
						double valScale = H / 2.0 * scopeTimeValueScale;
						ctx.setLineWidth(1.0);
						ctx.strokeLine(0, valOffset, W, valOffset);
						ctx.setLineWidth(0.25);
						for (int i = 0; i * valScale < H / 2.0; i++) {
							ctx.strokeLine(0, valOffset - valScale * i, W, valOffset - valScale * i);
							ctx.strokeLine(0, valOffset + valScale * i, W, valOffset + valScale * i);
						}
						drawTimeDomainVoltage(ctx, U, W, H);
						drawTimeDomainCurrent(ctx, I, W, H);
						//drawResistance(ctx, R, W, H);
					}
					else {	// scope mode == frequency domain
						ArrayList<Double> angularFrequency = network.getSimulatedAngularFrequencies();
						Vector voltage = selected.getFrequencyDomainVoltageDrop();
						Vector current = selected.getFrequencyDomainCurrent();

						setNormalDrawingAttributes(ctx);
						ctx.setLineWidth(0.8);
						ctx.strokeText("t = " + String.format("%,.2f", totalTimeSec) + " s", 10,H - 20);

						double valOffset = H * 0.8;
						double valScale = H / 5.0;
						ctx.setLineWidth(1.0);
						ctx.strokeLine(0, valOffset, W, valOffset);
						ctx.setLineWidth(0.25);
						ctx.strokeLine(0, valOffset - valScale * 1, W, valOffset - valScale * 1);
						ctx.strokeLine(0, valOffset - valScale * 2, W, valOffset - valScale * 2);
						ctx.strokeLine(0, valOffset - valScale * 3, W, valOffset - valScale * 3);
						ctx.strokeLine(0, valOffset - valScale * 4, W, valOffset - valScale * 4);
						ctx.strokeLine(0, valOffset - valScale * 5, W, valOffset - valScale * 5);

						drawFrequencyDomainVoltage(ctx, angularFrequency, voltage, W, H);
						drawFrequencyDomainCurrent(ctx, angularFrequency, current, W, H);
						drawFrequencyDomainLabels(ctx, W, H);
					}
				}
				else {
					scopeCurrentBuffer.clear();
					scopeVoltageBuffer.clear();
				}
			}
		}
	}

	public static void toggleScopeMode()
	{
		synchronized (accessMutexObj)
		{
			scopeInTimeDomain = !scopeInTimeDomain;
		}
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
