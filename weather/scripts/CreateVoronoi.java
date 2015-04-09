package weather.scripts;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import weather.data.Constants;
import weather.process.Voronoi;
import weather.util.DistanceFunction;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import static weather.data.Constants.*;

/**
 * Purpose: Create img and serialized int[][] -> index to Sensor[] for later.
 * Prereqs: Create Sensor[] using CreateSensors in the Thesis_Data folder
 * @author Evan
 *
 */
public class CreateVoronoi {

	public static void main(String[] args) throws Throwable {
		final File rainDir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File radarDir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
				
		// Not really needed -- can remove.
		Random rand = new Random();
		rand.setSeed(Constants.RAND_SEED);
		// Available radars.
		final String[] radarCodes = {"EWX", "GRK"};
		
		// All available rain sensors.
		Sensor[] sensorArr = Serializer.readSensors(rainDir, "\\LCRA\\SENSORS");
		
		// 0xffRRGGBB colors for each sensor.
		int[] colors = new int[sensorArr.length];
		for (int i = 0; i < colors.length; i++)
		{
			int r = rand.nextInt(256);
			int g = rand.nextInt(256);
			int b = rand.nextInt(256);
			int color = 0xff000000 + (r << 16) + (g << 8) + b;
			colors[i] = color;
		}
		
		for (String radarCode : radarCodes)
		{
			double lonPerPx = GRK_SCALE;
			double latPerPx = GRK_SCALE;
			double startLon = BOUND_W;
			double startLat = BOUND_N;
			int WIDTH = GRK_WIDTH;
			int HEIGHT = GRK_HEIGHT;
			if (radarCode.equals("EWX"))
			{
				lonPerPx = EWX_SCALE;
				latPerPx = EWX_SCALE;
				WIDTH = EWX_WIDTH;
				HEIGHT = EWX_HEIGHT;
			}
			

			/**
			 *     <------ x, c, lon ----->
			 *  ^
			 *  |
			 *  |
			 *  |
			 *  y, r, lat
			 *  |
			 *  |
			 *  |
			 *  v
			 *  
			 *  
			 * 
			 * 
			 */
			
			// Convert points to coordinate system of the radar image.
			Point[] convertedPoints = new Point[sensorArr.length];
			for (int i = 0; i < convertedPoints.length; i++)
			{
				double dlat = sensorArr[i].getLat();
				double dlon = sensorArr[i].getLon();
				System.out.printf("%f %f\n",dlat,dlon);
				// on y axis, which is also the r axis
				dlat = (dlat - startLat) / latPerPx;
				dlon = (dlon - startLon) / lonPerPx;
				System.out.printf("%f %f\n",dlat,dlon);
				int x = (int)(Math.round(dlon));
				int y = (int)(Math.round(dlat));
				convertedPoints[i] = new Point(-y, x);
				System.out.println(convertedPoints[i]);
			}
			
			// Calculation will take a long time.
			int[][] data = Voronoi.generateMapSlow(HEIGHT, WIDTH, convertedPoints, DistanceFunction.EUCLIDEAN, true);
			
			Serializer.writeVoronoi(data, radarDir, radarCode);
			
			BufferedImage img = new BufferedImage(data[0].length, data.length, BufferedImage.TYPE_INT_ARGB);
			for (int r = 0; r < data.length; r++) {
				for (int c = 0; c < data[0].length; c++) {
					img.setRGB(c, r, colors[data[r][c] % colors.length]);
				}
			}
			ImageIO.write(img, "gif", new File(radarDir, "Voronoi_" + radarCode + "_test.gif"));
		}
		
	}

}
