package weather.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import weather.process.Voronoi;
import weather.util.DistanceFunction;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import static weather.util.CoordinateConversion.degreeToDMS;

/**
 * Constants taken from the .gfw files and other sources.
 * Lat/Lon values are given in N or E. Offsets are given from the northwestern-most
 * corner.
 * 
 * In the voronoi images, N offset corresponds to what would normally be treated as a column.
 * So r ~ E, c ~ N.
 * @author Evan
 *
 */
public class Constants {
	public static final long RAND_SEED = 14565415141784562L;
	
	public static final int GRK_N_OFFSET = -200;
	public static final int GRK_E_OFFSET = 50;
	public static final int GRK_SIZE = 125;
	
	// Values taken from .gfw files.
	public static final double GRK_W = -99.8897353432395;
	public static final double GRK_N = 33.0194891496138;
	public static final double GRK_SCALE = 0.00836972323330964;
	
	public static final double BOUND_W = GRK_W + GRK_SCALE * GRK_E_OFFSET;
	public static final double BOUND_E = GRK_W + GRK_SCALE * (GRK_E_OFFSET + GRK_SIZE);
	public static final double BOUND_N = GRK_N + GRK_SCALE * GRK_N_OFFSET;
	public static final double BOUND_S = GRK_N + GRK_SCALE * (GRK_N_OFFSET - GRK_SIZE);
	
	// Values taken from .gfw files.
	public static final double EWX_W = -100.508957144997;
	public static final double EWX_N = 31.9778661485152;
	public static final double EWX_SCALE = 0.00828366366299714;
			
	
	public static final int EWX_N_OFFSET = (int)((BOUND_N - EWX_N) / EWX_SCALE + 0.5);
	public static final int EWX_E_OFFSET = (int)((BOUND_W - EWX_W) / EWX_SCALE + 0.5);
	public static final int EWX_SIZE = (int)(GRK_SIZE * GRK_SCALE / EWX_SCALE + 0.5);
	
	public static void main(String[] args) throws Throwable
	{
		int SCALE = 4;
		File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		Sensor[] sensorArr = Serializer.readSensors(dir, "SENSORS");
		
		Point[] sensorPoints = new Point[sensorArr.length];
		for (int i = 0; i < sensorArr.length; i++)
		{
			Sensor s = sensorArr[i];
			int nOffset =  -((int)((s.getLat() - BOUND_N) * SCALE / (GRK_SCALE) + 0.5));
			int eOffset =  (int)((s.getLon() - BOUND_W) * SCALE / (GRK_SCALE) + 0.5);
			sensorPoints[i] = new Point(nOffset, eOffset);
		}
		
//		int[][] voronoi = Voronoi.generateMapSlow(GRK_SIZE * SCALE, GRK_SIZE * SCALE, sensorPoints, DistanceFunction.EUCLIDEAN, true);
//		Serializer.writeVoronoi(voronoi, dir, "GRK_EXTENDED");
		
		
		int[][] grk = Serializer.readVoronoi(dir, "GRK_EXTENDED");
		int[] colors = new int[300];
		Random rand = new Random();
//		rand.setSeed(RAND_SEED);
		for (int i = 0; i < colors.length; i++)
		{
			int r = rand.nextInt(256);
			int g = rand.nextInt(256);
			int b = rand.nextInt(256);
			int color = 0xff000000 + (r << 16) + (r << 8) + r;
			colors[i] = color;
		}
		BufferedImage img = new BufferedImage(GRK_SIZE * SCALE, GRK_SIZE * SCALE, BufferedImage.TYPE_INT_ARGB);
		for (int c = 0; c < grk[0].length; c++) {
			for (int r = 0; r < grk.length; r++) {
				img.setRGB(r, c, colors[grk[r][c] % colors.length]);
			}
		}
		
		int[] rs = {-2, -2, -2, -2, -2, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
		int[] cs = {-2, -1,  0,  1,  2, -2, -1,  0,  1,  2, -2, -1,  0,  1,  2, -2, -1,  0,  1,  2, -2, -1,  0,  1,  2}; 
		
		for (Point p : sensorPoints)
		{
			for (int i = 0; i < rs.length; i++)
			{
				int r = p.getR() + rs[i];
				int c = p.getC() + cs[i];
				if (r >= 0 && r < GRK_SIZE * SCALE && c >= 0 && c < GRK_SIZE * SCALE)
					img.setRGB(r, c, 0xffff0000);
			}
		}
		
		
		
		ImageIO.write(img, "png", new File("C:/Users/Evan/Dropbox/THESIS/images/voronoi.png"));

	}
	
	
}
