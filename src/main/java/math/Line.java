package main.java.math;

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
		
		a.setAt(0, ax);
		a.setAt(1, ay);

		b.setAt(0, bx);
		b.setAt(1, by);
		
	}

	public static void transform(Line line, float scale, float angle, Vector offset) {

		line.a = MyMath.multiply(scale, line.a);
		line.b = MyMath.multiply(scale, line.b);
		
		Matrix rotation = new Matrix(2,2);
		rotation.setAt(0, 0, (float)Math.cos(angle));
		rotation.setAt(0, 1, (float)-Math.sin(angle));

		rotation.setAt(1, 0, (float)Math.sin(angle));
		rotation.setAt(1, 1, (float)Math.cos(angle));

		line.a = MyMath.multiply(rotation, line.a);
		line.b = MyMath.multiply(rotation, line.b);
		
		
		line.a = MyMath.add(line.a, offset);
		line.b = MyMath.add(line.b, offset);
	}
}
