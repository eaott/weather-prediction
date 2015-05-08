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
import weather.network.NetworkGraph;
import weather.network.SimpleNetwork;
import weather.process.LoopyBP;
import weather.process.Voronoi;
import weather.util.DataIO;
import weather.util.DistanceFunction;
import weather.util.ErrorCalculator;
import weather.util.PairwiseFunction;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CreateExperiment {
	final static int MSE = 0;
	final static int MSE_2 = 1;
	final static int MSE_S = 2;
	final static int MSE_S_2 = 3;
	final static int NUM_THINGS = 4;
	public static void main(String[] args) throws Throwable {
		long startTime = System.currentTimeMillis();
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File rainDir = new File(dir, "LCRA");
		// HIDDEN = 10 and NEIGHBOR_DISTANCE = 5.0 seem reasonable for memory purposes
		
		int HIDDEN = 5;
		double NEIGHBOR_DISTANCE = 3.0;
		int MAX_ITERATIONS = 10;
		double percentRain = 0.5;
		
		
		
		double percentTraining = .8;
		double LEARNING_RATE = 0.3;

		
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF");
		
		
		double startLon = BOUND_W;
		double startLat = BOUND_N;
		int[][] voronoi_grk = Serializer.readVoronoi(dir, "GRK");
		Point[][] coordinateMap_GRK = Serializer.readCoordinateConversion(dir, "GRK_COORDINATE");
		int[][] voronoi_ewx = Serializer.readVoronoi(dir, "EWX");
		Point[][] coordinateMap_EWX = Serializer.readCoordinateConversion(dir, "EWX_COORDINATE");
		Sensor[] sensors = Serializer.readSensors(rainDir, "SENSORS");
		
		
		
		// need to keep track of ones used for training.
		Set<String> testCases = new HashSet<>();
		
		
				
		// Convert points to coordinate system of the radar image.
		Point[] convertedPoints_GRK = new Point[sensors.length];
		
		for (int i = 0; i < convertedPoints_GRK.length; i++)
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
			dlat = (dlat - startLat) / GRK_SCALE;
			dlon = (dlon - startLon) / GRK_SCALE;
			int x_grk = (int)(Math.round(dlon));
			int y_grk = (int)(Math.round(dlat));
			convertedPoints_GRK[i] = new Point(-y_grk, x_grk);
			
		}
		
		int[][] input_voronoi_grk = Voronoi.generateMapSlow(voronoi_grk.length, voronoi_grk[0].length, convertedPoints_GRK, DistanceFunction.EUCLIDEAN, false);
		
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		System.out.println("Data loaded. " + (System.currentTimeMillis() - startTime));
		System.out.printf("size: (%d, %d)\n", voronoi_grk.length, voronoi_grk[0].length);
		
		SimpleNetwork n = SimpleNetwork.naiveLinear(voronoi_grk.length, voronoi_grk[0].length, new Label[]{new Label("DopplerGRK", 0),new Label("DopplerEWX", 1),new Label("LCRA", 2)}, new Label[]{new Label("LCRA", 0)}, HIDDEN, NEIGHBOR_DISTANCE, LEARNING_RATE);
		
		SimpleNetwork n_one = SimpleNetwork.naiveLinear(1, 1, new Label[]{new Label("DopplerGRK", 0),new Label("DopplerEWX", 1),new Label("LCRA", 2)}, new Label[]{new Label("LCRA", 0)}, HIDDEN, 1.0, LEARNING_RATE);
		
		System.out.println("Network created. " + (System.currentTimeMillis() - startTime));

		// Only use files that have a particular coverage percentage. 
		String[] usableFileArr = Serializer.readFiles(dopplerDir, "NONZEROFILES");
		
		System.out.println("Total files: " + (usableFileArr.length));
		
		double filesComplete = 0;
		
		for (int ITER = 0; ITER < MAX_ITERATIONS; ITER++)
		{
			for (int i = 0; i < usableFileArr.length; i+=2)
			{
				String filename_ewx = usableFileArr[i];
				File file_ewx = new File(dopplerDir, filename_ewx);
				String filename_grk = usableFileArr[i+1];
				File file_grk = new File(dopplerDir, filename_grk);

				if (ITER == 0 && Math.random() >= percentTraining)
				{
					testCases.add(filename_ewx);
					testCases.add(filename_grk);
					continue;
				}
				else if (testCases.contains(filename_ewx) || testCases.contains(filename_grk))
					continue;
				Tuple<double[][][], Double> output_percent = DataIO.getRainDataFromMaps(voronoi_grk, file_grk, maps);
				double[][][] output_region = output_percent.first();
				double percent = output_percent.second();
				
				filesComplete+=2;
				double percentdone = (filesComplete) / (MAX_ITERATIONS * (usableFileArr.length * percentTraining));
				System.out.printf("percent coverage: %f -- %f done\n", percent, percentdone);
				
				if (percent < PERCENT_COVERAGE)
					continue;
				
				double[][][] input_netCDF_grk = DataIO.getDataFromNetCDF(voronoi_grk, file_grk, coordinateMap_GRK);
				double[][][] input_netCDF_ewx = DataIO.getDataFromNetCDF(voronoi_ewx, file_ewx, coordinateMap_EWX);
				// irrelevant which one is chosen
				double[][][] input_voronoi_rain = DataIO.getRainDataFromMaps(input_voronoi_grk, file_grk, maps).first();
				double[][][] input_region = new double[input_netCDF_grk.length][input_netCDF_grk[0].length][3];
				
				double[][][] input_single = new double[1][1][3];
				double[][][] output_single = new double[1][1][1];
				for (int r = 0; r < input_region.length; r++)
					for (int c = 0; c < input_region[0].length; c++)
					{
						input_region[r][c][0] = input_netCDF_grk[r][c][0];
						input_region[r][c][1] = input_netCDF_ewx[r][c][0];
						input_region[r][c][2] = input_voronoi_rain[r][c][0];
						
						
						// Train the single value network too.
						input_single[0][0][0] = input_region[r][c][0];
						input_single[0][0][1] = input_region[r][c][1];
						input_single[0][0][2] = input_region[r][c][2];
						output_single[0][0][0] = output_region[r][c][0];
						n_one.train(input_single, output_single);
					}

				n.train(input_region, output_region);
			}
		}
		
		System.out.println("Calculating error...");
		double[] training = new double[NUM_THINGS];
		double[] training_single = new double[NUM_THINGS];
		double[] testing = new double[NUM_THINGS];
		double[] testing_single = new double[NUM_THINGS];
		
		
		
		int trainingFiles = 0;
		int testingFiles = 0;

		// TRAINING
		for (int i = 0; i < usableFileArr.length; i+=2)
		{
			String filename_ewx = usableFileArr[i];
			File file_ewx = new File(dopplerDir, filename_ewx);
			String filename_grk = usableFileArr[i+1];
			File file_grk = new File(dopplerDir, filename_grk);
			
			boolean isTest = false;

			if (testCases.contains(filename_ewx) || testCases.contains(filename_grk))
				isTest = true;
			if (!isTest && Math.random() < 0.75)
				continue;
			
			Tuple<double[][][], Double> output_percent = DataIO.getRainDataFromMaps(voronoi_grk, file_grk, maps);
			double percent = output_percent.second();
			
			if (percent < PERCENT_COVERAGE)
				continue;
			
			double[][][] input_netCDF_grk = DataIO.getDataFromNetCDF(voronoi_grk, file_grk, coordinateMap_GRK);
			double[][][] input_netCDF_ewx = DataIO.getDataFromNetCDF(voronoi_ewx, file_ewx, coordinateMap_EWX);
			// irrelevant which one is chosen
			double[][][] input_voronoi_rain = DataIO.getRainDataFromMaps(input_voronoi_grk, file_grk, maps).first();
			double[][][] input_region = new double[input_netCDF_grk.length][input_netCDF_grk[0].length][3];
			
			
			
			double[][][] expected_output = output_percent.first();
			double[][][] output_single = new double[input_netCDF_grk.length][input_netCDF_grk[0].length][1];
			
			for (int r = 0; r < input_region.length; r++)
				for (int c = 0; c < input_region[0].length; c++)
				{
					input_region[r][c][0] = input_netCDF_grk[r][c][0];
					input_region[r][c][1] = input_netCDF_ewx[r][c][0];
					input_region[r][c][2] = input_voronoi_rain[r][c][0];
					
					
					// Train the single value network too.
					double[][][] input_single = new double[1][1][3];
					input_single[0][0][0] = input_region[r][c][0];
					input_single[0][0][1] = input_region[r][c][1];
					input_single[0][0][2] = input_region[r][c][2];
					n_one.processInput(input_single);
					output_single[r][c][0] = n_one.getOutput()[0][0][0];
				}


			n.processInput(input_region);
			double[][][] output_region = n.getOutput();
			
			if (isTest)
			{
				calculateErrors(testing, testing_single, output_region, output_single, expected_output, n.getGraph());
				testingFiles++;
			}
			else
			{
				calculateErrors(training, training_single, output_region, output_single, expected_output, n.getGraph());
				trainingFiles++;
			}
			System.out.println("error percent done: " + (i + 1.0) / usableFileArr.length);
		}
		
		
		String tn = "";
		for (int i = 0; i < NUM_THINGS; i+= 2)
		{
			int N = trainingFiles;
			double avg = training[i] / N;
			double var = training[i+1] / N - avg * avg;
			tn += String.format("%.8f, %.8f, ", avg,  Math.sqrt(var));
		}
		for (int i = 0; i < NUM_THINGS; i+= 2)
		{
			int N = trainingFiles;
			double avg = training_single[i] / N;
			double var = training_single[i+1] / N - avg * avg;
			tn += String.format("%.8f, %.8f, ", avg,  Math.sqrt(var));
		}
		for (int i = 0; i < NUM_THINGS; i+= 2)
		{
			int N = testingFiles;
			double avg = testing[i] / N;
			double var = testing[i+1] / N - avg * avg;
			tn += String.format("%.8f, %.8f, ", avg,  Math.sqrt(var));
		}
		for (int i = 0; i < NUM_THINGS; i+= 2)
		{
			int N = testingFiles;
			double avg = testing_single[i] / N;
			double var = testing_single[i+1] / N - avg * avg;
			tn += String.format("%.8f, %.8f, ", avg,  Math.sqrt(var));
		}
		System.out.printf("config: %s\n", tn);	
		
		n.close();
		n_one.close();
		
	}
	

	
	
	static void calculateErrors(double[] region, double[] single, double[][][] output_region, double[][][] output_single, double[][][] expected_output, final NetworkGraph g) throws InterruptedException, ExecutionException
	{
		double[] vals = ErrorCalculator.mses(output_region, output_single, expected_output);
		region[MSE] += vals[0];
		region[MSE_2] += vals[0] * vals[0];
		
		region[MSE_S] += vals[1];
		region[MSE_S_2] += vals[1] * vals[1];
		
		single[MSE] += vals[2];
		single[MSE_2] += vals[2] * vals[2];
		
		single[MSE_S] += vals[3];
		single[MSE_S_2] += vals[3] * vals[3];	
		
		
//		
//		// 0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0 (inches / 5.16).
//		final double[][][] single_cdf_input = new double[output_single.length][output_single[0].length][9];
//		final double[][][] region_cdf_input = new double[output_single.length][output_single[0].length][9];
//		
//		for (int r = 0; r < single_cdf_input.length; r++)
//			for (int c = 0; c < single_cdf_input[0].length; c++)
//			{
//				double single_index = output_single[r][c][0] * 8;
//				double normal_index = output_region[r][c][0] * 8;
//				
//				int low_s = (int)single_index;
//				int low_n = (int)normal_index;
//				
//				single_cdf_input[r][c][low_s] = 1 - (single_index - low_s);
//				if (low_s < 8)
//					single_cdf_input[r][c][low_s + 1] = single_index - low_s;
//				
//				region_cdf_input[r][c][low_n] = 1 - (normal_index - low_n);
//				if (low_n < 8)
//					region_cdf_input[r][c][low_n + 1] = normal_index - low_n;
//			}
//		ExecutorService threadPool = Executors.newFixedThreadPool(8);
//		List<Callable<double[][][]>> callables = new ArrayList<Callable<double[][][]>>();
//
//		for (int i = 0; i < fns.length; i++)
//		{
//			final PairwiseFunction fn = fns[i];
//			callables.add(new Callable<double[][][]>(){
//				@Override
//				public double[][][] call() throws Exception {
//					return LoopyBP.infer(region_cdf_input, 4, fn, g);
//				}});
//			callables.add(new Callable<double[][][]>(){
//				@Override
//				public double[][][] call() throws Exception {
//					return LoopyBP.infer(single_cdf_input, 4, fn, g);
//				}});
//		}
//
//		System.out.println("Submitting LoopyBP jobs...");
//		List<Future<double[][][]>> futures = threadPool.invokeAll(callables);		
//		System.out.println("LoopyBP done");
//		for (int i = 0; i < fns.length; i++)
//		{			
//			int startIndex = 4 + 4 * i;
//			double[][][] loopy_region = futures.get(2 * i).get();
//			double[][][] loopy_single = futures.get(2 * i + 1).get();
//			// need to update values......
//			double[][][] region_output_pred = new double[loopy_region.length][loopy_region[0].length][1];
//			double[][][] single_output_pred = new double[loopy_region.length][loopy_region[0].length][1];
//			for (int r = 0; r < loopy_region.length; r++)
//				for (int c = 0; c < loopy_region[0].length; c++)
//				{
//					double regionVal = 0;
//					double singleVal = 0;
//					for (int k = 0; k < loopy_region[0][0].length; k++)
//					{
//						regionVal += loopy_region[r][c][k] * k / 8.0;
//						singleVal += loopy_single[r][c][k] * k / 8.0;
//					}
//					region_output_pred[r][c][0] = regionVal;
//					single_output_pred[r][c][0] = singleVal;
//				}
//			
//			vals = ErrorCalculator.mses(region_output_pred, single_output_pred, expected_output);
//			region[startIndex] += vals[0];
//			region[startIndex+1] += vals[0] * vals[0];
//			
//			region[startIndex+2] += vals[1];
//			region[startIndex+3] += vals[1] * vals[1];
//			
//			single[startIndex] += vals[2];
//			single[startIndex+1] += vals[2] * vals[2];
//			
//			single[startIndex+2] += vals[3];
//			single[startIndex+3] += vals[3] * vals[3];	
//		}
//		threadPool.shutdownNow();
	}
	
	static PairwiseFunction[] fns = {
		new PairwiseFunction(){
			@Override
			public double prob(int rA, int cA, int kA, int rB, int cB, int kB) {
				double A = .5;
				double norm = 1 + Math.exp(-A);
				return kA == kB ? 1.0 / norm : Math.exp(-A)
						/ norm;
			}},
		new PairwiseFunction() {
				@Override
				public double prob(int rA, int cA, int kA, int rB,
						int cB, int kB) {
					double A = 2;
					double norm = 1 + Math.exp(-A);
					return kA == kB ? 1.0 / norm : Math.exp(-A)
							/ norm;
				}
			},
			new PairwiseFunction() {
				@Override
				public double prob(int rA, int cA, int kA, int rB,
						int cB, int kB) {
					double strength = Math.abs(kA - kB)+1;
					return 1.0 / Math.sqrt(strength);
				}
			},
			new PairwiseFunction() {
				@Override
				public double prob(int rA, int cA, int kA, int rB,
						int cB, int kB) {
					double strength = Math.abs(kA - kB)+1;
					return 1.0 / Math.pow(strength, 2.0);
				}
			},
	};
}
