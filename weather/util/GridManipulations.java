package weather.util;

public class GridManipulations
{
	public static double[][][] expand(double[][][] input, int scale)
	{
		double[][][] out = new double[scale * input.length][scale * input[0].length][input[0][0].length];
		for (int r = 0; r < out.length; r++)
		{
			for (int c = 0; c < out[0].length; c++)
			{
				for (int k = 0; k < out[0][0].length; k++)
				{
					out[r][c][k] = input[r / scale][c / scale][k];
				}
			}
		}
		return out;
	}
}