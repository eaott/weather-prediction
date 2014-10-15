package weather.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import weather.network.Label;

public class DataIO {
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

	public static void writeData(File f, double[][][] data,
			Map<Integer, Integer> labels) {
		try {
			BufferedImage img = new BufferedImage(data.length, data[0].length,
					BufferedImage.TYPE_INT_ARGB);
			for (int r = 0; r < data.length; r++) {
				for (int c = 0; c < data[0].length; c++) {
					int maxIndex = 0;
					for (int k = 0; k < data[0][0].length; k++) {
						if (data[r][c][k] > data[r][c][maxIndex])
							maxIndex = k;
					}

					img.setRGB(r, c, labels.get(maxIndex));
				}
			}

			ImageIO.write(img, "gif", f);
		} catch (Throwable t) {
		}
	}
}
