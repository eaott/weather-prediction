package weather;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.util.CancelTask;

public class NetCDFTest {

	public static void main(String[] args) throws Throwable{
		// TODO Auto-generated method stub
		String filein = "C:\\Users\\Evan\\GitProjects\\weather-prediction\\data2\\KFWD_SDUS34_N1PGRK_201308012242"
;
		NetcdfFile ncfile = NetcdfFile.open(filein);
//		System.out.println(ncfile.getVariables());
		Variable vLat = ncfile.findVariable("latitude");
		Variable vLon = ncfile.findVariable("longitude");
		System.out.printf("Latitude: %f\n",((float[])vLat.read().copyTo1DJavaArray())[0]);
		System.out.println(((float[])vLon.read().copyTo1DJavaArray())[0]);
		Variable var_precip = ncfile.findVariable("Precip1hr");
		Array arr = var_precip.read();
		float[][] data = (float[][])arr.copyToNDJavaArray();
		System.out.println(data.length);
		System.out.println(data[0].length);
	}
	

}
