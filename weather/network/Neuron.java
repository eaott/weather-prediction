package weather.network;

import weather.util.ActivationFunction;

public class Neuron {
	double[] weights;
	int[] inputs;
	double curValue;
	Network n;
	ActivationFunction fn;

	// Used for input nodes.
	public Neuron()
	{
		weights = new double[0];
		inputs = new int[0];
	}
	public Neuron(int[] in, double[] w, Network n, ActivationFunction fn)
	{
		inputs = in;
		weights = w;
		this.fn = fn;
		this.n = n;
	}
	public double setValue(double val)
	{
		double temp = curValue;
		curValue = val;
		return temp;
	}
	public double[] getWeights()
	{
		return weights;
	}
	public int[] getInputs()
	{
		return inputs;
	}
	/**
	 * Should be called in succession from input neurons
	 * to output.
	 */
	public void recompute()
	{
		if (weights == null || weights.length == 0)
			return;
		curValue = 0;
		for (int i = 0; i < weights.length; i++)
		{
			curValue += weights[i] * n.getNeuron(inputs[i]).getValue();
		}
		// FIXME make sure this is correct.
		curValue = fn.compute(curValue);
	}
	public double getValue()
	{
		return curValue;
	}
}
