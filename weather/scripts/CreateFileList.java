package weather.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import static weather.data.Constants.*;
import weather.util.DataIO;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

public class CreateFileList {

	public static void main(String[] args) throws Throwable{
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF");
		final File rainDir = new File(dir, "LCRA");
		
		long startTime = System.currentTimeMillis();
		int[][] ewx_voronoi = Serializer.readVoronoi(dir, "EWX");
		Point[][] coordinateMap_GRK = Serializer.readCoordinateConversion(dir, "GRK_COORDINATE");
		int[][] grk_voronoi = Serializer.readVoronoi(dir, "GRK");
		Point[][] coordinateMap_EWX = Serializer.readCoordinateConversion(dir, "EWX_COORDINATE");
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		System.out.println("Data loaded. " + (System.currentTimeMillis() - startTime));
		
		
		// HIDDEN = 10 and NEIGHBOR_DISTANCE = 5.0 seem reasonable for memory purposes
		
		File[] dopplerFiles = dopplerDir.listFiles();
		System.out.println("Total files: " + dopplerFiles.length);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		
		TreeMap<Long,String> sortedEWX = new TreeMap<Long,String>();
		TreeMap<Long,String> sortedGRK = new TreeMap<Long,String>();
		for (File f : dopplerFiles)
		{
			if (f.getName().contains(".ser"))
				continue;
			// Prune data by output first...
			String datestring = f.getName().substring(7);
			Date date = format.parse(datestring);
			long t = date.getTime();
			if (f.getName().contains("EWX"))
			{
				sortedEWX.put(t,f.getName());
			}
			else{
				sortedGRK.put(t, f.getName());
			}
		}
		System.out.println("sorted.");
		List<String> potentialFiles = new ArrayList<>();
		for (Long t : sortedEWX.keySet())
		{
			Entry<Long, String> low = sortedGRK.floorEntry(t);
			Entry<Long, String> high = sortedGRK.ceilingEntry(t);
			Entry<Long, String> best = null;
			if (low == null)
				best = high;
			else if (high == null)
				best = low;
			else
				best = (Math.abs(low.getKey() - t) < Math.abs(high.getKey()) - t) ? low : high;
			
			if (Math.abs(best.getKey() - t) < ALLOWED_TIME)
			{
				potentialFiles.add(sortedEWX.get(t));
				potentialFiles.add(sortedGRK.get(best.getKey()));
			}
		}

		System.out.println("trimmed");
		
		List<String> usedFiles = new ArrayList<>();
		for (int i = 0; i < potentialFiles.size(); i+=2)
		{
			String filename_ewx = potentialFiles.get(i);
			File file_ewx = new File(dopplerDir, filename_ewx);
			String filename_grk = potentialFiles.get(i + 1);
			File file_grk = new File(dopplerDir, filename_grk);
			Tuple<double[][][], Double> output = DataIO.getRainDataFromMaps(ewx_voronoi, file_ewx, maps);
			
			if (output.second() < PERCENT_COVERAGE) {
				continue;
			}
				

			double[][][] input_grk = DataIO.getDataFromNetCDF(grk_voronoi, file_grk, coordinateMap_GRK);
			double[][][] input_ewx = DataIO.getDataFromNetCDF(ewx_voronoi, file_ewx, coordinateMap_EWX);
			
			
			double mse1 = ErrorCalculator.mse_no_zero(output.first(), input_grk);
			double mse2 = ErrorCalculator.mse_no_zero(output.first(), input_ewx);
			
			if (mse1 == -1 || mse2 == -1)
				continue;
			
			
			usedFiles.add(file_ewx.getName());
			usedFiles.add(file_grk.getName());
		}
		
		Serializer.writeFiles(usedFiles.toArray(new String[0]), dopplerDir, "NONZEROFILES");
		System.out.println("Discovered " + usedFiles.size() + " nonzero files.");
	}

}
