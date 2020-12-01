package main.java.gui;

import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import main.java.math.Coordinate;
import main.java.math.Line;
import main.java.math.MyMath;
import main.java.math.Vector;
import main.java.network.Network;

/**
 * Auxiliary methods for drawing components.
 * HUN: Segéd metódsok komponensek rajzolásához.
 * @author simon
 *
 */
public class DrawingHelper {
	
	private static final javafx.scene.paint.Color COLOR_NORMAL = javafx.scene.paint.Color.BLACK;
	private static final javafx.scene.paint.Color COLOR_SELECT = javafx.scene.paint.Color.GREEN;
	private static final StrokeLineCap LINE_CAP_NORMAL = StrokeLineCap.ROUND;
	private static final double LINE_WIDTH_NORMAL = 2.0;
	private static final double LINE_WIDTH_SELECT = 0.6;
	private static final double DASHES_NORMAL = 0.0;
	private static final double[] DASHES_SELECT = new double[] {10.0, 3.0};
	private static final double DASH_OFFSET_SELECT = 5.0;
	
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
	protected static void updateCanvasContent(Canvas canvas, Network network) {
		GraphicsContext ctx;
		if (canvas != null && (ctx = canvas.getGraphicsContext2D()) != null) {
			ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			network.draw(ctx);
		}
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
	 */
	public static void drawShape(GraphicsContext ctx, Coordinate inputPos, Coordinate outputPos, List<Line> lines, float default_length, boolean selected) {

		Vector vInput  = MyMath.coordToVector(inputPos); 
		Vector vOutput = MyMath.coordToVector(outputPos);
		
		Vector orientation = MyMath.subtract(vOutput, vInput);
		float scale = MyMath.magnitude(orientation) / default_length;
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
		
}
