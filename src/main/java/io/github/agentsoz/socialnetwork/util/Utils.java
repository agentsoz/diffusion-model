package io.github.agentsoz.socialnetwork.util;

import io.github.agentsoz.socialnetwork.util.Global;


public class Utils {


	
	/*
	 *  70% within one SD
	 *  90% within two SDs
	 *  99% within three SDs
	 *   most values will be within three SDs either side away.
	 *  absolute min and max generated?
	 *  nextGaussian() could sooner or later produce values outside our expected range. Therefore. better to artifically contrain the range
	 *  Remember that you should generate 38,384 values.
	 */
	public static double getRandomGaussion(double desiredSD, double desiredMean) {
        if(desiredSD == 0.0 && desiredMean == 0.0) { // no need to calculate random probability, no propagation
            return 0.0;
        }
        else {
            double gausVal = Global.getRandom().nextGaussian() * desiredSD + desiredMean;
            while (gausVal < 0 || gausVal > 1) {  // lessthan 0 OR > 1
                gausVal = Global.getRandom().nextGaussian() * desiredSD + desiredMean;
            }

            return gausVal;
        }
	}

    public static double getRandomGaussionWithinThreeSD(double desiredSD, double desiredMean) {
	    if(desiredSD == 0.0 && desiredMean == 0.0) { // no need to calculate random probability, no propagation
	        return 0.0;
        }
        else {
            double gausVal = Global.getRandom().nextGaussian() * desiredSD + desiredMean;
            double lbound = desiredMean - (3 * desiredSD); // interval lb
            double ubound = desiredMean + (3 * desiredSD); // interval ub

            if (lbound < 0.0) {
                lbound = 0.0;
            } //  restrict boundaries within 0 and 1.
            if (ubound > 1.0) {
                ubound = 1.0;
            }

            while (gausVal < lbound || gausVal > ubound) {  // lessthan 0 OR > 1
                gausVal = Global.getRandom().nextGaussian() * desiredSD + desiredMean;
            }

            return gausVal;
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if {@code a} is {@code null}
     */
    public static void shuffle(int[] a) {
        validateNotNull(a);
        int n = a.length;
        for (int i = 0; i < n; i++) {
            int r = i + uniform(n-i);     // between i and n-1
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }
    
    /**
     * Returns a random integer uniformly in [0, n).
     * 
     * @param n number of possible integers
     * @return a random integer uniformly between 0 (inclusive) and {@code n} (exclusive)
     * @throws IllegalArgumentException if {@code n <= 0}
     */
    public static int uniform(int n) {
        if (n <= 0) throw new IllegalArgumentException("argument must be positive: " + n);
        return Global.getRandom().nextInt(n);
    }


    // throw an IllegalArgumentException if x is null
    // (x can be of type Object[], double[], int[], ...)
    private static void validateNotNull(Object x) {
        if (x == null) {
            throw new IllegalArgumentException("argument is null");
        }
    }
	
}
