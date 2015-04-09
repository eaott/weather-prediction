package weather.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import static weather.data.Constants.*;
import weather.util.Sensor;
import weather.util.Serializer;

public class CreateFileList {

	public static void main(String[] args) throws Throwable{
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF");
		final File rainDir = new File(dir, "LCRA");
		
		long startTime = System.currentTimeMillis();
		int[][] ewx_voronoi = Serializer.readVoronoi(dir, "EWX");
		int[][] grk_voronoi = Serializer.readVoronoi(dir, "GRK");
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		System.out.println("Data loaded. " + (System.currentTimeMillis() - startTime));
		
		
		// HIDDEN = 10 and NEIGHBOR_DISTANCE = 5.0 seem reasonable for memory purposes
		
		File[] dopplerFiles = dopplerDir.listFiles();
		System.out.println("Total files: " + dopplerFiles.length);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");

		List<String> usedFiles = new ArrayList<>();
		double index = 0;
		for (File file : dopplerFiles)
		{
			if (file.getName().contains("USABLEFILES"))
				continue;
			String radarCode = file.getName().substring(3, 6);
			int[][] voronoi = radarCode.equals("EWX") ? ewx_voronoi : grk_voronoi;
			double[][][] output_region = new double[voronoi.length][voronoi[0].length][1];
			
			// Prune data by output first...
			String datestring = file.getName().substring(7);
			Date date = format.parse(datestring);
			long t = date.getTime();
			
			double count = 0;
			for (int r = 0; r < voronoi.length; r++)
			{
				for (int c = 0; c < voronoi[0].length; c++)
				{
					int bestSensorIndex = voronoi[r][c];
					TreeMap<Long, Double> map = (TreeMap<Long,Double>)maps[bestSensorIndex];
					Entry<Long, Double> low = map.floorEntry(t);
					Entry<Long, Double> high = map.ceilingEntry(t);
					Entry<Long, Double> best = null;
					if (low == null)
						best = high;
					else if (high == null)
						best = low;
					else
						best = (Math.abs(low.getKey() - t) < Math.abs(high.getKey()) - t) ? low : high;
					if (best != null && Math.abs(best.getKey() - t) < ALLOWED_TIME)
					{
						Calendar cal = new GregorianCalendar();
						cal.setTimeInMillis(t);
						if (cal.get(Calendar.YEAR) < 2010)
						{
							System.err.println("WHAT THE DEUCE " +  file.getAbsolutePath());
						}
						output_region[r][c][0] = best.getValue();
						count++;
					}
				}
			}
			System.out.printf("%f complete\n", (++index) / dopplerFiles.length);
			// If not enough rain sensors have data, don't bother training.
			if (count / (voronoi.length * voronoi[0].length) >= PERCENT_COVERAGE)
					continue;
			usedFiles.add(file.getName());
		}
		
		Serializer.writeFiles(usedFiles.toArray(new String[0]), dopplerDir, "UNUSABLEFILES");
	}

}
