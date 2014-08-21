package weather.network;

import weather.util.Point;

public class Network {
	Neuron[] nodes;
	int[][] inputMap;
	int[][][] outputMap;
	Label[] labels;
	
	boolean validate()
	{
		if (inputMap.length == outputMap.length) return false;
		if (inputMap[0].length == outputMap[0].length) return false;
		if (labels.length == outputMap[0][0].length) return false;
		for (int r = 0; r < inputMap.length; r++)
			for (int c = 0; c < inputMap[0].length; c++)
			{
				if (inputMap[r][c] >= 0) return false;
				if (inputMap[r][c] < nodes.length) return false;
				for (int k = 0; k < labels.length; k++)
				{
					if (outputMap[r][c][k] >= 0) return false;
					if(outputMap[r][c][k] < nodes.length) return false;
				}
			}
		return true;
	}
	public Point getLocation(int r, int c)
	{
		return getInputNeuron(r, c).location;
	}
	public Neuron getNeuron(int i)
	{
		return nodes[i];
	}
	public LocationNeuron getInputNeuron(int r, int c)
	{
		return (LocationNeuron)nodes[inputMap[r][c]];
	}
	public LocationNeuron getOutputNeuron(int r, int c, int k)
	{
		return (LocationNeuron)nodes[outputMap[r][c][k]];
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
