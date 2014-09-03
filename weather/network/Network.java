package weather.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

import weather.util.ActivationFunction;
import weather.util.Point;

public class Network {
	static final ActivationFunction[] FUNCTIONS = new ActivationFunction[]{new ActivationFunction(){
		@Override
		public double compute(double val) {
			return 1.0 / (1 + Math.exp(-val));
		}
		@Override
		public double derivative(double val) {
			double function = compute(val);
			return function * (1 - function);
		}
		@Override
		public double inverse(double val) {
			return -Math.log(1.0/val - 1.0);
		}
		}};
	static final int SIGMOID = 0;
	
	static final Neuron ONE = new Neuron();
	static
	{
		ONE.setValue(1.0);
	}
	int chosenFunction;
	double updateRate;
	Neuron[] nodes;
	int[][][] inputMap;
	int[][][] outputMap;
	Label[] inputLabels;
	Label[] outputLabels;
	Point[][] locations;
	int[] layerSizes;
		
	boolean validate()
	{
		if (inputMap.length == outputMap.length) return false;
		if (inputMap[0].length == outputMap[0].length) return false;
		if (inputMap[0][0].length == outputMap[0][0].length) return false;
		if (inputLabels.length == outputMap[0][0].length) return false;
		for (int r = 0; r < inputMap.length; r++)
			for (int c = 0; c < inputMap[0].length; c++)
			{
				for (int k = 0; k < inputLabels.length; k++)
				{
					if (inputMap[r][c][k] >= 0) return false;
					if (inputMap[r][c][k] < nodes.length) return false;
					if (outputMap[r][c][k] >= 0) return false;
					if (outputMap[r][c][k] < nodes.length) return false;
				}
			}
		return true;
	}
	private Network()
	{
		
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		
	}
	
	public static Network createNetwork(ObjectInputStream stream)
	{
		Network n = new Network();
		try {
			n.readObject(stream);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	
	/**
	 * Naive version, with same number of neurons in each layer. Doing this statically allows for
	 * different configurations later on (in addition to the params of the network to begin with).
	 * 
	 * FIXME make this recurrent? -- think they should be modeled as neurons of "fixed" value?
	 * @param outputLabels TODO
	 */
	public static Network naive(int rows, int cols, Label[] inputLabels, Label[] outputLabels, int hiddenLayers, double maxRadius, double learningRate)
	{
		Network n = new Network();
		n.chosenFunction = 0;
		n.updateRate = learningRate;
		// Need an input layer and output layer.
		int totalNodes = rows * cols * inputLabels.length * (hiddenLayers + 2);
		n.nodes = new Neuron[totalNodes];
		n.locations = new Point[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				n.locations[i][j] = new Point(i, j);
		n.inputMap = new int[rows][cols][inputLabels.length];
		// These are the input nodes.
		for (int r = 0; r < rows; r++)
		{
			for (int c = 0; c < cols; c++)
			{
				for (int k = 0; k < inputLabels.length; k++)
				{
					int index = r * cols * inputLabels.length + c * inputLabels.length + k;
					n.nodes[index] = new Neuron();
					n.inputMap[r][c][k] = index;
				}
			}
		}
		
		// FIXME num in layer
		
		n.outputMap = new int[rows][cols][outputLabels.length];
		NetworkGraph graph = NetworkGraph.getGraph(n.locations, maxRadius);
		// Do hidden layers AND the output layer.
		for (int layer = 1; layer <= hiddenLayers + 1; layer++)
		{
			for (int r = 0; r < rows; r++)
			{
				for (int c = 0; c < cols; c++)
				{
					for (int k = 0; k < outputLabels.length; k++)
						// FIXME update math for output instead.
					{
						int curIndex = layer * (rows * cols * inputLabels.length) + r * (cols * inputLabels.length) + c * (inputLabels.length) + k;
						// Should look at all neighbors of all labels.
						Set<Point> neighbors = graph.getNeighbors(r, c);
						double[] weights = new double[neighbors.size() * inputLabels.length + 1];
						int[] neurons = new int[neighbors.size() * inputLabels.length + 1];
						int smallIndex = 0;
						for (Point neighbor : neighbors)
						{
							for (int l = 0; l < inputLabels.length; l++)
							{
								int tempIndex = (layer - 1) * (rows * cols * inputLabels.length) + neighbor.getR() * (cols * inputLabels.length) + neighbor.getC() * (inputLabels.length) + l;
								weights[smallIndex] = 2 * (1 - Math.random()) - 1; // FIXME should range be [0, 1) or (-1, 1)?
								neurons[smallIndex++] = tempIndex;
							}
						}
						// Point each node at the bias as well (-1 -> ONE).
						weights[smallIndex] = 2 * (1 - Math.random()) - 1;
						neurons[smallIndex++] = -1;
						
						n.nodes[curIndex] = new Neuron(neurons, weights, n, FUNCTIONS[n.chosenFunction]);
						if (layer == hiddenLayers + 1)
						{
							n.outputMap[r][c][k] = curIndex;
						}
					}
				}
			}
		}
		
		n.inputLabels = inputLabels;
		n.outputLabels = outputLabels;
		
		return n;
	}
	
	public void processInput(double[][][] data)
	{
		if (data == null || data.length != numRows() || data[0].length != numCols() || data[0][0].length != numInputLabels())
			throw new IllegalArgumentException(String.format("%s %s %s %s %s %s", data.length, numRows(), data[0].length, numCols(), data[0][0].length, numInputLabels()));
		for (int r = 0; r < numRows(); r++)
			for (int c = 0; c < numCols(); c++)
				for (int k = 0; k < numInputLabels(); k++)
					getInputNeuron(r, c, k).setValue(data[r][c][k]);
		for (int i = 0; i < nodes.length; i++)
			nodes[i].recompute();
	}
	
	public void train(double[][][] input, double[][][] output)
	{
		// This is the "backpropagation" algorithm.
		processInput(input);
		System.out.println("Done processing");
		// Now, we can get the currently computed values for any point in the network.
		
		double[] F_Yi = new double[numRows() * numCols() * numInputLabels()];
		for (int r = 0; r < numRows(); r++)
		{
			for (int c = 0; c < numCols(); c++)
			{
				for (int k = 0; k < numInputLabels(); k++)
				{
					F_Yi[r * numCols() * numInputLabels() + c * numInputLabels() + k] = getOutputNeuron(r, c, k).getValue() - output[r][c][k];
				}
			}
		}
		
		int N = nodes.length - numRows() * numCols() * numInputLabels();
		// little extra data, but easier math.
		double[] F_xi = new double[nodes.length];
		double[] F_neti = new double[nodes.length];
		ActivationFunction f = FUNCTIONS[chosenFunction];
		int past = 0;
		double numHiddenLayers = nodes.length / (numRows() * numCols() * numInputLabels()) - 2;
		// Don't try to update the inputs.
		for (int i = nodes.length - 1; i >= numRows() * numCols() * numInputLabels(); i--)
		{
			double f_xi = 0;
			if (i >= N)
				f_xi += F_Yi[i - N];
			for (int j = 1 + i; j < nodes.length; j++)
			{
				double term = F_neti[j];
				Neuron neuron = nodes[j];
				boolean found = false;
				
				// See if the neuron actually points back to the one we're currently dealing with.
				for (int inputIndex = 0; inputIndex < neuron.getInputs().length; inputIndex++)
				{
					if (neuron.getInputs()[inputIndex] == i)
					{
						term *= neuron.getWeights()[inputIndex];
						found = true;
						break;
					}
				}
				
				if (found)
					f_xi += term;
			}
			F_xi[i] = f_xi;
			
			F_neti[i] = f.derivative(f.inverse(nodes[i].getValue())) * F_xi[i];
			double percent = (nodes.length - i) / (0.01 * numRows() * numCols() * numInputLabels() * (numHiddenLayers + 1));
			if (((int)percent) > past + 4)
			{
				System.out.printf("Fnet %f%% complete.\n", percent);
				past = (int)percent;
			}
		}
		
		// Update the weights.
		for (int i = numRows() * numCols() * numInputLabels(); i < nodes.length; i++)
		{
			double[] weights = nodes[i].getWeights();
			int[] inputs = nodes[i].getInputs();
			for (int weightIndex = 0; weightIndex < weights.length; weightIndex++)
			{
				double curWeight = weights[weightIndex];
				curWeight -= F_neti[i] * getNeuron(inputs[weightIndex]).getValue();
				weights[weightIndex] = curWeight;
			}
			double percent = (i - numRows() * numCols() * numInputLabels()) / (0.01 * numRows() * numCols() * numInputLabels() * (numHiddenLayers + 1));
			if (((int)percent) > past + 4)
			{
				System.out.printf("Weights %.1f%% complete.\n", percent);
				past = (int)percent;
			}		}
	}
	
	
	public Point getLocation(int r, int c)
	{
		return locations[r][c];
	}
	public Neuron getNeuron(int i)
	{
		// FIXME Can add handling here for recurrent network
		// Provide a different mapping scheme if i >= nodes.length
		if (i == -1)
			return ONE;
		return nodes[i];
	}
	public Neuron getInputNeuron(int r, int c, int k)
	{
		return nodes[inputMap[r][c][k]];
	}
	public Neuron getOutputNeuron(int r, int c, int k)
	{
		return nodes[outputMap[r][c][k]];
	}
	public Label getInputLabel(int i)
	{
		return inputLabels[i];
	}
	public Label getOutputLabel(int i)
	{
		return outputLabels[i];
	}
	public int numNeurons()
	{
		return nodes.length;
	}
	public int numRows()
	{
		return inputMap.length;
	}
	public int numCols()
	{
		return inputMap[0].length;
	}
	public int numInputLabels()
	{
		return inputLabels.length;
	}
	public int numOutputLabels()
	{
		return outputLabels.length;
	}
	public int numLayers()
	{
		return layerSizes.length;
	}
	public int numInLayer(int layer)
	{
		return layerSizes[layer];
	}
}
