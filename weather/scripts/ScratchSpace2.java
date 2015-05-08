package weather.scripts;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import static weather.data.Constants.*;
import weather.network.Label;
import weather.network.SimpleNetwork;
import weather.process.Voronoi;
import weather.util.CoordinateConversion;
import weather.util.DataIO;
import weather.util.DistanceFunction;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

import java.util.Map.Entry;

import javax.imageio.ImageIO;

public class ScratchSpace2 {
	static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
	public static void main(String[] args) throws Throwable {
		int SCALE = 4;
		File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\LCRA");

		Sensor[] sensors = Serializer.readSensors(dir, "SENSORS");
		int[][] voronoi = Serializer.readVoronoi(dir.getParentFile(), "GRK_EXTENDED");
		
		int[][] grk = voronoi;
		int[] distances = new int[sensors.length];
		double maxDistance = 0;
		for (int i = 0; i < sensors.length; i++)
		{
			int rv = (int)Math.round((sensors[i].getLat() - BOUND_N) / (SCALE * GRK_SCALE));
			int cv = (int)(Math.round((sensors[i].getLon() - BOUND_W) / (SCALE * GRK_SCALE)));
			int dist = (int)(Math.round(Math.hypot(rv, cv)));
			distances[i] = dist;
			if (dist > maxDistance)
				maxDistance = dist;
		}

		BufferedImage img = new BufferedImage(GRK_WIDTH * SCALE, GRK_HEIGHT * SCALE, BufferedImage.TYPE_INT_ARGB);
		for (int c = 0; c < grk[0].length; c++) {
			for (int r = 0; r < grk.length; r++) {
				int index = voronoi[r][c];
				int s = (int)(Math.pow(distances[index] / maxDistance, 1) * 255);
				s = Math.max(Math.min((int)(s - 60 + Math.random() * 60), 255),0);
				
				img.setRGB(c, r, 0xff0000ff + (s << 16) + (s << 8));
			}
		}
		
		
		
		
		ImageIO.write(img, "png", new File("C:/Users/Evan/Dropbox/THESIS/images/example_rain_pred.png"));
		
	}
}
