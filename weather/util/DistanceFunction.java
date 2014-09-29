package weather.util;

public interface DistanceFunction {
	double dist(double x1, double y1, double x2, double y2);
	public class EuclideanDistance implements DistanceFunction {
		@Override
		public double dist(double x1, double y1, double x2, double y2) {
			return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
		}
	}
	public class ManhattanDistance implements DistanceFunction {
		@Override
		public double dist(double x1, double y1, double x2, double y2) {
			return Math.abs(x1 - x2) + Math.abs(y1 - y2);
		}
		
	}
}
