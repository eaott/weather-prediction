package weather.process;

import java.util.HashMap;
import java.util.Map;

import weather.network.Network;
import weather.network.NetworkGraph;
import weather.util.PairwiseFunction;
import weather.util.Point;

public class LoopyBP {
	/**
	 * 2d array of probability distributions.
	 * [row][col][label]
	 * 
	 * FIXME use double[][][] instead, along with network graph
	 */
	public static double[][][] infer(Network net, int maxIter, PairwiseFunction fn) {
		NetworkGraph g = net.getGraph();
		double[][][] initProbs = new double[net.numRows()][net.numCols()][net.numOutputLabels()];
		for (int r = 0; r < net.numRows(); r++)
		{
			for (int c = 0; c < net.numCols(); c++)
			{
				for (int k = 0; k < net.numOutputLabels(); k++)
				{
					initProbs[r][c][k] = net.getOutputNeuron(r, c, k).getValue();
				}
			}
		}
		
		// Message TO A FROM B about label K
		Map<Point, Map<Point, double[]>> messages = null;
		if (maxIter < 1)
			maxIter = 1;
		for (int iter = 0; iter < maxIter; iter++)
		{
			messages = iter(net, g, messages, initProbs, fn);
		}
		
		double[][][] result = new double[net.numRows()][net.numCols()][net.numOutputLabels()];
		for (int r = 0; r < net.numRows(); r++)
		{
			for (int c = 0; c < net.numCols(); c++)
			{
				Point p = net.getLocation(r, c);
				double[] probs = result[r][c];
				// initialize to unary probs -- then multiply the last messages.
				for (int k = 0; k < net.numOutputLabels(); k++)
					probs[k] = initProbs[r][c][k];
				
				for (Point neighbor : messages.get(p).keySet())
				{
					double[] mult = messages.get(p).get(neighbor);
					for (int k = 0; k < net.numOutputLabels(); k++)
					{
						probs[k] *= mult[k];
					}
				}
				double sum = 0;
				for (double prob : probs)
					sum += prob;
				for (int i = 0; i < probs.length; i++)
					probs[i] /= sum;
				
			}
		}
		return result;
	}
	
	private static Map<Point, Map<Point, double[]>> iter(Network net, NetworkGraph g, Map<Point, Map<Point, double[]>> messages, double[][][] initProbs, PairwiseFunction fn)
	{
		// May not want neuron... may want Point instead...
		Map<Point, Map<Point, double[]>> updated = new HashMap<>();
		for (int rowA = 0; rowA < net.numRows(); rowA++)
		{
			for (int colA = 0; colA < net.numCols(); colA++)
			{
				// THESE TWO LOOPS REALLY ONLY COUNT AS ONE.
				
				double[] pastMessages = new double[net.numOutputLabels()];
				Point ptA = net.getLocation(rowA, colA);
				for (int labelA = 0; labelA < net.numOutputLabels(); labelA++)
				{	
					double totalPastMessages = 1;
					// If we have previous messages, incorporate them.
					if (messages != null)
					{
						for (Point ptU : g.getNeighbors(rowA, colA))
						{
							if (ptU.getR() == rowA && ptU.getC() == colA)
								continue;
							totalPastMessages *= messages.get(ptA).get(ptU)[labelA];
						}
					}
					pastMessages[labelA] = totalPastMessages;
				}
				
				// pastMessages[i] = PROD_{all neighbors u} M_{u->a)^(i-1)[i]
				
				for (Point ptB : g.getNeighbors(rowA, colA))
				{
					if (updated.get(ptB) == null)
						updated.put(ptB, new HashMap<Point, double[]>());
					double[] newVals = new double[net.numOutputLabels()]; 
					double sumOverAll = 0;
					for (int labelB = 0; labelB < net.numOutputLabels(); labelB++)
					{
						for (int labelA = 0; labelA < net.numOutputLabels(); labelA++)
						{
							double tempVal = initProbs[rowA][colA][labelA];
							tempVal *= fn.prob(net, rowA, colA, labelA, ptB.getR(), ptB.getC(), labelB);
							tempVal *= pastMessages[labelA];
							if (messages != null)
								tempVal /= messages.get(ptA).get(ptB)[labelA];
							newVals[labelB] += tempVal;
						}
						sumOverAll += newVals[labelB];
					}
					
					for (int i = 0; i < newVals.length; i++)
					{
						newVals[i] /= sumOverAll;
					}
					
					updated.get(ptB).put(ptA, newVals);
				}
			}
		}
		return updated;
	}
}
