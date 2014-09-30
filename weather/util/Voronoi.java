package weather.util;

import java.util.TreeSet;

public class Voronoi {
	public static int[][] generateMapSlow(int width, int height,
			TreeSet<Point> sites, DistanceFunction d)
	{
		int[][] result = new int[width][height];
		for (Point site1 : sites)
		{
			for (Point site2 : sites)
			{
				if (site1 == site2)
					continue;
			}
		}
		return result;
	}
}
