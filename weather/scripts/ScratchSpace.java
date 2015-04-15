package weather.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static weather.data.Constants.*;
import weather.data.Constants;
import weather.network.SimpleNetwork;
import weather.util.DataIO;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

public class ScratchSpace {

	public static void main(String[] args) throws Throwable{
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File dopplerDir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF_Test");

		final File rainDir = new File(dir, "LCRA");
		System.out.println(Constants.EWX_HEIGHT);
		System.out.println(Constants.EWX_WIDTH);
		System.out.println(Constants.GRK_HEIGHT);
		System.out.println(Constants.GRK_WIDTH);
		
		
	}

}
