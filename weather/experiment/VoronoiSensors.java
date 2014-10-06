package weather.experiment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import weather.util.DistanceFunction;
import weather.util.Sensor;
import weather.util.Voronoi;

public class VoronoiSensors {

	public static void main(String[] args) throws Throwable{
		String type = "EWX";
		Random rand = new Random();
		rand.setSeed(14565415141784562L);
		Scanner boundaryIn = new Scanner(new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\" + type + "_N1P_0.gfw"));
		double lonPerPx = boundaryIn.nextDouble();
		boundaryIn.nextDouble(); // rotation
		boundaryIn.nextDouble(); // rotation
		double latPerPx = boundaryIn.nextDouble();
		double startLon = boundaryIn.nextDouble();
		double startLat = boundaryIn.nextDouble();
		boundaryIn.close();
		// FIXME need size of the image...
		int WIDTH = 600;
		int HEIGHT = 550;
		
		List<Sensor> list = new ArrayList<>();
		String sensorFile = "C:\\Users\\Evan\\Dropbox\\Thesis_Data\\HydrometBook2.csv";
		CSVParser p = CSVParser.parse(new File(sensorFile), Charset.defaultCharset(), 
				CSVFormat.DEFAULT);
		for (CSVRecord r : p)
		{
			String name = r.get(0);
			String lat = r.get(5);
			String lon = r.get(6);
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
			System.out.printf("%40s (%f, %f) -> ",name, dlat, dlon);
			// on y axis
			dlat = (dlat - startLat) / latPerPx;
			dlon = (dlon - startLon) / lonPerPx;
			System.out.printf("(%f, %f)\n",dlat, dlon);
			
			list.add(new Sensor(name, dlon, dlat, 0));
		}
		Sensor[] sensorArr = list.toArray(new Sensor[0]);
		System.out.println("generating map");
		int[][] data = Voronoi.generateMapSlow(WIDTH, HEIGHT, sensorArr, DistanceFunction.EUCLIDEAN);
		System.out.println("map generated -- " + data.length + " " + data[0].length);
		int[] colors = new int[sensorArr.length];
		
		for (int i = 0; i < colors.length; i++)
		{
			int r = rand.nextInt(256);
			int g = rand.nextInt(256);
			int b = rand.nextInt(256);
			int color = 0xff000000 + (r << 16) + (g << 8) + b;
			colors[i] = color;
		}
		BufferedImage img = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_ARGB);
		for (int r = 0; r < data.length; r++) {
			for (int c = 0; c < data[0].length; c++) {

				img.setRGB(r, c, colors[data[r][c] % colors.length]);
			}
		}
		ImageIO.write(img, "gif", new File("data2\\" + type + "_test.gif"));
	}
}
