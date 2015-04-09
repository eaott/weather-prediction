package weather.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static weather.data.Constants.*;
import weather.network.SimpleNetwork;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;

public class ScratchSpace {

	public static void main(String[] args) throws Throwable{
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF_Test");

		final File rainDir = new File(dir, "LCRA");
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		String[] unusableFileArr = Serializer.readFiles(dopplerDir, "UNUSABLEFILES");
		Set<String> unusableFiles = new HashSet<>();
		for (String s : unusableFileArr)
			unusableFiles.add(s);
		
		File[] allFiles = dopplerDir.listFiles();
		
		int[][] grk_voronoi = Serializer.readVoronoi(dir, "GRK");
		Point[][] coordinateMap = Serializer.readCoordinateConversion(dir, "GRK_COORDINATE");
		
		SimpleNetwork n = Serializer.readNetwork(dopplerDir, "testNetwork");
		System.out.println("loaded");
		long time = System.currentTimeMillis();
		double mse = ErrorCalculator.mse(allFiles, unusableFiles, n, grk_voronoi, coordinateMap, maps);
		System.out.printf("mse: %f time: %d\n", mse, System.currentTimeMillis() - time);
		n.close();
	}

}
