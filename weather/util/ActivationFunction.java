package weather.util;

public interface ActivationFunction {
	double compute(double val);
	double derivative(double val);
	double inverse(double val);
	/**
	 * @return a string ready for formatting with textual substitution 
	 * for the function with a %s.
	 * 
	 * For example, a function f(z)=z^2 would return the string "%s^2".
	 */
	String expr();
}
