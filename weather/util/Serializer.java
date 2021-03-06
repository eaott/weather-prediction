package weather.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import weather.network.SimpleNetwork;

public class Serializer {
	public static final String VORONOI_PREFIX = "voronoi_";
	public static final String VORONOI_SUFFIX = ".ser";
	public static void writeVoronoi(int[][] voronoi, File dir, String radarCode)
	{
		String fullFilename = new File(dir, String.format("%s%s%s", VORONOI_PREFIX, radarCode, VORONOI_SUFFIX)).getAbsolutePath();
		try(FileOutputStream fileOut = new FileOutputStream(fullFilename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeInt(voronoi.length);
			out.writeInt(voronoi[0].length);
			for (int r = 0; r < voronoi.length; r++)
				for (int c = 0; c < voronoi[0].length; c++)
					out.writeInt(voronoi[r][c]);
		} catch (IOException e) {
 		}
	}
	public static int[][] readVoronoi(File dir, String radarCode)
	{
		String filename = new File(dir, String.format("%s%s%s", VORONOI_PREFIX, radarCode, VORONOI_SUFFIX)).getAbsolutePath();
		int[][] result = null;
		try(FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn)) {
			int rows = in.readInt();
			int cols = in.readInt();
			result = new int[rows][cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
					result[r][c] = in.readInt();
		} catch (IOException e) {
 		}
		return result;
	}
	
	public static void writeSensors(Sensor[] sensors, File dir, String name)
	{
		String fullFilename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		try(FileOutputStream fileOut = new FileOutputStream(fullFilename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeInt(sensors.length);
			for (Sensor s : sensors)
				out.writeObject(s);
		} catch (IOException e) {
 		}
	}
	public static Sensor[] readSensors(File dir, String name)
	{
		String filename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		Sensor[] result = null;
		try(FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn)) {
			int num = in.readInt();
			result = new Sensor[num];
			for(int i = 0; i < num; i++)
			{
				result[i] = (Sensor)in.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
 		}
		return result;
	}
	
	public static void writeRain(Map[] vals, File dir, String name)
	{
		String fullFilename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		try(FileOutputStream fileOut = new FileOutputStream(fullFilename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeInt(vals.length);
			for (Map m : vals)
				out.writeObject(m);
		} catch (IOException e) {
 		}
	}
	
	public static Map[] readRain(File dir, String name)
	{
		String filename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		Map[] result = null;
		try(FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn)) {
			int num = in.readInt();
			result = new Map[num];
			for(int i = 0; i < num; i++)
			{
				result[i] = (Map)in.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
 		}
		return result;
	}
	
	public static void writeFiles(String[] files, File dir, String name)
	{
		String fullFilename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		try(FileOutputStream fileOut = new FileOutputStream(fullFilename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeInt(files.length);
			for (String s : files)
				out.writeObject(s);
		} catch (IOException e) {
 		}
	}
	public static String[] readFiles(File dir, String name)
	{
		String filename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		String[] result = null;
		try(FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn)) {
			int num = in.readInt();
			result = new String[num];
			for(int i = 0; i < num; i++)
			{
				result[i] = (String)in.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
 		}
		return result;
	}
	
	public static void writeNetwork(SimpleNetwork n, File dir, String name)
	{
		String fullFilename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		try(FileOutputStream fileOut = new FileOutputStream(fullFilename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeObject(n);
		} catch (IOException e) {
 		}
	}
	public static SimpleNetwork readNetwork(File dir, String name)
	{
		String filename = new File(dir, String.format("%s%s", name, VORONOI_SUFFIX)).getAbsolutePath();
		SimpleNetwork result = null;
		try(FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn)) {
			result = (SimpleNetwork)in.readObject();
		} catch (IOException | ClassNotFoundException e) {
 		}
		return result;
	}
	
	
	public static void writeCoordinateConversion(Point[][] points, File dir, String radarCode)
	{
		String fullFilename = new File(dir, String.format("%s%s%s", VORONOI_PREFIX, radarCode, VORONOI_SUFFIX)).getAbsolutePath();
		try(FileOutputStream fileOut = new FileOutputStream(fullFilename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeInt(points.length);
			out.writeInt(points[0].length);
			for (int r = 0; r < points.length; r++)
				for (int c = 0; c < points[0].length; c++)
				{
					if (points[r][c] == null)
					{
						System.out.println(r + " " + c);
						continue;
					}
					out.writeInt(points[r][c].getR());
					out.writeInt(points[r][c].getC());
				}
		} catch (IOException e) {
 		}
	}
	public static Point[][] readCoordinateConversion(File dir, String radarCode)
	{
		String filename = new File(dir, String.format("%s%s%s", VORONOI_PREFIX, radarCode, VORONOI_SUFFIX)).getAbsolutePath();
		Point[][] result = null;
		try(FileInputStream fileIn = new FileInputStream(filename);
				ObjectInputStream in = new ObjectInputStream(fileIn)) {
			int rows = in.readInt();
			int cols = in.readInt();
			result = new Point[rows][cols];
			for (int r = 0; r < rows; r++)
				for (int c = 0; c < cols; c++)
					result[r][c] = new Point(in.readInt(), in.readInt());
		} catch (IOException e) {
 		}
		return result;
	}
}
