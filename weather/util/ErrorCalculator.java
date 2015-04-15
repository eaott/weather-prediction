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
	public static double mse(String[] usableFiles, Set<String> testCases, SimpleNetwork n, int[][] grk_voronoi, int[][] input_voronoi, Point[][] coordinateMap, Map[] maps, boolean test) throws Throwable
	{
		double errorTotal = 0;
		int totalFiles = 0;
		
		for (String filename : usableFiles)
		{
			if (test && !testCases.contains(filename))
				continue;
			if (!test && testCases.contains(filename))
				continue;
			File file = new File(filename);

			Tuple<double[][][], Double> output_percent = DataIO.getRainDataFromMaps(grk_voronoi, file, maps);
			double[][][] output_region = output_percent.first();
			double percent = output_percent.second();
			
			if (percent < PERCENT_COVERAGE)
				continue;
			
			double[][][] input_netCDF = DataIO.getDataFromNetCDF(grk_voronoi, file, coordinateMap);
			double[][][] input_voronoi_rain = DataIO.getRainDataFromMaps(input_voronoi, file, maps).first();
			double[][][] input_region = new double[input_netCDF.length][input_netCDF[0].length][2];
			for (int r = 0; r < input_region.length; r++)
				for (int c = 0; c < input_region[0].length; c++)
				{
					input_region[r][c][0] = input_netCDF[r][c][0];
					input_region[r][c][1] = input_voronoi_rain[r][c][0];
				}

			n.processInput(input_region);
			
			double[][][] observed_output = n.getOutput();
			
			double mse = mse(observed_output, output_region, file.getName());
			errorTotal += mse;
			totalFiles++;
			System.out.println("Current mse:" + (errorTotal / totalFiles));
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
