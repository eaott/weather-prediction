package weather.scripts;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import weather.util.Sensor;
import weather.util.Serializer;

public class CreateSensors {

	public static void main(String[] args) throws Throwable {
		File sensorFile = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data\\LCRA\\LCRASiteInfo.csv");
		List<Sensor> list = new ArrayList<>();
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
			String lon = r.get(2);
			String lat = r.get(3);
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
			
			list.add(new Sensor(name, id, dlat, dlon, 0));

		}
		p.close();
		// Hacked way to create the array.
		Sensor[] sensorArr = list.toArray(new Sensor[0]);
		
		Serializer.writeSensors(sensorArr, sensorFile.getParentFile(), "SENSORS");
	}

}
