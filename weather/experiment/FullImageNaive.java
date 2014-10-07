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
import weather.process.LoopyBP;
import weather.util.DataIO;
import weather.util.PairwiseFunction;
import weather.util.Tuple;

public class FullImageNaive {
	public static void main(String[] args) throws Throwable
	{
		long start = System.currentTimeMillis();
		final int ITERATIONS = Integer.parseInt(args[0]);
		final int HIDDEN = Integer.parseInt(args[1]);
		final double res = Double.parseDouble(args[2]);
		
		File dirIn = new File("data2/input");
		File dirOut = new File("data2/output");
		
		
		Tuple<Label[], Map<Integer,Integer>> tuple = DataIO.getLabels(dirIn, null);

		File tempFile = new File(dirIn, dirIn.list()[0]);
		BufferedImage tempImage = ImageIO.read(tempFile);
		int width = tempImage.getWidth();
		int height = tempImage.getHeight();

		Label[] labelArr = tuple.first();
		Map<Integer, Integer> map = tuple.second();
		
		System.out.println("Labels identified.");
		
		Network n = Network.naiveLinear(width, height, labelArr, labelArr, HIDDEN, 1.0, res);
		String[] files = dirIn.list();
		Arrays.sort(files, new Comparator<String>(){
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}});
		System.out.println("Network created " + (System.currentTimeMillis() - start));
		for (int iter = 0; iter < ITERATIONS; iter++)
		{
			double[][][] input = null;
			int i = 1;
			for (String f : files)
			{
				File file = new File(dirIn, f);
				double[][][] output = DataIO.getData(file, map);
				if (input != null)
				{
					System.out.println("training " + i++);
					n.train(input, output);
				}
				input = output;
			}
			System.out.println("Training iteration " + iter + " complete. " + (System.currentTimeMillis() - start));
		}
		
		
		// Model is trained
		for (int i = 0; i < files.length - 1; i++)
		{
			String f = files[i];
			File file = new File(dirIn, f);
			BufferedImage img = ImageIO.read(file);
			double[][][] input = new double[img.getWidth()][img.getHeight()][labelArr.length];
			for (int x = img.getMinX(); x < img.getWidth(); x++)
				for (int y = img.getMinY(); y < img.getHeight(); y++)
				{
					int curLabel = map.get(img.getRGB(x, y));
					input[x][y][curLabel] = 1;
				}
			n.processInput(input);
			// Potts inference (already normalized).
			double[][][] output = LoopyBP.infer(input, 4, new PairwiseFunction(){
				@Override
				public double prob(int rA, int cA, int kA, int rB,
						int cB, int kB) {
					double A = 2;
					double norm = 1 + Math.exp(-A);
					return kA == kB ? 1.0 / norm : Math.exp(-A) / norm;
				}}, n.getGraph());
			double[][][] next = new double[output.length][output[0].length][output[0][0].length];
			for (int x = 0; x < output.length; x++)
				for (int y = 0; y < output[0].length; y++)
				{
					int maxIndex = 0;
					for (int k = 0; k < output[0][0].length; k++)
					{
						if (output[x][y][maxIndex] < output[x][y][k])
							maxIndex = k;
					}
					next[x][y][maxIndex] = 1;
					img.setRGB(x, y, (int)labelArr[maxIndex].getValue());
				}
			ImageIO.write(img, "gif", new File(dirOut, files[i + 1].substring(0, files[i+1].length() - 4) + "_predicted.gif"));
			System.out.println(files[i+1] + " complete");
			input = next;
		}
		System.out.println(System.currentTimeMillis() - start);
	}
}
