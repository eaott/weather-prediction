package weather.util;

import java.awt.image.BufferedImage;
import java.io.File;
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
	public static Tuple<Label[], Map<Integer, Integer>> getLabels(File dirIn)
	{
		String[] files = dirIn.list();
		Arrays.sort(files, new Comparator<String>(){
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}});
		
		Set<Integer> labels = new HashSet<>();
		for (String f : files)
		{
			File file = new File(dirIn, f);
			BufferedImage img;
			try {
				img = ImageIO.read(file);
			} catch (IOException e) {
				return null;
			}
			for (int x = img.getMinX(); x < img.getWidth(); x++)
				for (int y = img.getMinY(); y < img.getHeight(); y++)
				{
					labels.add(img.getRGB(x, y));
				}
		}
		Label[] labelArr = new Label[labels.size()];
		Map<Integer, Integer> map = new HashMap<>();
		int index = 0;
		for (Integer l : labels)
		{
			map.put(l.intValue(), index);
			labelArr[index++] = new Label(index + "", l.intValue());
		}
		return new Tuple<>(labelArr, map);
	}
	
	public static double[][][] getData(File file, Map<Integer, Integer> labels)
	{
		BufferedImage img;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			return null;
		}
		double[][][] results = new double[img.getWidth()][img.getHeight()][labels.size()];
		for (int x = img.getMinX(); x < img.getWidth(); x++)
			for (int y = img.getMinY(); y < img.getHeight(); y++)
			{
				int color = img.getRGB(x, y);
				if (! labels.containsKey(color))
				{
					System.out.printf("%x %d %d\n",color,x,y);
				}
				int curLabel = labels.get(color);
				results[x][y][curLabel] = 1;
			}
		return results;
	}
}
