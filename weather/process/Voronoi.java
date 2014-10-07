package weather.process;

import weather.util.DistanceFunction;
import weather.util.Point;
import weather.util.Sensor;

public class Voronoi {
	public static final double TOLERANCE = 0.0001;
	/**
	Assumes Euclidean distance on the whole map. Too difficult to define the bisector line otherwise.
	 * @param debug TODO
	*/
	public static int[][] generateMapSlow(int height, int width,
			Point[] sensors, DistanceFunction d, boolean debug)
	{
		int[][] result = new int[height][width];
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				result[i][j] = -1;
			}
		}
		// for (int i = 0; i < sensors.length; i++)
		// {
		// 	boolean[][] isB = new boolean[width][height];
		// 	for (int j = 0; j < sensors.length; j++)
		// 	{
		// 		if (i == j)
		// 			continue;
		// 		Sensor sensorA = sensors[i];
		// 		Sensor sensorB = sensors[j];
		// 		if (Math.abs(sensorA.y - sensorB.y) < TOLERANCE)
		// 		{
		// 			// In this case, want to just sort on x values.
		// 			if (sensorA.x < sensorB.x)
		// 			{
		// 				for (int r = 0; r < isB.length; r++)
		// 				{
		// 					for (int c = (int)Math.round((sensorA.x + sensorB.x)/2); c < isB[0].length; c++)
		// 					{
		// 						isB[r][c] = true;
		// 					}
		// 				}
		// 			}
		// 			else // sensorA.x > sensorB.x
		// 			{
		// 				for (int r = 0; r < isB.length; r++)
		// 				{
		// 					for (int c = 0; c < (int)Math.round((sensorA.x + sensorB.x)/2); c++)
		// 					{
		// 						isB[r][c] = true;
		// 					}
		// 				}
		// 			}
		// 		}
		// 		else
		// 		{
		// 			// In this case, find the bisecting line, then see if sign of difference
		// 			// from line prediction at each point is the same as A.
		// 			double x_mid = (sensorA.x + sensorB.x) / 2;
		// 			double y_mid = (sensorA.y + sensorB.y) / 2;
		// 			double slope = (sensorB.y - sensorA.y) / (sensorB.x - sensorA.x);
		// 			double perpM = -1/slope;
		// 			double inter = -perpM * x_mid + y_mid;
		// 			boolean aLow =  sensorA.y < (perpM * sensorA.x + inter);
		// 			System.out.printf("%d, %d: %b\n", i, j, aLow);
		// 			for (int r = 0; r < isB.length; r++)
		// 			{
		// 				for (int c = 0; c < isB[0].length; c++)
		// 				{
		// 					if (aLow && r >= (perpM * c + inter))
		// 					{
		// 						/*
		// 						----------------------
		// 						|       \  T
		// 						|     A  \  B
		// 						|         \
		// 						|          \
		// 						-----------------------
		// 						*/
		// 						isB[r][c] = true;
		// 					}
		// 					else if (!aLow && r < (perpM * c + inter))
		// 					{
		// 						/*
		// 						----------------------
		// 						|          / T
		// 						|     A   / B
		// 						|        / 
		// 						|       /   
		// 						-----------------------
		// 						*/
		// 						isB[r][c] = true;
		// 					}
		// 				}
		// 			}
		// 		}
		// 	}
		// 	// Anything that hasn't been marked AND is not marked as OTHER
		// 	// is then the region for sensorA (index i).
		// 	for (int r = 0; r < isB.length; r++)
		// 	{
		// 		for (int c = 0; c < isB[0].length; c++)
		// 		{
		// 			if (!isB[r][c] && result[r][c] == -1)
		// 				result[r][c] = i;
		// 		}
		// 	}
		// }


		// This is the slow, dumb way. But it works.
		for (int r = 0; r < height; r++)
		{
			for (int c = 0; c < width; c++)
			{
				if (result[r][c] == -1)
				{
					int minIndex = 0;
					for (int i = 1; i < sensors.length; i++)
					{
						Point min = sensors[minIndex];
						Point cur = sensors[i];
						if (d.dist(min.getR(), min.getC(), r, c) > d.dist(cur.getR(), cur.getC(), r, c))
							minIndex = i;
					}
					result[r][c] = minIndex;
				}
			}
			System.out.printf("Voronoi: %f%% complete.\n", (r + 0.0) / height * 100);
		}
		return result;
	}
}
