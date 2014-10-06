package weather.experiment;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SensorValue {

	public static void main(String[] args) throws Throwable{
		File dir = new File("C:\\Users\\Evan\\Dropbox\\Thesis_Data");
		Map<String, Map<Long, Double>> rainMap = new HashMap<>();
		{
			String name = "Bangs 6 W";
			File file = new File(dir, name + ".csv");
			Map<Long, Double> myRain = new TreeMap<>();
			CSVParser p = CSVParser.parse(file, Charset.defaultCharset(), 
					CSVFormat.DEFAULT);
			SimpleDateFormat f = new SimpleDateFormat("MMM dd yyyy hh:mmaa");
			for (CSVRecord r : p)
			{
				String sTime = r.get(0);
				String sRain = r.get(1);
				
				Date d = f.parse(sTime);
				double value = Double.parseDouble(sRain);
				
				myRain.put(d.getTime(), value);
			}
			rainMap.put(name, myRain);
		}
		
		System.out.println(rainMap);
	}
}
