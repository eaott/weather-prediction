package weather.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import weather.network.Label;
import weather.network.Network;

public class LorentzNaive {
	public static void main(String[] args) throws Throwable
	{
		long start = System.currentTimeMillis();
		final int ITERATIONS = Integer.parseInt(args[0]);
		final int HIDDEN = Integer.parseInt(args[1]);
		final double res = Double.parseDouble(args[2]);
		
		File in = new File("C:/Users/Evan/GitProjects/weather-prediction/LorenzIn.csv");
		File out1 = new File("C:/Users/Evan/GitProjects/weather-prediction/LorenzOut.csv");
		File out2 = new File("C:/Users/Evan/GitProjects/weather-prediction/LorenzOut2.csv");
		List<double[][][]> dataIn = new ArrayList<>();
		Scanner scan = new Scanner(in);
		while(scan.hasNextLine())
		{
			Scanner temp = new Scanner(scan.nextLine().replaceAll(",", " "));
			double x = temp.nextDouble();
			double y = temp.nextDouble();
			double z = temp.nextDouble();
			dataIn.add(new double[][][]{{{x,y,z}}});
			temp.close();
		}
		scan.close();
		
		Label[] labelArr = new Label[]{
				new Label("x", 0),
				new Label("y", 1),
				new Label("z", 2)
		};
		
		Network n = Network.naiveLinear(1, 1, labelArr, labelArr, HIDDEN, 1.0, res);
		
		System.out.println("Network created " + (System.currentTimeMillis() - start));
		for (int iter = 0; iter < ITERATIONS; iter++)
		{
			double[][][] input = dataIn.get(0);
			for (int i = 1; i < dataIn.size(); i++)
			{
				double[][][] output = dataIn.get(i);
				n.train(input, output);
				input = output;
			}
			System.out.println("Training iteration " + iter + " complete. " + (System.currentTimeMillis() - start));
		}
		
		double xDiff = 0, yDiff = 0, zDiff = 0, xDiff2 = 0, yDiff2 = 0, zDiff2 = 0;
		// Model is trained
		BufferedWriter outWriter = new BufferedWriter(new FileWriter(out1));
		for (int i = 0; i < dataIn.size() - 1; i++)
		{
			n.processInput(dataIn.get(i));
			// Potts inference (already normalized).
			double[][][] output = new double[1][1][3];
			output[0][0][0] = n.getOutputNeuron(0, 0, 0).getValue();
			output[0][0][1] = n.getOutputNeuron(0, 0, 1).getValue();
			output[0][0][2] = n.getOutputNeuron(0, 0, 2).getValue();
			outWriter.write(String.format("%.9f,%.9f,%.9f\n", output[0][0][0], output[0][0][1], output[0][0][2]));
			xDiff += output[0][0][0] - dataIn.get(i+1)[0][0][0];
			yDiff += output[0][0][1] - dataIn.get(i+1)[0][0][1];
			zDiff += output[0][0][2] - dataIn.get(i+1)[0][0][2];
			xDiff2 += Math.pow(output[0][0][0] - dataIn.get(i+1)[0][0][0], 2);
			yDiff2 += Math.pow(output[0][0][1] - dataIn.get(i+1)[0][0][1], 2);
			zDiff2 += Math.pow(output[0][0][2] - dataIn.get(i+1)[0][0][2], 2);
		}
		outWriter.close();
		System.out.println("first done");
		double[][][] input = dataIn.get(0);
		outWriter = new BufferedWriter(new FileWriter(out2));
		for (int i = 0; i < dataIn.size() - 1; i++)
		{
			n.processInput(input);
			// Potts inference (already normalized).
			double[][][] output = new double[1][1][3];
			output[0][0][0] = n.getOutputNeuron(0, 0, 0).getValue();
			output[0][0][1] = n.getOutputNeuron(0, 0, 1).getValue();
			output[0][0][2] = n.getOutputNeuron(0, 0, 2).getValue();
			outWriter.write(String.format("%.9f,%.9f,%.9f\n", output[0][0][0], output[0][0][1], output[0][0][2]));
			xDiff += output[0][0][0] - dataIn.get(i+1)[0][0][0];
			yDiff += output[0][0][1] - dataIn.get(i+1)[0][0][1];
			zDiff += output[0][0][2] - dataIn.get(i+1)[0][0][2];
			xDiff2 += Math.pow(output[0][0][0] - dataIn.get(i+1)[0][0][0], 2);
			yDiff2 += Math.pow(output[0][0][1] - dataIn.get(i+1)[0][0][1], 2);
			zDiff2 += Math.pow(output[0][0][2] - dataIn.get(i+1)[0][0][2], 2);
			input = output;
		}
		outWriter.close();
		System.out.printf("%f %f %f %f %f %f\n", xDiff / (dataIn.size() - 1), yDiff / (dataIn.size() - 1), zDiff / (dataIn.size() - 1), xDiff2 / (dataIn.size() - 1), yDiff2 / (dataIn.size() - 1), zDiff2 / (dataIn.size() - 1));
		System.out.println(System.currentTimeMillis() - start);
	}
}
