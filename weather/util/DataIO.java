package weather.util;

import static weather.data.Constants.ALLOWED_TIME;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import weather.network.Label;

public class DataIO {
	static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
	/**
	 * Identifies the labels in the directory.
	 * 
	 * Map produced is from
	 * Map is from color value to index in the array.
	 * @param dirIn
	 * @param filter
	 * @return
	 */
	public static Tuple<Label[], Map<Integer, Integer>> getLabels(File dirIn,
			FilenameFilter filter) {
		String[] files = filter == null ? dirIn.list() : dirIn.list(filter);
		Arrays.sort(files, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		});

		Set<Integer> labels = new HashSet<>();
		for (String f : files) {
			File file = new File(dirIn, f);
			BufferedImage img;
			try {
				img = ImageIO.read(file);
			} catch (IOException e) {
				return null;
			}
			for (int x = img.getMinX(); x < img.getWidth(); x++)
				for (int y = img.getMinY(); y < img.getHeight(); y++) {
					labels.add(img.getRGB(x, y));
				}
		}
		Label[] labelArr = new Label[labels.size()];
		Map<Integer, Integer> map = new HashMap<>();
		int index = 0;
		for (Integer l : labels) {
			map.put(l.intValue(), index);
			labelArr[index++] = new Label(index + "", l.intValue());
		}
		return new Tuple<>(labelArr, map);
	}
	
	public static Tuple<double[][][], Double> getRainDataFromMaps(int[][] grk_voronoi, File file, Map[] maps) throws Throwable
	{
		double[][][] output_region = new double[grk_voronoi.length][grk_voronoi[0].length][1];
		
		// Prune data by output first...
		String datestring = file.getName().substring(7);
		Date date = format.parse(datestring);
		long t = date.getTime();
		
		double count = 0;
		for (int r = 0; r < grk_voronoi.length; r++)
		{
			for (int c = 0; c < grk_voronoi[0].length; c++)
			{
				int bestSensorIndex = grk_voronoi[r][c];
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
					output_region[r][c][0] = best.getValue();
					count++;
				}
			}
		}
		return new Tuple<>(output_region, count / (grk_voronoi.length * grk_voronoi[0].length));
	}
	
	public static double[][][] getDataFromNetCDF(int[][] grk_voronoi, File file, Point[][] coordinateMap) throws Throwable
	{
		double[][][] input_region = new double[grk_voronoi.length][grk_voronoi[0].length][1];
		NetcdfFile ncfile = NetcdfFile.open(file.getAbsolutePath());
		Variable var_precip = ncfile.findVariable("Precip1hr");
		Array arr = var_precip.read();
		float[][] data = (float[][])arr.copyToNDJavaArray();
		for (int r = 0; r < input_region.length; r++)
			for (int c = 0; c < input_region[0].length; c++)
			{
				int theta = coordinateMap[r][c].getR();
				int dist = coordinateMap[r][c].getC();
				input_region[r][c][0] = data[theta][dist];
				if (Double.isNaN(input_region[r][c][0]))
				{
					input_region[r][c][0] = 0;
				}
			}
		return input_region;
	}

	@Deprecated
	public static double[][][] getData(File file, Map<Integer, Integer> labels) {
		BufferedImage img;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			return null;
		}
		double[][][] results = new double[img.getWidth()][img.getHeight()][labels
				.size()];
		for (int x = img.getMinX(); x < img.getWidth(); x++)
			for (int y = img.getMinY(); y < img.getHeight(); y++) {
				int color = img.getRGB(x, y);
				if (!labels.containsKey(color)) {
					System.out.printf("%x %d %d\n", color, x, y);
				}
				int curLabel = labels.get(color);
				results[x][y][curLabel] = 1;
			}
		return results;
	}


}
