package weather.experiment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import weather.network.Label;
import weather.util.DataIO;
import weather.util.Tuple;

public class MovieMaker {
	public static void main(String[] args)
	{
		String input = "C:\\Users\\Evan\\Dropbox\\THESIS\\input";
		String output_no = "C:\\Users\\Evan\\Dropbox\\THESIS\\output_no";
		String output_crf = "C:\\Users\\Evan\\Dropbox\\THESIS\\output_crf";
		Tuple<Label[], Map<Integer, Integer>> tuple = DataIO.getLabels(new File(output_no));
		
		go(tuple, new File(input), new File(output_no), new File(output_crf), new File("C:\\Users\\Evan\\Dropbox\\THESIS\\movie\\"));
	}
	
	private static void go(Tuple<Label[], Map<Integer, Integer>> tuple,
			File inputDir, File outputNoDir, File outputCrfDir, File demoDir) {
		demoDir.mkdirs();
		File[] files = outputNoDir.listFiles();
		for (File outputNoFile : files)
		{
			String name = outputNoFile.getName();
			File inputFile = new File(inputDir, name);
			File outputCrfFile = new File(outputCrfDir, name);
			double[][][] outputNo = DataIO.getData(outputNoFile, tuple.second());
			double[][][] outputCrf = DataIO.getData(outputCrfFile, tuple.second());
			double[][][] input = DataIO.getData(inputFile, tuple.second());
			BufferedImage img = new BufferedImage(outputNo.length + input.length + 10 + outputCrf.length, outputNo[0].length, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < outputNo[0].length; y++)
			{
				for (int x = 0; x < input.length; x++)
				{
					int maxIndex = 0;
					for (int k = 0; k < input[0][0].length; k++)
						if (input[x][y][k] > input[x][y][maxIndex])
							maxIndex = k;
					img.setRGB(x, y, (int)tuple.first()[maxIndex].getValue());
				}
				
				for (int x = 0; x < outputNo.length; x++)
				{
					int maxIndex = 0;
					for (int k = 0; k < outputNo[0][0].length; k++)
						if (outputNo[x][y][k] > outputNo[x][y][maxIndex])
							maxIndex = k;
					img.setRGB(x + input.length + 5, y, (int)tuple.first()[maxIndex].getValue());
				}
				
				for (int x = 0; x < outputCrf.length; x++)
				{
					int maxIndex = 0;
					for (int k = 0; k < outputCrf[0][0].length; k++)
						if (outputCrf[x][y][k] > outputCrf[x][y][maxIndex])
							maxIndex = k;
					img.setRGB(x + input.length + 10 + outputNo.length, y, (int)tuple.first()[maxIndex].getValue());
				}
			}
			
			try {
				ImageIO.write(img, "gif", new File(demoDir, name));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(name + " complete.");
		}
	}
}
