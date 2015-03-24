package weather.scripts;

import java.io.File;
import java.util.Map;

import weather.util.Serializer;

public class TestRain {

	public static void main(String[] args) throws Throwable{
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File rainDir = new File(dir, "LCRA");
		
		Map[] maps = Serializer.readRain(rainDir, "RAINMAP");
		System.out.println(maps.length);
		for (Map m : maps)
			System.out.println(m.size());
	}

}
