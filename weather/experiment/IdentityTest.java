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
import weather.network.SimpleNetwork;
import weather.util.DataIO;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

public class IdentityTest {
	static final File rainDir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data_Sensor\\");
	static final File radarDir = new File("C:\\Users\\Evan\\GitProjects\\weather-prediction\\data2\\fullTest\\");
	static final File sensorFile = new File(rainDir, "HydrometBook2.csv");
	
	// Available radars.
	static final String[] radarCodes = {"EWX", "GRK"};
	
	static final int XMIN = 100, XMAX = 200, YMIN = 100, YMAX = 200;
	
	public static void main(String[] args) throws Throwable{
		final int[] HIDDENS = {0, 1, 2, 4, 8};
		final double[] MAX_DISTANCES = {1.0, 1.5};
		final double[] LEARNING_RATES = {0.35};
		final int[] NUM_ITERATIONS = {1, 2, 4, 8, 16};
		CSVPrinter out = new CSVPrinter(new FileWriter(new File(radarDir, "identity.csv")), CSVFormat.DEFAULT);
		
		out.print("HIDDEN");
		out.print("MAX_DISTANCE");
		out.print("LEARNING_RATE");
		out.print("ITERATIONS");
		out.print("TIME_FOR_ALL_SENSORS");
		for (String radarCode : radarCodes)
		{
			out.print(radarCode + " STD");
		}
		out.println();
		
		for (int HIDDEN : HIDDENS)
			for (double MAX_DISTANCE : MAX_DISTANCES)
				for (double LEARNING_RATE : LEARNING_RATES)
					for (int ITERATIONS : NUM_ITERATIONS)
						{
							long time = System.currentTimeMillis();
							double[] result = run(HIDDEN, MAX_DISTANCE, LEARNING_RATE, ITERATIONS);
							out.print(HIDDEN);
							out.print(MAX_DISTANCE);
							out.print(LEARNING_RATE);
							out.print(ITERATIONS);
							out.print(System.currentTimeMillis() - time);
							for (double d : result)
								out.print(d);
							out.println();
						}
		out.close();
	}
	
	public static double[] run(int HIDDEN, double MAX_DISTANCE, double LEARNING_RATE, int ITERATIONS) throws Throwable
	{
		System.out.printf("HIDDEN: %d\nMAX_DISTANCE: %f\n" +
			"LEARNING_RATE: %f\nITERATIONS: %d\n", HIDDEN, MAX_DISTANCE, LEARNING_RATE, ITERATIONS);
		
		// Map<String sensorName, Map<Long time, Double rainVal>>
		final Map<String, TreeMap<Long, Double>> rainMap = new HashMap<>();
		
		// Map<String type, int[][] voronoi>
		final Map<String, int[][]> radarVoronoiMap = new HashMap<>();
		
		// Not really needed -- can remove.
		final long randSeed = 14565415141784562L;
		
		double[] RESULT = new double[radarCodes.length];
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
			int[][] bigdata = Serializer.readVoronoi(radarDir, radarCode);
			int[][] data = new int[XMAX - XMIN + 1][YMAX - YMIN + 1];
			for (int r = 0; r < data.length; r++)
				for (int c = 0; c < data[0].length; c++)
					data[r][c] = bigdata[r + XMIN][c + YMIN];
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
			
			for (int value : tuple.second().keySet())
				System.out.printf("%x\t", value);
			System.out.println();
			
			Label[] outputLabels = inputLabels;
			System.out.println("creating network for " + radarCode);
			SimpleNetwork n = SimpleNetwork.naiveLinear(voronoi.length, voronoi[0].length, inputLabels, outputLabels, HIDDEN, MAX_DISTANCE, LEARNING_RATE);
			System.out.println("network created for " + radarCode);
			final SimpleDateFormat radarParser = new SimpleDateFormat("'" + radarCode + "_'yyyyMMdd'_'HHmm'.gif'");
			File[] radarFiles = radarDir.listFiles(filter);
			double totalFiles = radarFiles.length * ITERATIONS;
			double index = 0;
			for (int ITER = 0; ITER < ITERATIONS; ITER++)
			{
				for (File radarFile : radarFiles)
				{				
					double[][][] bigInputData = DataIO.getData(radarFile, tuple.second());
					double[][][] inputData = new double[XMAX - XMIN + 1][YMAX - YMIN + 1][inputLabels.length];
					for (int r = 0; r < inputData.length; r++)
						for (int c = 0; c < inputData[0].length; c++)
							for (int k = 0; k < inputData[0][0].length; k++)
								inputData[r][c][k] = bigInputData[r + XMIN][c + YMIN][k];
					
					Date date = radarParser.parse(radarFile.getName());
										
					long recordTime = date.getTime();
	
					double[][][] outputData = getRainfall(voronoi, sensorArr, rainMap, recordTime, inputData);
					
					n.train(inputData, outputData);
					System.out.printf("\t%.1f%% trained\n", 100*(1 + index++) / totalFiles);
				}
			}
			

			// Now, output stats on the difference.
			double diff2 = 0;
			for (File radarFile : radarFiles)
			{				
				double[][][] bigInputData = DataIO.getData(radarFile, tuple.second());
				double[][][] inputData = new double[XMAX - XMIN + 1][YMAX - YMIN + 1][inputLabels.length];
				for (int r = 0; r < inputData.length; r++)
					for (int c = 0; c < inputData[0].length; c++)
						for (int k = 0; k < inputData[0][0].length; k++)
							inputData[r][c][k] = bigInputData[r + XMIN][c + YMIN][k];
				
				long recordTime = radarParser.parse(radarFile.getName()).getTime();

				// FIXME if TARGET_VALUE is included, it actually ignores all rain data.
				double[][][] outputData = getRainfall(voronoi, sensorArr, rainMap, recordTime, inputData);
			
				n.processInput(inputData);
				
				double[][][] predictedOutput = n.getOutput();

				for (int r = 0; r < outputData.length; r++)
				{
					for (int c = 0; c < outputData[0].length; c++)
					{
						for (int k = 0; k < outputData[0][0].length; k++)
						{
							double diff = predictedOutput[r][c][k] - outputData[r][c][k];
							diff2 += diff * diff;
						}
					}
				}
			}
			n.close();
			int N = voronoi.length * voronoi[0].length * outputLabels.length;
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
	
	
	private static double[][][] getRainfall(
			int[][] voronoi, 
			Sensor[] sensorArr, 
			Map<String, TreeMap<Long, Double>> rainMap,
			long recordTime,
			double[][][] input)
	{
		double[][][] outputData = new double[input.length][input[0].length][input[0][0].length];
		for (int r = 0; r < voronoi.length; r++)
			for (int c = 0; c < voronoi[0].length; c++)
			{
				for (int k = 0; k < input[0][0].length; k++)
				{
					outputData[r][c][k] = input[r][c][k];
				}
			}
		return outputData;
	}
}
