package weather.util;

import static weather.data.Constants.ALLOWED_TIME;
import static weather.data.Constants.PERCENT_COVERAGE;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import weather.network.SimpleNetwork;

public class ErrorCalculator {
	
	/*
	 * Look at varying metrics of error, including:
	 * Y proportion of data that is non-zero
	 * Y intensity of the storm
	 * -- if averaging over multiple frames?
	 * 
	 * Add in comparison against doppler directly to LCRA:
	 * -- if my system gives a smaller error (under each metric),
	 * that's good indication that this is solid post-processing,
	 * and a better estimate for where rainfall has occured. 
	 */

	
	public static double mse(double[][][] a, double[][][] b)
	{
		double err = 0;
		for (int r = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++)
				for (int k = 0; k < a[0][0].length; k++)
				{
					double val = a[r][c][k] - b[r][c][k];
					err += val * val;
				}
		return err / (a.length * a[0].length * a[0][0].length);
	}
	
	public static double mse_square_strength(double[][][] a, double[][][] b)
	{
		double err = 0;
		for (int r = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++)
				for (int k = 0; k < a[0][0].length; k++)
				{
					double val = a[r][c][k] - b[r][c][k];
					double avg = (a[r][c][k] + b[r][c][k]) / 2.0;
					err += val * val * avg * avg;
					
					// Can also be computed by
					// .25 * Math.pow(a[r][c][k] * a[r][c][k] - b[r][c][k] * b[r][c][k],2.0);
				}
		return err / (a.length * a[0].length * a[0][0].length);
	}
	
	public static double[] mses(double[][][] a, double[][][] b)
	{
		double mse = 0;
		double str = 0;
		for (int r = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++)
				for (int k = 0; k < a[0][0].length; k++)
				{
					double val = a[r][c][k] - b[r][c][k];
					double avg = (a[r][c][k] + b[r][c][k]) / 2.0;
					mse += val * val;
					str += val * val * avg * avg;
					
					// Can also be computed by
					// .25 * Math.pow(a[r][c][k] * a[r][c][k] - b[r][c][k] * b[r][c][k],2.0);
				}
		return new double[]{mse / (a.length * a[0].length * a[0][0].length),
				str / (a.length * a[0].length * a[0][0].length),
				};
	}
	
	
	
	
	
	public static double[] mses(double[][][] a, double[][][] b, double[][][] correct)
	{
		double[] vals = new double[4];

		for (int r = 0; r < a.length; r++)
			for (int c = 0; c < a[0].length; c++)
				for (int k = 0; k < a[0][0].length; k++)
				{
					double val = a[r][c][k] - correct[r][c][k];
					double avg = (a[r][c][k] + correct[r][c][k]) / 2.0;
					vals[0] += val * val;
					vals[1] += val * val * avg * avg;
					
					val = b[r][c][k] - correct[r][c][k];
					avg = (b[r][c][k] + correct[r][c][k]) / 2.0;
					vals[2] += val * val;
					vals[3] += val * val * avg * avg;
				}
		vals[0] /= (a.length * a[0].length * a[0][0].length);
		vals[1] /= (a.length * a[0].length * a[0][0].length);
		vals[2] /= (a.length * a[0].length * a[0][0].length);
		vals[3] /= (a.length * a[0].length * a[0][0].length);
		return vals;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static double mse_no_zero(double[][][] observed, double[][][] expected)
	{
		double err = 0;
		int num = 0;
		for (int r = 0; r < observed.length; r++)
			for (int c = 0; c < observed[0].length; c++)
				for (int k = 0; k < observed[0][0].length; k++)
				{
					if (expected[r][c][k] < 0.00001 || observed[r][c][k] < 0.00001)
					{
						continue;
					}
					double val = observed[r][c][k] - expected[r][c][k];
					err += val * val;
					num++;
				}
		if (num == 0)
			return -1;
		return err / num;
	}
	
	

}
