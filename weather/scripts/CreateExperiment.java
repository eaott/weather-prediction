package weather.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import static weather.data.Constants.*;
import weather.network.Label;
import weather.network.SimpleNetwork;
import weather.process.Voronoi;
import weather.util.DataIO;
import weather.util.DistanceFunction;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

import java.util.Map.Entry;

public class CreateExperiment {
	public static void main(String[] args) throws Throwable {
		long startTime = System.currentTimeMillis();
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File rainDir = new File(dir, "LCRA");
		// HIDDEN = 10 and NEIGHBOR_DISTANCE = 5.0 seem reasonable for memory purposes
		
		// Before conversion from polar to rectangular...
		// H 2 N 1 --> 1 hr / iteration
		// H 2 N 3 --> 2 hr / iteration
		// H 2 N 5 --> 5 hr / iteration
		// H 2 N 10 --> 18 hr / iteration
		
		// H 5 N 1 --> 2 hr / iteration
		// H 5 N 3 --> 3.5 hr / iteration
		// H 5 N 5 --> 8 hr / iteration
		
		// H 10 N 1 --> 4 hr/ iteration
		// H 10 N 3 --> 7 hr / iteration
		// H 10 N 5 --> 17 hr / iteration
		
		String networkName = "testNetwork16";
		int HIDDEN = 1;
		double NEIGHBOR_DISTANCE = 1.0;
		double LEARNING_RATE = 0.3;
		int MAX_ITERATIONS = 1;
		
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF_Test");
		
		
		double lonPerPx = GRK_SCALE;
		double latPerPx = GRK_SCALE;
		double startLon = BOUND_W;
		double startLat = BOUND_N;
		int[][] voronoi = Serializer.readVoronoi(dir, "GRK");
		Point[][] coordinateMap = Serializer.readCoordinateConversion(dir, "GRK_COORDINATE");
		Sensor[] sensors = Serializer.readSensors(rainDir, "SENSORS");
		
		double percentTraining = .9;
		// need to keep track of ones used for training.
		Set<String> testCases = new HashSet<>();
		
		double percentRain = 0.5;
				
		// Convert points to coordinate system of the radar image.
		Point[] convertedPoints = new Point[sensors.length];
		for (int i = 0; i < convertedPoints.length; i++)
		{
			double dlat = sensors[i].getLat();
			double dlon = sensors[i].getLon();
			if (Math.random() < percentRain)
			{
				// Need to instead have the voronoi use the same
				// list of sensors (to keep consistent with <code>maps</code>, but give
				// false locations. Put them at (0d, 0d).
				dlat = 0;
				dlon = 0;
			}
			// on y axis, which is also the r axis
			dlat = (dlat - startLat) / latPerPx;
			dlon = (dlon - startLon) / lonPerPx;
			int x = (int)(Math.round(dlon));
			int y = (int)(Math.round(dlat));
			convertedPoints[i] = new Point(-y, x);
		}
		
		int[][] input_voronoi = Voronoi.generateMapSlow(voronoi.length, voronoi[0].length, convertedPoints, DistanceFunction.EUCLIDEAN, false);

		
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		System.out.println("Data loaded. " + (System.currentTimeMillis() - startTime));
		System.out.printf("size: (%d, %d)\n", voronoi.length, voronoi[0].length);
		
		SimpleNetwork n = SimpleNetwork.naiveLinear(voronoi.length, voronoi[0].length, new Label[]{new Label("Doppler", 0),new Label("LCRA", 1)}, new Label[]{new Label("LCRA", 0)}, HIDDEN, NEIGHBOR_DISTANCE, LEARNING_RATE);
		System.out.println("Network created. " + (System.currentTimeMillis() - startTime));

		// Only use files that have a particular coverage percentage.
		String[] usableFileArr = Serializer.readFiles(dopplerDir, "USABLEFILES");
		
		System.out.println("Total files: " + (usableFileArr.length));
		
		double filesComplete = 0;
		double usedFiles = 0;
		long trainingTimeTotal = 0;
		
		for (int ITER = 0; ITER < MAX_ITERATIONS; ITER++)
		{
			for (String filename : usableFileArr)
			{
				if (ITER == 0 && Math.random() >= percentTraining)
				{
					testCases.add(filename);
					continue;
				}
				else if (testCases.contains(filename))
					continue;
				File file = new File(filename);
				Tuple<double[][][], Double> output_percent = DataIO.getRainDataFromMaps(voronoi, file, maps);
				double[][][] output_region = output_percent.first();
				double percent = output_percent.second();
				
				filesComplete++;
				double percentdone = (filesComplete) / (MAX_ITERATIONS * (usableFileArr.length * percentTraining));
				System.out.printf("percent coverage: %f -- %f done\n", percent, percentdone);
				
				if (percent < PERCENT_COVERAGE)
					continue;
				
				double[][][] input_netCDF = DataIO.getDataFromNetCDF(voronoi, file, coordinateMap);
				double[][][] input_voronoi_rain = DataIO.getRainDataFromMaps(input_voronoi, file, maps).first();
				double[][][] input_region = new double[input_netCDF.length][input_netCDF[0].length][2];
				for (int r = 0; r < input_region.length; r++)
					for (int c = 0; c < input_region[0].length; c++)
					{
						input_region[r][c][0] = input_netCDF[r][c][0];
						input_region[r][c][1] = input_voronoi_rain[r][c][0];
					}

				long trainStart = System.currentTimeMillis();

				n.train(input_region, output_region);
				trainingTimeTotal += System.currentTimeMillis() - trainStart;
				usedFiles++;
			}
		}
		
		System.out.println("Calculating training mse...");
		double trainMse = ErrorCalculator.mse(usableFileArr, testCases, n, voronoi, input_voronoi, coordinateMap, maps, false);
		System.out.println("Calculating testing mse...");
		double testMse = ErrorCalculator.mse(usableFileArr, testCases, n, voronoi, input_voronoi, coordinateMap, maps, true);
		
		System.out.println("Processed. " + (System.currentTimeMillis() - startTime));
		System.out.printf("Average time for training: %fms\n", trainingTimeTotal / usedFiles);
		System.out.printf("Train MSE: %.8f\n", trainMse);
		System.out.printf("Test MSE: %.8f\n", testMse);
		
		
		if (new File(dopplerDir, networkName + ".ser").exists())
		{
			System.out.print("Old name: " + networkName + ". New name? ");
			networkName = new Scanner(System.in).nextLine();
		}
		Serializer.writeNetwork(n, dopplerDir, networkName);
		
		n.close();
	}
}
