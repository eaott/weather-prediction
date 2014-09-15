package weather.network;

import java.util.HashSet;
import java.util.Set;

import weather.util.Point;

public class NetworkGraph {
	private Set<Point>[][] neighbors;
	private double maxDistance;
	private Point[][] network;
	private NetworkGraph(){}
	/**
	 * Guaranteed to include the Point corresponding to (r, c).
	 * @param r
	 * @param c
	 * @return
	 */
	public Set<Point> getNeighbors(int r, int c)
	{
		if (neighbors != null)
			return neighbors[r][c];
		return generate(r, c);
	}
	
	private Set<Point> generate(int r, int c)
	{
		Set<Point> points = new HashSet<>();
		fill(points, r, c, r, c);
//		points.remove(network[r][c]);
		return points;
	}
	
	// FIXME should probably convert to a queue of points instead of recursion
	private void fill(Set<Point> points, int r, int c, int startR, int startC) {
		if (r < 0 || c < 0 || r >= network.length || c >= network[0].length)
			return;
		if (Math.hypot(r - startR, c - startC) > maxDistance)
			return;
		Point nextPoint = network[r][c];
		if (points.contains(nextPoint))
			return;
		points.add(nextPoint);
		fill(points, r + 1, c, startR, startC);
		fill(points, r - 1, c, startR, startC);
		fill(points, r, c + 1, startR, startC);
		fill(points, r, c - 1, startR, startC);
	}


	@SuppressWarnings("unchecked")
	public static NetworkGraph getGraph(Point[][] map, double maxDistance)
	{
		if (maxDistance < 1)
			maxDistance = 1;
		NetworkGraph g = new NetworkGraph();
		g.network = map;
		g.maxDistance = maxDistance;
		g.neighbors = (Set<Point>[][])new Set[map.length][map[0].length];
		for (int r = 0; r < map.length; r++)
		{
			for (int c = 0; c < map[0].length; c++)
			{
				g.neighbors[r][c] = g.generate(r, c);
			}
		}
		
		return g;
	}
}
