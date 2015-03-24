package weather.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TreeMap;

import weather.util.Sensor;
import weather.util.Serializer;

/**
 * Purpose: create a single serialized document with (sensor_index, long_time)->rain
 * Prereqs: CreateSensors
 * @author Evan
 *
 */
public class CreateRainArchive {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws Throwable{
		final File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\");
		final File rainDir = new File(dir, "LCRA");
		
		Sensor[] sensors = Serializer.readSensors(rainDir, "SENSORS");
		TreeMap[] map = new TreeMap[sensors.length];
		for (int i = 0; i < map.length; i++)
		{
			map[i] = new TreeMap<Long, Double>();
			Sensor sensor = sensors[i];

			File rainFile = new File(rainDir, sensor.getId() + "_rpt.prn");
			Scanner in = new Scanner(rainFile);
			while (in.hasNextLine())
			{
				String line = in.nextLine();
				String[] arr = line.split("\\s+");
				String sDate = arr[1];
				String sTime = arr[2];
				String sVal = arr[3];
				double value = Double.parseDouble(sVal);
				String sDateTime = sDate + " " + sTime;
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				Date date = format.parse(sDateTime);
				long time = date.getTime();
				((TreeMap<Long, Double>)map[i]).put(time, value);
			}
			System.out.println((i + 1.0) / map.length);
			in.close();
		}
		Serializer.writeRain(map, rainDir, "RAINMAP");
	}
}
