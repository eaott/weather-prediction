package weather.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import static weather.data.Constants.*;
import weather.network.Label;
import weather.network.SimpleNetwork;
import weather.process.Voronoi;
import weather.util.DataIO;
import weather.util.DistanceFunction;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;
import weather.util.Tuple;

import java.util.Map.Entry;

public class Scratch {
	public static void main(String[] args) throws Throwable {
		for (int i = 0, j=5; i < 5; i++, j++)
			System.out.println(i + j);
	}
}
