package weather.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static weather.data.Constants.*;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import weather.network.SimpleNetwork;
import weather.util.ErrorCalculator;
import weather.util.Point;
import weather.util.Sensor;
import weather.util.Serializer;

public class ScratchSpace2 {

	public static void main(String[] args) throws Throwable{
		File dir = new File("C:\\Users\\Evan\\Desktop\\Thesis_NetCDF_Test");
		System.out.println("Start");
		for (File f : dir.listFiles())
		{
			if (f.getName().contains(".ser"))
				continue;
			String filein = f.getAbsolutePath();
			NetcdfFile ncfile = NetcdfFile.open(filein);
	//						System.out.println(ncfile.getVariables());
//			Variable vLat = ncfile.findVariable("latitude");
//			Variable vLon = ncfile.findVariable("longitude");
//			System.out.printf("Latitude: %f\n",((float[])vLat.read().copyTo1DJavaArray())[0]);
//			System.out.println(((float[])vLon.read().copyTo1DJavaArray())[0]);
			Variable var_precip = ncfile.findVariable("Precip1hr");
			Array arr = var_precip.read();
//			int[] shape = arr.getShape();
//			System.out.println(shape[0] + " " + shape[1]);
//			Index index = Index.factory(shape);
//			System.out.println(index);
//			System.out.println(arr.getFloat(index));
//			System.out.println(arr.getDataType());
			float[][] data = (float[][])arr.copyToNDJavaArray();
			System.out.println(f.getName());
			for (int r = 0; r < data.length; r++)
			{
				for (int c = 0; c < data[0].length; c++)
				{
//					data[r][c] = arr.getFloat(index);
//					index.incr();
					if (!Double.isNaN(data[r][c]) && data[r][c] > 0)
						System.out.println(f.getName() + " " + r + " " + c + " " + data[r][c]);
				}
			}
		}
	}

}