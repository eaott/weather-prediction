package weather.network;

import weather.util.ActivationFunction;

public class Neuron {
	double[] weights;
	Neuron[] inputs;
	double curValue;
	ActivationFunction fn;

	// Used for input nodes.
	public Neuron()
	{
		weights = new double[0];
		inputs = new Neuron[0];
	}
	public Neuron(Neuron[] in, double[] w, ActivationFunction fn)
	{
		inputs = in;
		weights = w;
		this.fn = fn;
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
			curValue += weights[i] * inputs[i].getValue();
		}
		// FIXME make sure this is correct.
		curValue = fn.compute(curValue);
	}
	public double getValue()
	{
		return curValue;
	}
}
