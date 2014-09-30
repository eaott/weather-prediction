package weather.util;

public interface DistanceFunction {
	double dist(double x1, double y1, double x2, double y2);
	public static DistanceFunction EUCLIDEAN = new DistanceFunction(){
		@Override
		public double dist(double x1, double y1, double x2, double y2) {
			return Math.hypot(x1 - x2, y1 - y2);
		}
	};
	public static DistanceFunction MANHATTAN = new DistanceFunction() {
		@Override
		public double dist(double x1, double y1, double x2, double y2) {
			return Math.abs(x1 - x2) + Math.abs(y1 - y2);
		}	
	};
}
