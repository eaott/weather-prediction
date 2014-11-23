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
		System.out.println(ncfile.getVariables());
		Variable var_precip = ncfile.findVariable("Precip1hr");
		Array arr = var_precip.read();
		for (int s : arr.getShape())
			System.out.println(s);
	}
	

}
