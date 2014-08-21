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
		}}};
	static final int SIGMOID = 0;
	int chosenFunction;
	
	Neuron[] nodes;
	int[][][] inputMap;
	int[][][] outputMap;
	Label[] labels;
	Point[][] locations;
	
	double[][][][] lookback;
	
	boolean validate()
	{
		if (inputMap.length == outputMap.length) return false;
		if (inputMap[0].length == outputMap[0].length) return false;
		if (inputMap[0][0].length == outputMap[0][0].length) return false;
		if (labels.length == outputMap[0][0].length) return false;
		for (int r = 0; r < inputMap.length; r++)
			for (int c = 0; c < inputMap[0].length; c++)
			{
				for (int k = 0; k < labels.length; k++)
				{
					if (inputMap[r][c][k] >= 0) return false;
					if (inputMap[r][c][k] < nodes.length) return false;
					if (outputMap[r][c][k] >= 0) return false;
					if(outputMap[r][c][k] < nodes.length) return false;
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
	 */
	public static Network naive(int rows, int cols, Label[] labels, int hiddenLayers, double maxRadius)
	{
		Network n = new Network();
		n.chosenFunction = 0;
		// Need an input layer and output layer.
		int totalNodes = rows * cols * labels.length * (hiddenLayers + 2);
		n.nodes = new Neuron[totalNodes];
		n.locations = new Point[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				n.locations[i][j] = new Point(i, j);
		n.inputMap = new int[rows][cols][labels.length];
		// These are the input nodes.
		for (int r = 0; r < rows; r++)
		{
			for (int c = 0; c < cols; c++)
			{
				for (int k = 0; k < labels.length; k++)
				{
					int index = r * cols * labels.length + c * labels.length + k;
					n.nodes[index] = new Neuron();
					n.inputMap[r][c][k] = index;
				}
			}
		}
		n.outputMap = new int[rows][cols][labels.length];
		NetworkGraph graph = NetworkGraph.getGraph(n.locations, maxRadius);
		// Do hidden layers AND the output layer.
		for (int layer = 1; layer <= hiddenLayers + 1; layer++)
		{
			for (int r = 0; r < rows; r++)
			{
				for (int c = 0; c < cols; c++)
				{
					for (int k = 0; k < labels.length; k++)
					{
						int curIndex = layer * (rows * cols * labels.length) + r * (cols * labels.length) + c * (labels.length) + k;
						// Should look at all neighbors of all labels.
						Set<Point> neighbors = graph.getNeighbors(r, c);
						double[] weights = new double[neighbors.size() * labels.length];
						Neuron[] neurons = new Neuron[neighbors.size() * labels.length];
						int smallIndex = 0;
						for (Point neighbor : neighbors)
						{
							for (int l = 0; l < labels.length; l++)
							{
								int tempIndex = (layer - 1) * (rows * cols * labels.length) + neighbor.getR() * (cols * labels.length) + neighbor.getC() * (labels.length) + l;
								weights[smallIndex] = Math.random(); // FIXME should range be [0, 1) or (-1, 1)?
								neurons[smallIndex++] = n.nodes[tempIndex];
							}
						}
						n.nodes[curIndex] = new Neuron(neurons, weights, FUNCTIONS[n.chosenFunction]);
						if (layer == hiddenLayers + 1)
						{
							n.outputMap[r][c][k] = curIndex;
						}
					}
				}
			}
		}
		
		n.labels = labels;
		
		return n;
	}
	
	public void processInput(double[][][] data)
	{
		if (data == null || data.length != numRows() || data[0].length != numCols() || data[0][0].length != numLabels())
			throw new IllegalArgumentException();
		for (int r = 0; r < numRows(); r++)
			for (int c = 0; c < numCols(); c++)
				for (int k = 0; k < numLabels(); k++)
					getInputNeuron(r, c, k).setValue(data[r][c][k]);
		for (int i = 0; i < nodes.length; i++)
			nodes[i].recompute();
	}
	
	public void train(double[][][] input, double[][][] output)
	{
		// This is the "backpropagation" algorithm.
		processInput(input);
		// Now, we can get the currently computed values for any point in the network.
		
		
		
		
		
	}
	
	
	public Point getLocation(int r, int c)
	{
		return locations[r][c];
	}
	public Neuron getNeuron(int i)
	{
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
	public Label getLabel(int i)
	{
		return labels[i];
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
	public int numLabels()
	{
		return labels.length;
	}
}
