package weather.experiment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
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
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import weather.network.Label;
import weather.network.Network;
import weather.util.DataIO;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

public class VoronoiSensors {
	static final File rainDir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data_Sensor\\");
	static final File radarDir = new File("C:\\Users\\Evan\\GitProjects\\weather-prediction\\data2\\fullTest\\");
	static final File sensorFile = new File(rainDir, "HydrometBook2.csv");
	
	// Available radars.
	static final String[] radarCodes = {"EWX", "GRK"};
	
	public static void main(String[] args) throws Throwable{
		final long[] TIME_TOLERANCES = {1000 * 60 * 10};
		final int[] HIDDENS = {0};
		final double[] MAX_DISTANCES = {1.0, 2.0};
		final double[] LEARNING_RATES = {0.05, 0.25, 0.45, 0.65};
		final int[] NUM_ITERATIONS = {1, 2, 4, 8, 16, 32, 64};
		final double[] TARGET_VALUES = {0.0, 0.5, 1.0};
		CSVPrinter out = new CSVPrinter(new FileWriter(new File(radarDir, "EXPERIMENTS.csv")), CSVFormat.DEFAULT);
		
		out.print("TIME_TOLERANCE");
		out.print("HIDDEN");
		out.print("MAX_DISTANCE");
		out.print("LEARNING_RATE");
		out.print("ITERATIONS");
		out.print("TARGET_VALUE");
		out.print("TIME_FOR_ALL_SENSORS");
		for (String radarCode : radarCodes)
		{
			out.print(radarCode + " MEAN");
			out.print(radarCode + " STD");
		}
		out.println();
		
		
		for (long TIME_TOLERANCE : TIME_TOLERANCES)
			for (int HIDDEN : HIDDENS)
				for (double MAX_DISTANCE : MAX_DISTANCES)
					for (double LEARNING_RATE : LEARNING_RATES)
						for (int ITERATIONS : NUM_ITERATIONS)
							for (double TARGET_VALUE : TARGET_VALUES)
							{
								long time = System.currentTimeMillis();
								double[] result = run(TIME_TOLERANCE, HIDDEN, MAX_DISTANCE, LEARNING_RATE, ITERATIONS, TARGET_VALUE);
								out.print(TIME_TOLERANCE);
								out.print(HIDDEN);
								out.print(MAX_DISTANCE);
								out.print(LEARNING_RATE);
								out.print(ITERATIONS);
								out.print(TARGET_VALUE);
								out.print(System.currentTimeMillis() - time);
								for (double d : result)
									out.print(d);
								out.println();
							}
		out.close();
	}
	
	public static double[] run(long TIME_TOLERANCE, int HIDDEN, double MAX_DISTANCE, double LEARNING_RATE, int ITERATIONS, double TARGET_VALUE) throws Throwable
	{
		System.out.printf("TIME_TOLERANCE: %d\nHIDDEN: %d\nMAX_DISTANCE: %f\n" +
			"LEARNING_RATE: %f\nITERATIONS: %d\n",TIME_TOLERANCE, HIDDEN, MAX_DISTANCE, LEARNING_RATE, ITERATIONS);
		
		// Map<String sensorName, Map<Long time, Double rainVal>>
		final Map<String, TreeMap<Long, Double>> rainMap = new HashMap<>();
		
		// Map<String type, int[][] voronoi>
		final Map<String, int[][]> radarVoronoiMap = new HashMap<>();
		
		// Not really needed -- can remove.
		final long randSeed = 14565415141784562L;
		
		double[] RESULT = new double[radarCodes.length * 2];
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
					File file = new File(rainDir, name.trim() + ".csv");
					if (!file.exists())
						continue;
					
					rainMap.put(name.trim(), getRainValues(file));
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
	
			// Calculation will take a long time.
			int[][] data = Serializer.readVoronoi(radarDir, radarCode);
			
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
		
		int RESULT_INDEX = 0;
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
			System.out.println("creating network for " + radarCode);
			Network n = Network.naiveLinear(voronoi.length, voronoi[0].length, inputLabels, outputLabels, HIDDEN, MAX_DISTANCE, LEARNING_RATE);
			System.out.println("network created for " + radarCode);
			final SimpleDateFormat radarParser = new SimpleDateFormat("'" + radarCode + "_'yyyyMMdd'_'HHmm'.gif'");
			File[] radarFiles = radarDir.listFiles(filter);
			
			for (int ITER = 0; ITER < ITERATIONS; ITER++)
			{
				for (File radarFile : radarFiles)
				{				
					double[][][] inputData = DataIO.getData(radarFile, tuple.second());
					
					Date date = radarParser.parse(radarFile.getName());
										
					long recordTime = date.getTime();
	
					// FIXME if TARGET_VALUE is included, it actually ignores all rain data.
					double[][][] outputData = getRainfall(voronoi, sensorArr, rainMap, recordTime, TIME_TOLERANCE, TARGET_VALUE);
									
					n.train(inputData, outputData);
				}
			
			}
			

			// Now, output stats on the difference.
			double diff1 = 0, diff2 = 0;
			for (File radarFile : radarFiles)
			{				
				double[][][] inputData = DataIO.getData(radarFile, tuple.second());
				
				long recordTime = radarParser.parse(radarFile.getName()).getTime();

				// FIXME if TARGET_VALUE is included, it actually ignores all rain data.
				double[][][] outputData = getRainfall(voronoi, sensorArr, rainMap, recordTime, TIME_TOLERANCE, TARGET_VALUE);
			
				n.processInput(inputData);
				
				double[][][] predictedOutput = n.getOutput();

				for (int r = 0; r < outputData.length; r++)
				{
					for (int c = 0; c < outputData[0].length; c++)
					{
						for (int k = 0; k < outputData[0][0].length; k++)
						{
							double diff = predictedOutput[r][c][k] - outputData[r][c][k];
							diff1 += diff;
							diff2 += diff * diff;
						}
					}
				}
			}
			n.close();
			int N = voronoi.length * voronoi[0].length * outputLabels.length;
			RESULT[RESULT_INDEX++] = diff1 / N;
			RESULT[RESULT_INDEX++] = Math.sqrt(diff2 / (N - 1));
			System.out.println(radarCode + " done");
		}
		return RESULT;
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
	
	@SuppressWarnings("unused")
	private static double[][][] getRainfall(
			int[][] voronoi, 
			Sensor[] sensorArr, 
			Map<String, TreeMap<Long, Double>> rainMap,
			long recordTime,
			final long TIME_TOLERANCE)
	{
		double[][][] outputData = new double[voronoi.length][voronoi[0].length][1];
		for (int r = 0; r < voronoi.length; r++)
			for (int c = 0; c < voronoi[0].length; c++)
			{
				// FIXME test to see if it will converge to center.
				outputData[r][c][0] = 0.5;
				String name = sensorArr[voronoi[r][c]].name;
				TreeMap<Long, Double> map = rainMap.get(name);
				if (map == null)
					continue;
				Map.Entry<Long, Double> entry = map.ceilingEntry(recordTime);
				if (entry != null && entry.getKey() - recordTime <= TIME_TOLERANCE)
				{
					System.out.printf("%s: %d %f\n",name, entry.getKey(), entry.getValue());
					outputData[r][c][0] = entry.getValue();
				}
			}
		return outputData;
	}
	
	private static double[][][] getRainfall(
			int[][] voronoi, 
			Sensor[] sensorArr, 
			Map<String, TreeMap<Long, Double>> rainMap,
			long recordTime,
			final long TIME_TOLERANCE,
			double TARGET_VALUE)
	{
		double[][][] outputData = new double[voronoi.length][voronoi[0].length][1];
		for (int r = 0; r < voronoi.length; r++)
			for (int c = 0; c < voronoi[0].length; c++)
			{
				outputData[r][c][0] = TARGET_VALUE;
			}
		return outputData;
	}
}
