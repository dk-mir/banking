package dk.mir.banking.generators;
/**
 * Probability density function
 * @author Kalinin_DP
 *
 */
public class GaussianPdf {
	/**
	 * 
	 * @param x
	 * @return pdf(x) = standard Gaussian pdf
	 */
	public static double pdf(double x) {
		return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
	}
	/**
	 * @param x
	 * @param mu
	 * @param sigma
	 * @return pdf(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
	 */
	public static double pdf(double x, double mu, double sigma) {
		return pdf((x - mu) / sigma) / sigma;
	}
}
