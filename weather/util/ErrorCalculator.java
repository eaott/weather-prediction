package weather.util;

import static weather.data.Constants.ALLOWED_TIME;
import static weather.data.Constants.PERCENT_COVERAGE;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import weather.network.SimpleNetwork;

public class ErrorCalculator {
	public static double mse(File[] allFiles, Set<String> unusableFiles, SimpleNetwork n, int[][] grk_voronoi, Point[][] coordinateMap, Map[] maps) throws Throwable
	{
		double errorTotal = 0;
		int totalFiles = 0;
		
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File rainDir = new File(dir, "LCRA");
		long startOfAllIters = System.currentTimeMillis();
		double filesComplete = 0;
		System.out.println("starting");
		for (File file : allFiles)
		{
			if (unusableFiles.contains(file.getName()))
				continue;
			if (file.getName().contains("USABLE") || file.getName().contains("testNetwork"))
				continue;
			System.out.println("got here");
			Tuple<double[][][], Double> output_percent = DataIO.getOutput(grk_voronoi, file, maps);
			double[][][] output_region = output_percent.first();
			double percent = output_percent.second();
			
			filesComplete++;
			double percentdone = (filesComplete) / (allFiles.length - unusableFiles.size());
			if (percent < PERCENT_COVERAGE)
				continue;
			if (percentdone > 0.01)
				System.out.println(System.currentTimeMillis() - startOfAllIters);
			
			double[][][] input_region = DataIO.getInput(grk_voronoi, file, coordinateMap);

			n.processInput(input_region);
			
			double[][][] observed_output = n.getOutput();
			
			double mse = mse(observed_output, output_region, file.getName());
			if (mse < 0 || Double.isNaN(mse) || Double.isInfinite(mse))
			{
				System.out.println(mse + "----" + file.getName());
			}
			else {
				errorTotal += mse;
				totalFiles++;
			}
		}
		if (Double.isNaN(errorTotal) || Double.isInfinite(errorTotal))
		{
			System.out.println(errorTotal + "---- ERROR");
		}
		System.out.println(errorTotal + " " + totalFiles);
		return errorTotal / totalFiles;
	}
	
	public static double mse(double[][][] a, double[][][] b, String name)
	{
		double err = 0;
		for (int r = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++)
				for (int k = 0; k < a[0][0].length; k++)
				{
					double val = a[r][c][k] - b[r][c][k];
					
					if (Double.isNaN(val) || Double.isInfinite(val)
							|| Double.isNaN(val * val) || Double.isInfinite(val * val))
					{
						System.out.println(a[r][c][k] + " " + b[r][c][k] + " "+ name);
						return -1;
					}
					else {
						err += val * val;
					}
				}
		return err / (a.length * a[0].length * a[0][0].length);
	}
}
