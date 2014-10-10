package weather.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
	public static void writeVoronoi(int[][] voronoi, String filename)
	{
		try(FileOutputStream fileOut = new FileOutputStream(filename);
				ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
			out.writeInt(voronoi.length);
			out.writeInt(voronoi[0].length);
			for (int r = 0; r < voronoi.length; r++)
				for (int c = 0; c < voronoi[0].length; c++)
					out.writeInt(voronoi[r][c]);
		} catch (IOException e) {
 		}
	}
	public static int[][] readVoronoi(String filename)
	{
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
}
