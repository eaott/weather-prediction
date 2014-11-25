package weather.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
}
