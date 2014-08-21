package weather.network;

public class Neuron {
	double[] weights;
	Neuron[] inputs;
	double curValue;
	// FIXME initialization AND backpropagation
	/**
	 * Should be called in succession from input neurons
	 * to output.
	 */
	public void recompute()
	{
		curValue = 0;
		for (int i = 0; i < weights.length; i++)
		{
			curValue += weights[i] * inputs[i].getValue();
		}
		// FIXME make sure this is correct.
		curValue = 1.0 / (1 + Math.exp(-curValue));
	}
	public double getValue()
	{
		return curValue;
	}
}
