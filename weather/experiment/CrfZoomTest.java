package weather.experiment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import weather.network.Label;
import weather.network.Network;
import weather.network.NetworkGraph;
import weather.process.LoopyBP;
import weather.util.PairwiseFunction;

public class CrfZoomTest {
	public static void main(String[] args) throws Throwable
	{		
		// do 3x2 but scale up by 256x
		final int START_WIDTH = 3, START_HEIGHT = 2, LABELS = 5;
		int width = START_WIDTH*256, height = START_HEIGHT * 256;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		double[][][] start = new double[START_WIDTH][START_HEIGHT][LABELS];
		for (int r = 0; r < START_WIDTH; r++)
		{
			for (int c = 0; c < START_HEIGHT; c++)
			{
				double sum = 0;
				for (int k = 0; k < LABELS; k++)
				{
					start[r][c][k] = Math.random();
					sum += start[r][c][k];
				}
				for (int k = 0; k < LABELS; k++)
				{
					start[r][c][k] /= sum;
				}
			}
		}
		
		// input is normalized.
		
		for (int power = 0; power < 8; power++)
		{
			Point[][] points = new Point[width][height];
			NetworkGraph g = NetworkGraph.getGraph(points, 1.0);
			
			double[][][] output = LoopyBP.infer(null, 4, new PairwiseFunction(){
				@Override
				public double prob(int rA, int cA, int kA, int rB,
						int cB, int kB) {
					double A = 2;
					double norm = 1 + Math.exp(-A);
					return kA == kB ? 1.0 / norm : Math.exp(-A) / norm;
				}}, g);
			
			img.setRGB(x, y, (int)labelArr[maxIndex].getValue());
			ImageIO.write(img, "gif", new File(dirOut, files[i + 1].substring(0, files[i+1].length() - 4) + "_predicted.gif"));
		}
	}
}
