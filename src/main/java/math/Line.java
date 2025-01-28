package math;


/**
 * Represents a line in the 2D plane.
 * HUN: Szakasz a síkban.
 * @author Simon Zoltán
 *
 */
public class Line {

	public Vector a;
	public Vector b;

	public Line(Vector a, Vector b) {
		super();
		this.a = a;
		this.b = b;
	}

	public Line(float ax, float ay, float bx, float by) {
		a = new Vector(2);
		b = new Vector(2);
		
		a.setAt(0, new Complex(ax, 0));
		a.setAt(1, new Complex(ay, 0));

		b.setAt(0, new Complex(bx, 0));
		b.setAt(1, new Complex(by, 0));
		
	}

	/**
	 * Transforms the coordinates of the line.
	 * HUN: Transzformálja a szakaszt.
	 * @param line {@link Line} to be transformed.
	 * @param scale	float
	 * @param angle	float in radians.
	 * @param offset {@link Vector}.
	 */
	public static void transform(Line line, float scale, float angle, Vector offset) {

		line.a = MyMath.multiply(new Complex(scale, 0), line.a);
		line.b = MyMath.multiply(new Complex(scale, 0), line.b);
		
		Matrix rotation = new Matrix(2,2);
		rotation.setAt(0, 0, new Complex(Math.cos(angle), 0));
		rotation.setAt(0, 1, new Complex(-Math.sin(angle), 0));

		rotation.setAt(1, 0, new Complex(Math.sin(angle), 0));
		rotation.setAt(1, 1, new Complex(Math.cos(angle), 0));

		line.a = MyMath.multiply(rotation, line.a);
		line.b = MyMath.multiply(rotation, line.b);
		
		
		line.a = MyMath.add(line.a, offset);
		line.b = MyMath.add(line.b, offset);
	}
}
