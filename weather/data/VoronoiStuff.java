package weather.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import weather.util.Sensor;
import weather.util.Serializer;

public class VoronoiStuff {

	public static void main(String[] args) throws Throwable{
		// TODO Auto-generated method stub
		int[][] grk = Serializer.readVoronoi(new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\"), "GRK");
		File sensorFile = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\LCRA\\LCRASiteInfo.csv");
		Random rand = new Random();
		final long randSeed = 14565415141784562L;
		rand.setSeed(randSeed);
		List<Sensor> list = new ArrayList<>();
		
		// Read in all available sensors and associated rain data.
		CSVParser p = CSVParser.parse(sensorFile, Charset.defaultCharset(), 
				CSVFormat.DEFAULT);
		boolean seenHeader = false;
		for (CSVRecord r : p)
		{
			if (!seenHeader)
			{
				seenHeader = true;
				continue;
			}
			String name = r.get(0);
			int id = Integer.parseInt(r.get(1));
			String lat = r.get(2);
			String lon = r.get(3);
			double dlat = 0;
			double dlon = 0;
			try {
				dlat = Double.parseDouble(lat);
				dlon = Double.parseDouble(lon);
			}
			catch(Exception e)
			{
				continue;
			}
			
			list.add(new Sensor(name, id, dlat, dlon));

		}
		p.close();
		// Hacked way to create the array.
		Sensor[] sensorArr = list.toArray(new Sensor[0]);
		
		int[] colors = new int[sensorArr.length];
		for (int i = 0; i < colors.length; i++)
		{
			int r = rand.nextInt(256);
			int g = rand.nextInt(256);
			int b = rand.nextInt(256);
			int color = 0xff000000 + (r << 16) + (g << 8) + b;
			colors[i] = color;
		}
		
		
		
		// So connections[index].size() is number of connected regions. 

		int[][] data = grk;
		BufferedImage img = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_ARGB);
		int size = 125;
		int rstart = 50;
		int cstart = 200;
		Set<String> sensorsToInclude = new TreeSet<>();
		for (int r = rstart; r < rstart + size; r++) {
			for (int c = cstart; c < cstart + size; c++) {
					img.setRGB(r, c, colors[data[r][c] % colors.length]);
					sensorsToInclude.add(sensorArr[data[r][c]].name);
			}
		}
		ImageIO.write(img, "gif", new File("C:/Users/Evan/GitProjects/weather-prediction/data2/vConnections_test.gif"));
		for (String s : sensorsToInclude)
			System.out.println(s);
		System.out.println(sensorsToInclude.size());
	}


}
