package math;

/**
 * int coordinates of 2D point.
 * HUN: int koordináta 2D-ós pont leírására.
 * @author Simon Zoltán
 *
 */
public class Coordinate {
	public int x, y;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coordinate other = (Coordinate) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coordinate(Coordinate coor) {
		if (coor != this) {
			this.x = coor.x;
			this.y = coor.y;
		}
	}
	public static Coordinate snapToGrid(Coordinate coord, int gridSize) {
		double m = coord.x % gridSize;
    	coord.x = (int)((double)coord.x / (double)gridSize) * gridSize;
    	if (m >gridSize * 0.5) {
    		coord.x += gridSize;
    	}
    	m = coord.y % gridSize;
    	coord.y = (int)((double)coord.y / (double)gridSize) * gridSize; 
    	if (m > gridSize * 0.5) {
    		coord.y += gridSize;
    	}
    	
    	return new Coordinate(coord.x, coord.y);
    }

}
