package weather.util;

public class CoordinateConversion
{

	// In miles.
	public static final double EARTH_RADIUS = 3959.0;
	/**
	dlat: starting latitude (degrees)
	dlon: starting longitude (degrees)
	dist: distance (miles)
	bearing: angle from north (degrees CW)

	returns: (lat, lon) (degrees)
	*/
	public static Tuple<Double, Double> 
			startDistBearingToLatLong(double dlat,
									  double dlon,
									  double dist,
									  double dbearing)
	{
		double rlat = Math.toRadians(dlat);
		double rlon = Math.toRadians(dlon);
		double delta = dist / EARTH_RADIUS;
		double rbearing = Math.toRadians(dbearing);
		double rlat2 = Math.asin(Math.sin(rlat) * Math.cos(delta) +
								Math.cos(rlat) * Math.sin(delta) * Math.cos(rbearing));
		double rlon2 = rlon + Math.atan2(Math.sin(rbearing) * Math.sin(delta) * Math.cos(rlat),
			Math.cos(delta) - Math.sin(rlat) * Math.sin(rlat2));
		double dlat2 = (Math.toDegrees(rlat2) + 720) % 360;
		double dlon2 = (Math.toDegrees(rlon2) + 720) % 360;
		return new Tuple<Double, Double>(dlat2, dlon2);
	}

	public static void main(String[] args) throws Throwable{
		Tuple<Double, Double> res = startDistBearingToLatLong(34, 100, 1000, 270);
		System.out.println(res.first());
		System.out.println(res.second());
	}
}