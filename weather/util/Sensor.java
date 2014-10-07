package weather.util;

public class Sensor
{
	public double val;
	
	private final double lat;
	private final double lon;
	public String name;
	public Sensor(String name, double lat, double lon, double val)
	{
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.val = val;
	}
	public Sensor(String name, double lat, double lon)
	{
		this(name, lat, lon, 0);
	}
	public double getLat()
	{
		return lat;
	}
	public double getLon()
	{
		return lon;
	}
	public double getX(double startLat, double latPerPx)
	{
		return (lat - startLat) / latPerPx;
	}
	
	
	public String toString()
	{
		return String.format("%s: (%f, %f)->%f", name, lat, lon, val);
	}
}