package weather.network;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class NetworkTest {
	static BufferedWriter out;
	public static void main(String[] args) throws Throwable
	{
		File f = new File("C:/Users/Evan/Dropbox/THESIS/sigmoid_test.csv");
		out = new BufferedWriter(new FileWriter(f));
		int[] iterations = {10, 20, 50};
		int[] hidden = {0, 1, 2, 4, 8};
		double[] resolution = {0.1};
		for (int iter : iterations)
			for (int hid : hidden)
				for (double res : resolution)
				{
					double err = 0;
					int N = 100;
					for (int i = 0; i < N; i++)
					{
						out.write(String.format("%d,%d", iter, hid));
						double e = getError(iter, hid, res);
						out.newLine();
						err += e;
					}
				}
		out.close();
	}
	
	public static double getError(int iterations, int layers, double resolution) throws IOException {
		Label[] labels = new Label[] { new Label("A", 0)};
		SimpleNetwork network = SimpleNetwork.naiveLinear(1, 1, labels, labels, layers, 1.0, resolution);
		final int ITERATIONS = iterations;
		double[][][][] inputData = new double[100][1][1][1];
		double[][][][] outputData = new double[inputData.length][1][1][1];
		for (int i = 0; i < inputData.length; i++)
		{
			inputData[i][0][0][0] = Math.random();
//			inputData[i][0][0][1] = Math.random();
//			inputData[i][0][1][0] = Math.random();
//			inputData[i][0][1][1] = Math.random();
			
			outputData[i][0][0][0] = fn(inputData[i][0][0][0]);
			
//			outputData[i][0][0][1] = gauss(fn(inputData[i][0][0][0] * inputData[i][0][0][0]* inputData[i][0][0][0]*  inputData[i][0][0][0]) * inputData[i][0][1][0]);
//			outputData[i][0][1][0] = fn(fn(.7 * inputData[i][0][1][0] + .3 * inputData[i][0][1][1]));
//			outputData[i][0][1][1] = fn(gauss(.1 * inputData[i][0][0][0] + .1 * inputData[i][0][0][1] + .8 * inputData[i][0][1][1]));
//			outputData[i][0][1][1] *= outputData[i][0][1][1];
		}
		for (int iter = 0; iter < ITERATIONS; iter++) {
			for (int i = 0; i < inputData.length; i++)
				network.train(inputData[i], outputData[i]);
//			if (iter == ITERATIONS - 1)
//			{
////				System.out
////				.printf("OUTPUT FROM ITERATION %d\n******************************************************\n",
////						iter);
//				double mean = 0;
//				double error = 0;
//				
//				for (int i = 0; i < inputData.length; i++) {
//					network.processInput(inputData[i]);
//					
//					error += Math.pow(outputData[i][0][0][0] - network.getOutputNeuron(0, 0, 0).getValue(), 2.0);
//					error += Math.pow(outputData[i][0][0][1] - network.getOutputNeuron(0, 0, 1).getValue(), 2.0);
//					error += Math.pow(outputData[i][0][1][0] - network.getOutputNeuron(0, 1, 0).getValue(), 2.0);
//					error += Math.pow(outputData[i][0][1][1] - network.getOutputNeuron(0, 1, 1).getValue(), 2.0);
//					
//					mean += outputData[i][0][0][0] - network.getOutputNeuron(0, 0, 0).getValue();
//					mean += outputData[i][0][0][1] - network.getOutputNeuron(0, 0, 1).getValue();
//					mean += outputData[i][0][1][0] - network.getOutputNeuron(0, 1, 0).getValue();
//					mean += outputData[i][0][1][1] - network.getOutputNeuron(0, 1, 1).getValue();	
//					
////					System.out.printf(
////							"%.1f->%.5f %.1f->%.5f\n%.1f->%.5f %.1f->%.5f\n\n",
////							outputData[i][0][0][0], network.getOutputNeuron(0, 0, 0)
////									.getValue(), outputData[i][0][0][1], network
////									.getOutputNeuron(0, 0, 1).getValue(),
////									outputData[i][0][1][0], network.getOutputNeuron(0, 1, 0)
////									.getValue(), outputData[i][0][1][1], network
////									.getOutputNeuron(0, 1, 1).getValue());
//				}
//				System.out.printf("Iteration %d: Mean is: %f Error is: %f\n", iter, mean / (4 * inputData.length), Math.sqrt(error / (4 * inputData.length - 1)));
//				
//			}
		}
		double error = 0;
		
		for (int i = 0; i < inputData.length; i++) {
			network.processInput(inputData[i]);
			
			error += Math.pow(outputData[i][0][0][0] - network.getOutputNeuron(0, 0, 0).getValue(), 2.0);
//			error += Math.pow(outputData[i][0][0][1] - network.getOutputNeuron(0, 0, 1).getValue(), 2.0);
//			error += Math.pow(outputData[i][0][1][0] - network.getOutputNeuron(0, 1, 0).getValue(), 2.0);
//			error += Math.pow(outputData[i][0][1][1] - network.getOutputNeuron(0, 1, 1).getValue(), 2.0);
			
		}
		out.write("," + network.getOutputNeuron(0,0,0).getFn());
		network.close();
		return Math.sqrt(error / (1 * inputData.length - 1));
//			System.out.printf(
//					"%.1f->%.5f %.1f->%.5f\n%.1f->%.5f %.1f->%.5f\n\n",
//					outputData[i][0][0][0], network.getOutputNeuron(0, 0, 0)
//							.getValue(), outputData[i][0][0][1], network
//							.getOutputNeuron(0, 0, 1).getValue(),
//							outputData[i][0][1][0], network.getOutputNeuron(0, 1, 0)
//							.getValue(), outputData[i][0][1][1], network
//							.getOutputNeuron(0, 1, 1).getValue());
		
//		double[] m = new double[4];
//		for (int i = 0; i < inputData.length; i++) {
//			network.processInput(inputData[i]);
//			m[0] += outputData[i][0][0][0];
//			m[1] += outputData[i][0][0][1];
//			m[2] += outputData[i][0][1][0];
//			m[3] += outputData[i][0][1][1];
//			System.out.printf(
//					"%.5f->%.5f %.5f->%.5f\n%.5f->%.5f %.5f->%.5f\n\n",
//					outputData[i][0][0][0], network.getOutputNeuron(0, 0, 0)
//							.getValue(), outputData[i][0][0][1], network
//							.getOutputNeuron(0, 0, 1).getValue(),
//							outputData[i][0][1][0], network.getOutputNeuron(0, 1, 0)
//							.getValue(), outputData[i][0][1][1], network
//							.getOutputNeuron(0, 1, 1).getValue());
//		}
//		System.out.printf("DATA MEANS: %f %f %f %f", m[0] / inputData.length, m[1] / inputData.length, m[2] / inputData.length, m[3]/inputData.length);
	}
	static double fn(double v)
	{
		return 1.0 / (1 + Math.exp(-v));
	}
	
	static double gauss(double v)
	{
		return Math.exp(-v * v);
	}
}
