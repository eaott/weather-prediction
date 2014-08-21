package weather.util;

public class Point implements Comparable<Point>{
	int r, c;
	public Point(int r, int c){
		this.r = r;
		this.c = c;
	}
	public int getR() {
		return r;
	}
	public int getC() {
		return c;
	}
	public int hashCode()
	{
		return r * 31 + c;
	}
	public boolean equals(Object o)
	{
		if (!(o instanceof Point))
			return false;
		return ((Point)o).r == r && ((Point)o).c == c;
	}
	public int compareTo(Point arg0) {
		if (r != arg0.r)
			return r - arg0.r;
		return c - arg0.c;
	}
}
