package weather.process;

import java.util.HashMap;
import java.util.Map;

import weather.network.NetworkGraph;
import weather.util.PairwiseFunction;
import weather.util.Point;

public class LoopyBP {
	
	public static double[][][] infer(double[][][] input, int maxIter, PairwiseFunction fn, NetworkGraph g) {
		// Message TO A FROM B about label K
		Map<Point, Map<Point, double[]>> messages = null;
		if (maxIter < 1)
			maxIter = 1;
		for (int iter = 0; iter < maxIter; iter++)
		{
			messages = iter(g, messages, input, fn);
		}
		
		double[][][] result = new double[input.length][input[0].length][input[0][0].length];
		for (int r = 0; r < input.length; r++)
		{
			for (int c = 0; c < input[0].length; c++)
			{
				Point p = g.getAllPoints()[r][c];
				double[] probs = result[r][c];
				// initialize to unary probs -- then multiply the last messages.
				for (int k = 0; k < input[0][0].length; k++)
					probs[k] = input[r][c][k];
				
				for (Point neighbor : messages.get(p).keySet())
				{
					double[] mult = messages.get(p).get(neighbor);
					for (int k = 0; k < input[0][0].length; k++)
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
	private static Map<Point, Map<Point, double[]>> iter(NetworkGraph g,
			Map<Point, Map<Point, double[]>> messages, double[][][] input,
			PairwiseFunction fn) {
		// May not want neuron... may want Point instead...
		Map<Point, Map<Point, double[]>> updated = new HashMap<>();
		for (int rowA = 0; rowA < input.length; rowA++)
		{
			for (int colA = 0; colA < input[0].length; colA++)
			{
				// THESE TWO LOOPS REALLY ONLY COUNT AS ONE.
				
				double[] pastMessages = new double[input[0][0].length];
				Point ptA = g.getAllPoints()[rowA][colA];
				for (int labelA = 0; labelA < input[0][0].length; labelA++)
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
					double[] newVals = new double[input[0][0].length]; 
					double sumOverAll = 0;
					for (int labelB = 0; labelB < input[0][0].length; labelB++)
					{
						for (int labelA = 0; labelA < input[0][0].length; labelA++)
						{
							double tempVal = input[rowA][colA][labelA];
							tempVal *= fn.prob(rowA, colA, labelA, ptB.getR(), ptB.getC(), labelB);
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
