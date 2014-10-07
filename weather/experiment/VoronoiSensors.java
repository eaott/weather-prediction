package weather.experiment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import weather.network.Label;
import weather.network.Network;
import weather.process.Voronoi;
import weather.util.DataIO;
import weather.util.DistanceFunction;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Tuple;

public class VoronoiSensors {
	// tolerance for difference, in milliseconds
	final static long TIME_TOLERANCE = 5 * 60 * 1000;
	
	final static int HIDDEN = 0;
	
	final static double MAX_DISTANCE = 1.0;
	
	final static double LEARNING_RATE = 0.15;
	
	final static int ITERATIONS = 1;
	
	public static void main(String[] args) throws Throwable{
		final File rainDir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File radarDir = new File("C:\\Users\\Evan\\GitProjects\\weather-prediction\\data2\\fullTest\\");
		final File sensorFile = new File(rainDir, "HydrometBook2.csv");
		
		// Map<String sensorName, Map<Long time, Double rainVal>>
		final Map<String, TreeMap<Long, Double>> rainMap = new HashMap<>();
		
		// Map<String type, int[][] voronoi>
		final Map<String, int[][]> radarVoronoiMap = new HashMap<>();
		
		// Not really needed -- can remove.
		final long randSeed = 14565415141784562L;
		
		// Available radars.
		final String[] radarCodes = {"EWX", "GRK"};
		
		// All available rain sensors.
		Sensor[] sensorArr = null;
		
		// 0xffRRGGBB colors for each sensor.
		int[] colors = null;
		
		
		
		for (String radarCode : radarCodes)
		{
			// First, read in all sensor locations, create colors.
			if (sensorArr == null || colors == null)
			{
				Random rand = new Random();
				rand.setSeed(randSeed);
				List<Sensor> list = new ArrayList<>();
				
				// Read in all available sensors and associated rain data.
				CSVParser p = CSVParser.parse(sensorFile, Charset.defaultCharset(), 
						CSVFormat.DEFAULT);
				for (CSVRecord r : p)
				{
					String name = r.get(0);
					String lat = r.get(5);
					String lon = r.get(6);
					double dlat = 0;
					double dlon = 0;
					try {
						dlat = Double.parseDouble(lat);
						dlon = Double.parseDouble(lon);
					}
					catch(Exception e)
					{
						continue;
					}
					
					list.add(new Sensor(name, dlat, dlon));
					
					// Sensor is created. Now, read in its rain data.
					File file = new File(rainDir, name + ".csv");
					if (!file.exists())
						continue;
					
					rainMap.put(name, getRainValues(file));
				}
				p.close();
				// Hacked way to create the array.
				sensorArr = list.toArray(new Sensor[0]);
				
				colors = new int[sensorArr.length];
				for (int i = 0; i < colors.length; i++)
				{
					int r = rand.nextInt(256);
					int g = rand.nextInt(256);
					int b = rand.nextInt(256);
					int color = 0xff000000 + (r << 16) + (g << 8) + b;
					colors[i] = color;
				}
			}
			
			// For this radar, find its projection information.
			Scanner boundaryIn = new Scanner(new File(radarDir, radarCode + "_N1P_0.gfw"));
			double lonPerPx = boundaryIn.nextDouble();
			boundaryIn.nextDouble(); // rotation
			boundaryIn.nextDouble(); // rotation
			double latPerPx = boundaryIn.nextDouble();
			double startLon = boundaryIn.nextDouble();
			double startLat = boundaryIn.nextDouble();
			boundaryIn.close();
			
			// For this radar, find its image size.
			BufferedImage sizeImg = ImageIO.read(new File(radarDir, radarCode + "_size.gif"));
			int WIDTH = sizeImg.getWidth();
			int HEIGHT = sizeImg.getWidth();
			
			// Convert points to coordinate system of the radar image.
			Point[] convertedPoints = new Point[sensorArr.length];
			for (int i = 0; i < convertedPoints.length; i++)
			{
				double dlat = sensorArr[i].getLat();
				double dlon = sensorArr[i].getLon();
				// on y axis
				dlat = (dlat - startLat) / latPerPx;
				dlon = (dlon - startLon) / lonPerPx;
				
				int x = (int)(Math.round(dlon));
				int y = (int)(Math.round(dlat));
				convertedPoints[i] = new Point(x, y);
			}
			
			// Calculation will take a long time.
			int[][] data = Voronoi.generateMapSlow(WIDTH, HEIGHT, convertedPoints, DistanceFunction.EUCLIDEAN, true);
			
			radarVoronoiMap.put(radarCode, data);
			
			BufferedImage img = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_ARGB);
			for (int r = 0; r < data.length; r++) {
				for (int c = 0; c < data[0].length; c++) {
					img.setRGB(r, c, colors[data[r][c] % colors.length]);
				}
			}
			ImageIO.write(img, "gif", new File(radarDir, "Voronoi_" + radarCode + "_test.gif"));
		}
		
		// Map<String nameOfSensor, TreeMap<Long recordTime, Double rainValue>> rainMap -- has all rain data for each sensor.
		// Map<String radarName, int[][] voronoi> sensorMap -- maps the sensors that correspond to each pixel for each radar.
		// Sensor[] sensorArr -- all sensors (may not have data for all)
		
		
		for (final String radarCode : radarCodes)
		{
			FilenameFilter filter = new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(radarCode + "_[0-9]{8}_[0-9]{4}.gif");
				}};
			final int[][] voronoi = radarVoronoiMap.get(radarCode);
			Tuple<Label[], Map<Integer,Integer>> tuple = DataIO.getLabels(radarDir, filter);
			Label[] inputLabels = tuple.first();
			Label[] outputLabels = {new Label("rain", 0)};
			
			Network n = Network.naiveLinear(voronoi.length, voronoi[0].length, inputLabels, outputLabels, HIDDEN, MAX_DISTANCE, LEARNING_RATE);
			
			final SimpleDateFormat radarParser = new SimpleDateFormat("'" + radarCode + "_'yyyyMMdd'_'HHmm'.gif'");
			File[] radarFiles = radarDir.listFiles(filter);
			
			for (int ITER = 0; ITER < ITERATIONS; ITER++)
			{
				for (File radarFile : radarFiles)
				{				
					double[][][] inputData = DataIO.getData(radarFile, tuple.second());
					
					long recordTime = radarParser.parse(radarFile.getName()).getTime();
	
					double[][][] outputData = getRainfall(voronoi, sensorArr, rainMap, recordTime);
				
					n.train(inputData, outputData);
				}
			
			}
			
			// n is trained... time for output. FIXME start here... sorry, laptop died...
		}
	}

	/**
	 * Reads rain data from the given file. Expects that the first column is the time of the record
	 * (MMM dd yyyy hh:mmaa), with the second column as the rain value (in inches).
	 */
	private static TreeMap<Long, Double> getRainValues(File file) throws IOException, ParseException {
		TreeMap<Long, Double> myRain = new TreeMap<>();
		CSVParser p = CSVParser.parse(file, Charset.defaultCharset(), 
				CSVFormat.DEFAULT);
		SimpleDateFormat f = new SimpleDateFormat("MMM dd yyyy hh:mmaa");
		for (CSVRecord r : p)
		{
			String sTime = r.get(0);
			String sRain = r.get(1);
			
			Date d = f.parse(sTime);
			double value = Double.parseDouble(sRain);
			
			myRain.put(d.getTime(), value);
		}
		p.close();
		return myRain;
	}
	
	private static double[][][] getRainfall(
			int[][] voronoi, 
			Sensor[] sensorArr, 
			Map<String, TreeMap<Long, Double>> rainMap,
			long recordTime)
	{
		double[][][] outputData = new double[voronoi.length][voronoi[0].length][1];
		for (int r = 0; r < voronoi.length; r++)
			for (int c = 0; c < voronoi[0].length; c++)
			{
				Map.Entry<Long, Double> entry = rainMap.get(sensorArr[voronoi[r][c]].name).ceilingEntry(recordTime);
				if (entry != null && entry.getKey() - recordTime <= TIME_TOLERANCE)
					outputData[r][c][0] = entry.getValue();
			}
		return outputData;
	}
}
