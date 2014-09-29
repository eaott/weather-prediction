package weather.experiment;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import weather.network.NetworkGraph;
import weather.process.LoopyBP;
import weather.util.PairwiseFunction;
import weather.util.Point;

public class CrfZoomTest {
	public static void main(String[] args) throws Throwable {
		// do 3x2 but scale up by 256x
		final int START_WIDTH = 3, START_HEIGHT = 2, LABELS = 6, POWER = 8;
		int[] colors = { 0xff0000ff, 0xff00ff00, 0xffff0000, 0xffffff00,
				0xff00ffff, 0xffff00ff };
		BufferedImage img = new BufferedImage(START_WIDTH * (1 << POWER),
				START_HEIGHT * (1 << POWER), BufferedImage.TYPE_INT_ARGB);

		for (int ITER = 0; ITER < 10; ITER++) {

			double[][][] start = new double[START_WIDTH][START_HEIGHT][LABELS];
			for (int r = 0; r < START_WIDTH; r++) {
				for (int c = 0; c < START_HEIGHT; c++) {
					double sum = 0;
					for (int k = 0; k < LABELS; k++) {
						start[r][c][k] = Math.random();
						sum += start[r][c][k];
					}
					for (int k = 0; k < LABELS; k++) {
						start[r][c][k] /= sum;
					}
				}
			}

			// input is normalized.

			for (int power = 0; power <= POWER; power++) {
				int divisor = 1 << power;
				int width = START_WIDTH * divisor;
				int height = START_HEIGHT * divisor;

				double[][][] extended = new double[width][height][LABELS];
				Point[][] points = new Point[width][height];
				for (int r = 0; r < width; r++) {
					for (int c = 0; c < height; c++) {
						points[r][c] = new Point(r, c);
						for (int k = 0; k < LABELS; k++) {
							extended[r][c][k] = start[r / divisor][c / divisor][k];
						}
					}
				}
				NetworkGraph g = NetworkGraph.getGraph(points, 1.0);

				// input and graph are ready

				double[][][] output = LoopyBP.infer(extended, 4,
						new PairwiseFunction() {
							@Override
							public double prob(int rA, int cA, int kA, int rB,
									int cB, int kB) {
								double A = 2;
								double norm = 1 + Math.exp(-A);
								return kA == kB ? 1.0 / norm : Math.exp(-A)
										/ norm;
							}
						}, g);

				int bigDivisor = 1 << POWER;

				for (int r = 0; r < START_WIDTH * bigDivisor; r++) {
					for (int c = 0; c < START_HEIGHT * bigDivisor; c++) {
						int augR = r / (1 << (POWER - power));
						int augC = c / (1 << (POWER - power));
						int maxIndex = 0;
						for (int k = 0; k < LABELS; k++) {
							if (output[augR][augC][k] > output[augR][augC][maxIndex])
								maxIndex = k;
						}

						img.setRGB(r, c, colors[maxIndex]);
					}
				}

				ImageIO.write(img, "gif", new File("data2/CrfZoom/"+ ITER + "_"
						+ power + ".gif"));
				System.out.println("power " + power + " complete.");
			}
		}
	}
}
