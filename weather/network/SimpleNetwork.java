package weather.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weather.util.ActivationFunction;
import weather.util.Point;

/**
 * Not most generalized solution. FOR ALL LAYERS,
 * enforces the radius condition for computational efficiency.
 * @author Evan
 *
 */
public class SimpleNetwork implements Serializable{
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
		@Override
		public String expr() {
			return "(1.0 / (1.0 + E^(-(%s))))";
		}
		}};
	static final int SIGMOID = 0;
	
	static final Neuron ONE = new Neuron();
	static
	{
		ONE.setValue(1.0);
	}
	// Index of the function in FUNCTIONS to use.
	int chosenFunction;
	// Parameter for how fast to update. Values > 1 may lead to poor results.
	double updateRate;
	// All the data for the network.
	Neuron[] nodes;
	// Mapping of location and label to actual nodes for input.
	int[][][] inputMap;
	// Mapping of location and label to actual nodes for output.
	int[][][] outputMap;
	// Available input labels.
	Label[] inputLabels;
	// Available output labels.
	Label[] outputLabels;
	// Simple map for converting between int r, int c to a Point.
	Point[][] locations;
	// Total number of nodes in each layer. layerSizes[0] is rows * cols * inputLabels.length, layerSizes[layerSizes.length - 1] is rows * cols * outputLabels.length
	int[] layerSizes;
	NetworkGraph graph;
	
	// FIXME convert to 2 * NCPU
	ExecutorService threadPool = Executors.newFixedThreadPool(8);

	private SimpleNetwork()
	{
		
	}
	
	public void close()
	{
		threadPool.shutdownNow();
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		stream.writeInt(chosenFunction);
		stream.writeDouble(updateRate);
		
		stream.writeInt(inputLabels.length);
		for (Label l : inputLabels)
		{
			stream.writeObject(l.name);
			stream.writeDouble(l.value);
		}
		
		stream.writeInt(outputLabels.length);
		for (Label l : outputLabels)
		{
			stream.writeObject(l.name);
			stream.writeDouble(l.value);
		}
		
		stream.writeInt(layerSizes.length);
		for (int n : layerSizes)
			stream.writeInt(n);
		
		stream.writeInt(inputMap.length);
		stream.writeInt(inputMap[0].length);
		stream.writeInt(inputMap[0][0].length);
		for (int[][] mat : inputMap)
			for (int[] arr : mat)
				for (int n : arr)
					stream.writeInt(n);
		
		stream.writeInt(outputMap.length);
		stream.writeInt(outputMap[0].length);
		stream.writeInt(outputMap[0][0].length);
		for (int[][] mat : outputMap)
			for (int[] arr : mat)
				for (int n : arr)
					stream.writeInt(n);
		
		stream.writeInt(locations.length);
		stream.writeInt(locations[0].length);
		for (Point[] arr : locations)
			for (Point p : arr)
			{
				stream.writeInt(p.getR());
				stream.writeInt(p.getC());
			}
		
		stream.writeInt(nodes.length);
		for (Neuron node : nodes)
		{
			double[] weights = node.weights;
			int[] inputs = node.inputs;
			stream.writeInt(weights.length);
			for (int i = 0; i < weights.length; i++)
			{
				stream.writeDouble(weights[i]);
				stream.writeInt(inputs[i]);
			}
			stream.writeInt(node.row);
			stream.writeInt(node.col);
		}
		stream.writeDouble(graph.getMaxDistance());
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		threadPool = Executors.newFixedThreadPool(8);
		chosenFunction = stream.readInt();
		updateRate = stream.readDouble();
		int inputLabelLength = stream.readInt();
		inputLabels = new Label[inputLabelLength];
		for (int i = 0; i < inputLabelLength; i++)
		{
			inputLabels[i] = new Label(stream.readObject().toString(), stream.readDouble());
		}
		
		int outputLabelLength = stream.readInt();
		outputLabels = new Label[outputLabelLength];
		for (int i = 0; i < outputLabelLength; i++)
		{
			outputLabels[i] = new Label(stream.readObject().toString(), stream.readDouble());
		}
		
		int layerLength = stream.readInt();
		layerSizes = new int[layerLength];
		for (int i = 0 ; i < layerLength; i++)
			layerSizes[i] = stream.readInt();
		
		int inputR = stream.readInt();
		int inputC = stream.readInt();
		int inputK = stream.readInt();
		inputMap = new int[inputR][inputC][inputK];
		for (int r = 0; r < inputR; r++)
			for (int c = 0; c < inputC; c++)
				for (int k = 0; k < inputK; k++)
					inputMap[r][c][k] = stream.readInt();
		
		int outputR = stream.readInt();
		int outputC = stream.readInt();
		int outputK = stream.readInt();
		outputMap = new int[outputR][outputC][outputK];
		for (int r = 0; r < outputR; r++)
			for (int c = 0; c < outputC; c++)
				for (int k = 0; k < outputK; k++)
					outputMap[r][c][k] = stream.readInt();
		
		int rows = stream.readInt();
		int cols = stream.readInt();
		locations = new Point[rows][cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				locations[i][j] = new Point(stream.readInt(), stream.readInt());
		
		int numNodes = stream.readInt();
		nodes = new Neuron[numNodes];
		for (int i = 0; i < numNodes; i++)
		{
			int numWeights = stream.readInt();
			double[] weights = new double[numWeights];
			int[] inputs = new int[numWeights];
			for (int j = 0; j < numWeights; j++)
			{
				weights[j] = stream.readDouble();
				inputs[j] = stream.readInt();
			}
			int row = stream.readInt();
			int col = stream.readInt();
			nodes[i] = new Neuron(inputs, weights, row, col, this, FUNCTIONS[chosenFunction]);
		}
		
		
		// Must place as extra parameter (get from graph.maxDistance)
		double maxDistance = stream.readDouble();
		graph = NetworkGraph.getGraph(locations, maxDistance);
	}
	
	
	/**
	 * Naive version, with same number of neurons in each layer. Doing this statically allows for
	 * different configurations later on (in addition to the params of the network to begin with).
	 * 
	 * Linearly grows (or shrinks) from number of input labels to number of output labels.
	 * 
	 * FIXME make this recurrent? -- think they should be modeled as neurons of "fixed" value?
	 * @param outputLabels TODO
	 */
	public static SimpleNetwork naiveLinear(int rows, int cols, Label[] inputLabels, Label[] outputLabels, int hiddenLayers, double maxRadius, double learningRate)
	{
		SimpleNetwork n = new SimpleNetwork();
		n.chosenFunction = 0;
		n.updateRate = learningRate;
		n.layerSizes = new int[hiddenLayers + 2];
		n.inputLabels = inputLabels;
		n.outputLabels = outputLabels;

		// Define the linear scheme for layers.
		int totalNodes = 0;
		for (int layer = 0; layer < n.numLayers(); layer++)
		{
			// if output == input, numLabels == input
			// if 0 hidden, layer==1 is max, numLayers==2, numLabels == output
			// if N hidden, layer==N+1 is max, numLayers==N+2, numLabels == output
			// if 1 hidden, layer==1 is hidden, numLayers==2. numLabels = input + 1/2(out - in)
			final int numLabels = ((n.outputLabels.length - n.inputLabels.length) * layer) / (n.numLayers() - 1) + n.inputLabels.length;
			n.layerSizes[layer] = rows * cols * numLabels;
			totalNodes += n.layerSizes[layer];
		}
		
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
				
		n.outputMap = new int[rows][cols][outputLabels.length];
		
		NetworkGraph graph = NetworkGraph.getGraph(n.locations, maxRadius);
		n.graph = graph;
		// Start numbering at the first "hidden"/output node.
		int curIndex = rows * cols * inputLabels.length;
		
		// Do hidden layers AND the output layer.
		// FIXME multithread the creation?
		for (int layer = 1; layer < n.numLayers(); layer++)
		{
			final int curLayerStart = curIndex;
			final int numLabels = n.numInLayer(layer) / (rows * cols);
			final int prevNumLabels = n.numInLayer(layer - 1) / (rows * cols);
			for (int r = 0; r < rows; r++)
			{
				for (int c = 0; c < cols; c++)
				{
					for (int k = 0; k < numLabels; k++)
					{
						// Should look at all neighbors of all labels.
						Set<Point> neighbors = graph.getNeighbors(r, c);
						
						// Allow each point to connect to the input directly, previous layer, and bias.
						// (but first layer, just point to input)
						double[] weights = new double[neighbors.size() * (layer == 1 ? n.inputLabels.length : n.inputLabels.length + prevNumLabels) + 1];
						int[] neurons = new int[neighbors.size() * (layer == 1 ? n.inputLabels.length : n.inputLabels.length + prevNumLabels) + 1];
						
						int smallIndex = 0;
						for (Point neighbor : neighbors)
						{
							// First, do input labels.
							for (int tempLabel = 0; tempLabel < inputLabels.length; tempLabel++)
							{
								int tempIndex = neighbor.getR() * (cols * inputLabels.length) + neighbor.getC() * (inputLabels.length) + tempLabel;
								weights[smallIndex] = 2 * (1 - Math.random()) - 1; // FIXME ? should range be [0, 1) or (-1, 1)<- current
								neurons[smallIndex++] = tempIndex;
							}
							
							// First layer only points to input
							if (layer == 1)
								continue;
							
							// Next, do previous layer.
							for (int tempLabel = 0; tempLabel < prevNumLabels; tempLabel++)
							{
								int tempIndex = curLayerStart - n.numInLayer(layer - 1) + 
										neighbor.getR() * (cols * prevNumLabels) + neighbor.getC() * (prevNumLabels) + tempLabel;
								weights[smallIndex] = 2 * (1 - Math.random()) - 1; // FIXME ? should range be [0, 1) or (-1, 1)<- current
								neurons[smallIndex++] = tempIndex;
							}
						}
						
						
						// Point each node at the bias as well (-1 -> ONE).
						weights[smallIndex] = 2 * (1 - Math.random()) - 1;
						neurons[smallIndex++] = -1;
						
						n.nodes[curIndex] = new Neuron(neurons, weights, r, c, n, FUNCTIONS[n.chosenFunction]);
						if (layer == hiddenLayers + 1)
						{
							n.outputMap[r][c][k] = curIndex;
						}
						
						curIndex++;
					}
				}
			}
		}
		
		
		
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
		// FIXME Can ||-ize if necessary here.
		int curIndex = numRows() * numCols() * numInputLabels();
		for (int i = 1; i < numLayers(); curIndex += numInLayer(i++))
		{
			List<Callable<Void>> callables = new ArrayList<Callable<Void>>();
			for (int j = 0; j < numInLayer(i); j++)
			{
				final int index = curIndex + j;
				callables.add(new Callable<Void>(){
					@Override
					public Void call() throws Exception {
						nodes[index].recompute();
						return null;
					}});
			}
			List<Future<Void>> futures;
			try {
				futures = threadPool.invokeAll(callables);
				for (Future<Void> future : futures)
					future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}	
		}
	}
	
	public void train(double[][][] input, double[][][] output)
	{
		// This is the "backpropagation" algorithm.
		processInput(input);
		// Now, we can get the currently computed values for any point in the network.
		
		final double[] F_Yi = new double[numRows() * numCols() * numOutputLabels()];
		for (int r = 0; r < numRows(); r++)
		{
			for (int c = 0; c < numCols(); c++)
			{
				for (int k = 0; k < numOutputLabels(); k++)
				{
					F_Yi[r * numCols() * numOutputLabels() + c * numOutputLabels() + k] = getOutputNeuron(r, c, k).getValue() - output[r][c][k];
				}
			}
		}
		
		// Total number of hidden and output nodes.
		final int N = nodes.length - numRows() * numCols() * numOutputLabels();
		
		// little extra data, but easier math.
		final double[] F_xi = new double[nodes.length];
		final double[] F_neti = new double[nodes.length];
		final ActivationFunction f = FUNCTIONS[chosenFunction];
		// Don't try to update the inputs.
		// Skip the output layer
		for (int t_layer = numLayers() - 1, t_layerIndex = nodes.length - numInLayer(numLayers() - 1); t_layer > 0; t_layerIndex-=numInLayer(--t_layer)) {
			List<Callable<Void>> callables = new ArrayList<Callable<Void>>();
			for (int i = t_layerIndex; i < t_layerIndex + numInLayer(t_layer); i++)
			{
				final int index = i;
				final int layer = t_layer;
				final int layerIndex = t_layerIndex;
				callables.add(new Callable<Void>(){
					@Override
					public Void call() throws Exception {
						double f_xi = 0;
						// Used for last layer...
						if (index >= N)
							f_xi += F_Yi[index - N];
						// Now, need all nodes that connect to this one...
						// CONSTRAINT: only look at the next layer
						// CONSTRAINT: only look at neighbors
						// FIXME HANDLE LOOKING AT LAYER OUTSIDE BOUNDS....
						
						int row = nodes[index].getRow();
						int col = nodes[index].getCol();
						Set<Point> neighbors = graph.getNeighbors(row, col);
						
						// startIndex is thus the base for nodes in the next layer.
						int startIndex = layerIndex + numInLayer(layer);
						if (layer != numLayers() - 1)
							for (Point neighbor : neighbors)
							{
								int r = neighbor.getR();
								int c = neighbor.getC();
								for (int k = 0; k < numLabelsInLayer(layer + 1); k++)
								{
									int j = startIndex + r * numCols() * numLabelsInLayer(layer + 1) + c * numLabelsInLayer(layer + 1) + k;
									
									// ***********************************
									// OLD CODE
									
									double term = F_neti[j];
									Neuron neuron = nodes[j];
									boolean found = false;
									
									// See if the neuron actually points back to the one we're currently dealing with.
									// FIXME surely this can be better... May need to structure this type of storage differenty.
									for (int inputIndex = 0; inputIndex < neuron.getInputs().length; inputIndex++)
									{
										if (neuron.getInputs()[inputIndex] == index)
										{
											term *= neuron.getWeights()[inputIndex];
											found = true;
											break;
										}
									}
									
									if (found)
										f_xi += term;
									// END OLD CODE
									// *****************************************************
								}
							}
						F_xi[index] = f_xi;
						
						F_neti[index] = f.derivative(f.inverse(nodes[index].getValue())) * F_xi[index];
						return null;
					}});
			}
			List<Future<Void>> futures;
			try {
				futures = threadPool.invokeAll(callables);
				for (Future<Void> future : futures)
				{
					future.get();
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		// Update the weights.
		List<Callable<Void>> callables = new ArrayList<Callable<Void>>();
		for (int i = numRows() * numCols() * numInputLabels() /* Start after input*/; i < nodes.length; i++)
		{
			final int index = i;
			callables.add(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					double[] weights = nodes[index].getWeights();
					int[] inputs = nodes[index].getInputs();
					for (int weightIndex = 0; weightIndex < weights.length; weightIndex++)
					{
						double curWeight = weights[weightIndex];
						curWeight -= F_neti[index] * getNeuron(inputs[weightIndex]).getValue();
						weights[weightIndex] = curWeight;
					}
					return null;
				}
				});
		}
		List<Future<Void>> futures;
		try {
			futures = threadPool.invokeAll(callables);
			for (Future<Void> future : futures)
				future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
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
	public int numLabelsInLayer(int layer)
	{
		return layerSizes[layer] / (numRows() * numCols());
	}
	public NetworkGraph getGraph()
	{
		return graph;
	}
	
	public double[][][] getOutput()
	{
		double[][][] out = new double[numRows()][numCols()][numOutputLabels()];
		for (int r = 0; r < numRows(); r++)
			for (int c = 0; c < numCols(); c++)
				for (int k = 0; k < numOutputLabels(); k++)
					out[r][c][k] = getOutputNeuron(r, c, k).getValue();
		return out;
	}
}
