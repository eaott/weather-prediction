package weather.network;


public class NetworkTest {

	public static void main(String[] args)
	{
		int[] iterations = {10, 100, 1000, 10000};
		int[] hidden = {0, 1, 2, 3, 4, 5, 7, 10};
		double[] resolution = {0.005, 0.01, 0.05, 0.1, 0.2, 0.5};
		for (int iter : iterations)
			for (int hid : hidden)
				for (double res : resolution)
					System.out.printf("%d\t%d\t%f\t%f\n", iter, hid, res, getError(iter, hid, res));
	}
	
	public static double getError(int iterations, int layers, double resolution) {
		Label[] labels = new Label[] { new Label("A", 0), new Label("B", 1) };
		SimpleNetwork network = SimpleNetwork.naiveLinear(1, 2, labels, labels, layers, 2.0, resolution);
		final int ITERATIONS = iterations;
		double[][][][] inputData = new double[100][1][2][2];
		double[][][][] outputData = new double[inputData.length][1][2][2];
		for (int i = 0; i < inputData.length; i++)
		{
			inputData[i][0][0][0] = Math.random();
			inputData[i][0][0][1] = Math.random();
			inputData[i][0][1][0] = Math.random();
			inputData[i][0][1][1] = Math.random();
			
			outputData[i][0][0][0] = gauss(inputData[i][0][0][0] * inputData[i][0][0][1]);
			outputData[i][0][0][1] = gauss(fn(inputData[i][0][0][0] * inputData[i][0][0][0]* inputData[i][0][0][0]*  inputData[i][0][0][0]) * inputData[i][0][1][0]);
			outputData[i][0][1][0] = fn(fn(.7 * inputData[i][0][1][0] + .3 * inputData[i][0][1][1]));
			outputData[i][0][1][1] = fn(gauss(.1 * inputData[i][0][0][0] + .1 * inputData[i][0][0][1] + .8 * inputData[i][0][1][1]));
			outputData[i][0][1][1] *= outputData[i][0][1][1];
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
			error += Math.pow(outputData[i][0][0][1] - network.getOutputNeuron(0, 0, 1).getValue(), 2.0);
			error += Math.pow(outputData[i][0][1][0] - network.getOutputNeuron(0, 1, 0).getValue(), 2.0);
			error += Math.pow(outputData[i][0][1][1] - network.getOutputNeuron(0, 1, 1).getValue(), 2.0);
			
		}
		return Math.sqrt(error / (4 * inputData.length - 1));
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
