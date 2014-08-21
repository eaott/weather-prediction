package weather.network;

import java.util.HashSet;
import java.util.Set;

import weather.util.Point;

public class NetworkGraph {
	private Set<Point>[][] neighbors;
	private double maxDistance;
	private Network network;
	private NetworkGraph(){}
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
		points.remove(network.getLocation(r,c));
		return points;
	}
	
	// FIXME should probably convert to a queue of points instead of recursion
	private void fill(Set<Point> points, int r, int c, int startR, int startC) {
		if (r < 0 || c < 0 || r >= network.numRows() || c >= network.numCols())
			return;
		if (Math.hypot(r - startR, c - startC) > maxDistance)
			return;
		Point nextPoint = network.getLocation(r, c);
		if (points.contains(nextPoint))
			return;
		points.add(nextPoint);
		fill(points, r + 1, c, startR, startC);
		fill(points, r - 1, c, startR, startC);
		fill(points, r, c + 1, startR, startC);
		fill(points, r, c - 1, startR, startC);
	}


	@SuppressWarnings("unchecked")
	public static NetworkGraph getGraph(Network network, double maxDistance)
	{
		if (maxDistance < 1)
			maxDistance = 1;
		NetworkGraph g = new NetworkGraph();
		g.network = network;
		g.maxDistance = maxDistance;
		// If just looking at normal neighbors, no sense in storing all the info.
		if (maxDistance < 2)
		{
			return g;
		}
		g.neighbors = (Set<Point>[][])new Set[network.numRows()][network.numCols()];
		for (int r = 0; r < network.numRows(); r++)
		{
			for (int c = 0; c < network.numCols(); c++)
			{
				g.neighbors[r][c] = g.generate(r, c);
			}
		}
		
		return g;
	}
}
