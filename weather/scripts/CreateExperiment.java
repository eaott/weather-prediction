package weather.scripts;

import java.io.File;
import java.util.Map;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import weather.util.Sensor;
import weather.util.Serializer;

public class CreateExperiment {
	public static void main(String[] args) throws Throwable {
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF_Test");
		final File rainDir = new File(dir, "LCRA");
		
		int[][] ewx_voronoi = Serializer.readVoronoi(dir, "EWX");
		Sensor[] sensors = Serializer.readSensors(dir, "SENSORS");
//		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		
		System.out.println("Data loaded.");
		
		File[] dopplerFiles = dopplerDir.listFiles();
		
		
		// FIXME for now, just do Doppler to complete voronoi
		for (File file : dopplerFiles)
		{
			long start = System.currentTimeMillis();
			NetcdfFile ncfile = NetcdfFile.open(file.getAbsolutePath());
			Variable var_precip = ncfile.findVariable("Precip1hr");
			Array arr = var_precip.read();
			float[][] data = (float[][])arr.copyToNDJavaArray();
			System.out.println((System.currentTimeMillis() - start));
		}
	}
}
