package weather.util;

public class Sensor
{
	public double x;
	public double y;
	public double val;
	public String name;
	public Sensor(double x, double y, double val)
	{
		this.x = x;
		this.y = y;
		this.val = val;
	}
	public Sensor(String name, double x, double y, double val)
	{
		this.name = name;
		this.x = x;
		this.y = y;
		this.val = val;
	}
	
	public String toString()
	{
		return String.format("%s: (%f, %f)->%f", name, x, y, val);
	}
}