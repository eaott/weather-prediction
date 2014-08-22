package weather.util;

public interface ActivationFunction {
	double compute(double val);
	double derivative(double val);
	double inverse(double val);
}
