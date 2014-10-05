package weather.experiment;

import java.io.File;
import java.util.Scanner;

public class VoronoiSensors {

	public static void main(String[] args) throws Throwable{
		String sensorFile = "C:\\Users\\Evan\\Dropbox\\Thesis_Data\\hydrometBook2.csv";
		Scanner in = new Scanner(new File(sensorFile));
		while (in.hasNextLine())
		{
			System.out.println(in.nextLine());
		}

	}

}
