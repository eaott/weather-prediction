package weather.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import static weather.data.Constants.*;
import weather.network.Label;
import weather.network.SimpleNetwork;
import weather.util.DataIO;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

import java.util.Map.Entry;

public class CreateExperiment {
	public static void main(String[] args) throws Throwable {
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF_Test");
		final File rainDir = new File(dir, "LCRA");
		
		long startTime = System.currentTimeMillis();
		int[][] grk_voronoi = Serializer.readVoronoi(dir, "GRK");
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		Point[][] coordinateMap = Serializer.readCoordinateConversion(dir, "GRK_COORDINATE");
		System.out.println("Data loaded. " + (System.currentTimeMillis() - startTime));
		System.out.printf("size: (%d, %d)\n", grk_voronoi.length, grk_voronoi[0].length);
		
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
		int HIDDEN = 10;
		double NEIGHBOR_DISTANCE = 3.0;
		double LEARNING_RATE = 0.3;
		int MAX_ITERATIONS = 1;
		
		SimpleNetwork n = SimpleNetwork.naiveLinear(grk_voronoi.length, grk_voronoi[0].length, new Label[]{new Label("Doppler", 0)}, new Label[]{new Label("LCRA", 0)}, HIDDEN, NEIGHBOR_DISTANCE, LEARNING_RATE);
		System.out.println("Network created. " + (System.currentTimeMillis() - startTime));

		// Only use files that have a particular coverage percentage.
		String[] unusableFileArr = Serializer.readFiles(dopplerDir, "UNUSABLEFILES");
		Set<String> unusableFiles = new HashSet<>();
		for (String s : unusableFileArr)
			unusableFiles.add(s);
		
		File[] allFiles = dopplerDir.listFiles();
		System.out.println("Total files: " + (allFiles.length - unusableFileArr.length));
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		double filesComplete = 0;
		double usedFiles = 0;
		long trainingTimeTotal = 0;
		long startOfAllIters = System.currentTimeMillis();
		
		for (int ITER = 0; ITER < MAX_ITERATIONS; ITER++)
		{
			for (File file : allFiles)
			{
				if (unusableFiles.contains(file.getName()))
					continue;
				if (file.getName().contains("USABLE") || file.getName().contains("testNetwork"))
					continue;
				Tuple<double[][][], Double> output_percent = DataIO.getOutput(grk_voronoi, file, maps);
				double[][][] output_region = output_percent.first();
				double percent = output_percent.second();
				
				filesComplete++;
				double percentdone = (filesComplete) / (MAX_ITERATIONS * (allFiles.length - unusableFileArr.length));
				System.out.printf("percent coverage: %f -- %f done\n", percent, percentdone);
				
				if (percent < PERCENT_COVERAGE)
					continue;
				if (percentdone > 0.01)
					System.out.println(System.currentTimeMillis() - startOfAllIters);
				
				double[][][] input_region = DataIO.getInput(grk_voronoi, file, coordinateMap);

				long trainStart = System.currentTimeMillis();
				n.train(input_region, output_region);
				trainingTimeTotal += System.currentTimeMillis() - trainStart;
				usedFiles++;
			}
		}
		
		// FIXME actually measure the MSE
		
		System.out.println("Processed. " + (System.currentTimeMillis() - startTime));
		System.out.printf("Average time for training: %fms\n", trainingTimeTotal / usedFiles);
		
		Serializer.writeNetwork(n, dopplerDir, "testNetwork");
		
		n.close();
	}
}
