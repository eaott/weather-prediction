package weather.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static weather.data.Constants.*;
import weather.data.Constants;
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

public class FindDopplerErrors {

	public static void main(String[] args) throws Throwable {
		long startTime = System.currentTimeMillis();
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File rainDir = new File(dir, "LCRA");
		final File dopplerDir = new File(
				"C:\\Users\\Evan\\Desktop\\Thesis_NetCDF");


		int[][] voronoi_grk = Serializer.readVoronoi(dir, "GRK");
		int[][] voronoi_ewx = Serializer.readVoronoi(dir, "EWX");
		Point[][] coordinateMap_grk = Serializer.readCoordinateConversion(dir,
				"GRK_COORDINATE");
		Point[][] coordinateMap_ewx = Serializer.readCoordinateConversion(dir,
				"EWX_COORDINATE");

		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		System.out.println("Data loaded. " + (System.currentTimeMillis() - startTime));
		System.out.printf("size: (%d, %d)\n", voronoi_grk.length, voronoi_grk[0].length);

		System.out.println("Network created. "
				+ (System.currentTimeMillis() - startTime));

		// Only use files that have a particular coverage percentage.
		String[] usableFileArr = Serializer
				.readFiles(dopplerDir, "NONZEROFILES");

		System.out.println("Total files: " + (usableFileArr.length));

		double filesComplete = 0;
		double usedFiles = 0;

		double dopplerMse = 0;
		double dopplerMseSecond = 0;
		
		int numNoZero = 0;

		double dopplerStrength = 0;
		double dopplerStrengthSecond = 0;
		
		for (String filename : usableFileArr) {
			if (filename.contains("EWX"))
				continue;
			if (Math.random() < .8)
				continue;
			File file = new File(dopplerDir, filename);
			Tuple<double[][][], Double> output_percent = DataIO
					.getRainDataFromMaps(voronoi_grk, file, maps);
			double[][][] output_region = output_percent.first();
			double percent = output_percent.second();

			filesComplete++;
			double percentdone = (filesComplete) / ((usableFileArr.length));
			System.out.printf("percent coverage: %f -- %f done\n", percent,
					percentdone);

			if (percent < PERCENT_COVERAGE)
				continue;

			double[][][] input_netCDF_grk = DataIO.getDataFromNetCDF(voronoi_grk, file, coordinateMap_grk);
			double[][][] input_netCDF_ewx = DataIO.getDataFromNetCDF(voronoi_ewx, file, coordinateMap_ewx);
			
			double[][][] input_average = new double[input_netCDF_grk.length][input_netCDF_grk[0].length][1];
			for (int r = 0; r< input_average.length; r++)
				for (int c = 0; c < input_average.length; c++)
					input_average[r][c][0] = (input_netCDF_grk[r][c][0] + input_netCDF_ewx[r][c][0]) / 2.0;

			usedFiles++;

			double mse = ErrorCalculator.mse(input_average, output_region);
			double strength = ErrorCalculator.mse_square_strength(input_average, output_region);

			
			dopplerMse += mse;
			dopplerMseSecond += mse * mse;
			
			dopplerStrength += strength;
			dopplerStrengthSecond += strength * strength;
		}

		double meanDoppler = dopplerMse / usedFiles;
		double varDoppler = dopplerMseSecond / usedFiles - meanDoppler
				* meanDoppler;
		double stddevDoppler = Math.sqrt(varDoppler);
		System.out.printf("DOPPLER MSE: %.8f+/-%.8f\n", meanDoppler,
				stddevDoppler);
		
		
		double meanDopplerStrength = dopplerStrength / usedFiles;
		double varDopplerStrength = dopplerStrengthSecond / usedFiles - meanDopplerStrength
				* meanDopplerStrength;
		double stddevDopplerStrength = Math.sqrt(varDopplerStrength);
		System.out.printf("DOPPLER STRENGTH MSE: %.8f+/-%.8f\n", meanDopplerStrength,
				stddevDopplerStrength);
		
		System.out.println(usedFiles + " " + numNoZero);
	}

}
