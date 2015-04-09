package weather.scripts;

import java.io.File;

import weather.data.Constants;
import weather.util.CoordinateConversion;
import weather.util.Point;
import weather.util.Serializer;
import weather.util.Tuple;

public class CreateLatLonMap {

	public static void main(String[] args) throws Throwable{
		// GRK 360, 115
		// EWX 360, 115
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		String[] radarCodes = {"GRK","EWX"};
		int[][] neighbors = {{0,0},{0,-1},{0,1},{-1,0},{1,0},{-1,-1},{-1,1},{1,-1},{1,1},{0,2},{2,0},{-2,0},{0,-2},{1,2},{2,1},{-2,1},{1,-2},{-1,2},{2,-1},{-2,-1},{-1,-2}};
		for (String radarCode : radarCodes)
		{
			double start_lat = radarCode.equals("GRK") ? Constants.GRK_CENTER_LAT : Constants.EWX_CENTER_LAT;
			double start_lon = radarCode.equals("GRK") ? Constants.GRK_CENTER_LON : Constants.EWX_CENTER_LON;
			double scale = radarCode.equals("GRK") ? Constants.GRK_SCALE : Constants.EWX_SCALE;
			int height = radarCode.equals("GRK") ? Constants.GRK_HEIGHT : Constants.EWX_HEIGHT;
			int width = radarCode.equals("GRK") ? Constants.GRK_WIDTH : Constants.EWX_WIDTH;

			Point[][] conversion = new Point[height][width];
			
			for (int theta = 0; theta < 359; theta++)
				for (int dist = 0; dist < 115; dist++)
				{
					Tuple<Double, Double> dest = CoordinateConversion.startDistBearingToLatLong(start_lat, start_lon, dist, theta);
					double lat = dest.first();
					double lon = dest.second();
					
					int lat_index = -(int)Math.round((lat - Constants.BOUND_N) / scale);
					int lon_index = (int)Math.round((lon - Constants.BOUND_W) / scale);
					for (int[] neighbor : neighbors)
					{
						int aidx = lat_index + neighbor[0];
						int oidx = lon_index + neighbor[1];
						if (aidx >= 0 && aidx < height && oidx >= 0 && oidx < width)
						{
							conversion[aidx][oidx] = new Point(theta, dist);
//							if (radarCode.equals("EWX"))
//								System.out.printf("%d %d -> %d %d\n", aidx, oidx, theta, dist);
						}
					}
				}
			Serializer.writeCoordinateConversion(conversion, dir, radarCode + "_COORDINATE");
		}
	}

}
