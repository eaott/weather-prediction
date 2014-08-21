package weather.util;

import weather.network.Network;

/**
 * Could be implemented with the Potts model, or something much more
 * complicated.
 * @author Evan
 */
public interface PairwiseFunction {
	double prob(Network n, int rA, int cA, int kA, int rB, int cB, int kB);
}
