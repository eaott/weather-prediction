package weather.network;

import weather.util.ActivationFunction;

public class Neuron {
	double[] weights;
	int[] inputs;
	double curValue;
	int row;
	int col;
	SimpleNetwork n;
	ActivationFunction fn;
	// Used for input nodes.
	public Neuron()
	{
		weights = new double[0];
		inputs = new int[0];
	}
	public Neuron(int[] in, double[] w, int r, int c, SimpleNetwork n, ActivationFunction fn)
	{
		inputs = in;
		weights = w;
		row = r;
		col = c;
		this.fn = fn;
		this.n = n;
	}
	public int getCol() {
		return col;
	}
	public int[] getInputs()
	{
		return inputs;
	}

	public int getRow() {
		return row;
	}
	public double getValue()
	{
		return curValue;
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
			curValue += weights[i] * n.getNeuron(inputs[i]).getValue();
		}
		// FIXME make sure this is correct.
		curValue = fn.compute(curValue);
	}
	public void setCol(int col) {
		this.col = col;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public double setValue(double val)
	{
		double temp = curValue;
		curValue = val;
		return temp;
	}
}
