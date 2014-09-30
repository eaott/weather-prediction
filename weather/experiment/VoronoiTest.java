package weather.experiment;

import weather.util.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class VoronoiTest
{
	public static void main(String[] args) throws Throwable{
		int ROWS = 400;
		int COLS = 400;
		int MAX_SIZE = 800;
		int SENSORS = 50;
		int ITERS = 10;
		for (int ITER = 0; ITER < ITERS; ITER++)
		{
			Sensor[] sensors = new Sensor[SENSORS];
			for (int i = 0; i < SENSORS; i++)
			{
				double x = Math.random() * COLS;
				double y = Math.random() * ROWS;
				sensors[i] = new Sensor(x, y, i);
			}
			Map<Integer, Integer> map = new HashMap<>();
			for (int i = 0; i < sensors.length; i++)
			{
				int r = (int)(Math.random() * 256);
				int g = (int)(Math.random() * 256);
				int b = (int)(Math.random() * 256);
				int color = 0xff000000 + (r << 16) + (g << 8) + b;
				map.put(i, color);
			}

			int[][] voronoi = Voronoi.generateMapSlow(ROWS, COLS, sensors, DistanceFunction.EUCLIDEAN);
			double[][][] img_small = new double[voronoi.length][voronoi[0].length][sensors.length];
			for (int r = 0; r < img_small.length; r++)
				for (int c = 0; c < img_small[0].length; c++)
					img_small[r][c][voronoi[r][c]] = 1;
			int scale = (MAX_SIZE / Math.max(ROWS, COLS));
			double[][][] img_large = GridManipulations.expand(img_small, scale);
			writeData(new File("data2/voronoi/" + ITER + "euclidean.gif"), img_large, map, sensors, scale);

			voronoi = Voronoi.generateMapSlow(ROWS, COLS, sensors, DistanceFunction.MANHATTAN);
			img_small = new double[voronoi.length][voronoi[0].length][sensors.length];
			for (int r = 0; r < img_small.length; r++)
				for (int c = 0; c < img_small[0].length; c++)
					img_small[r][c][voronoi[r][c]] = 1;
			scale = (MAX_SIZE / Math.max(ROWS, COLS));
			img_large = GridManipulations.expand(img_small, scale);
			writeData(new File("data2/voronoi/" + ITER + "manhattan.gif"), img_large, map, sensors, scale);
			System.out.println("Iteration " + ITER + " complete");
		}
	}

	public static void writeData(File f, double[][][] data, Map<Integer, Integer> labels, 
		Sensor[] sensors, int scale) throws Throwable
	{
		f.mkdirs();
		BufferedImage img = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_ARGB);
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

		// Color in the sensors black.
		int margin = Math.max(data.length, data[0].length) / 100;
		for (Sensor s : sensors)
		{
			for (int r = (int)Math.max(0, scale * s.y - margin); 
				r < Math.min(scale * s.y + margin, data.length); r++)
			{
				for (int c = (int)Math.max(0, scale * s.x - margin); 
					c < Math.min(scale * s.x + margin, data.length); c++)
				{
					img.setRGB(c, r, 0xff000000);
				}
			}
		}


		ImageIO.write(img, "gif", f);
	}
}