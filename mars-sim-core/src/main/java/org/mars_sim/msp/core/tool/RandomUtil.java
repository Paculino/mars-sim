/*
 * Mars Simulation Project
 * RandomUtil.java
 * @date 2021-12-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.tool;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.mars_sim.msp.core.Msg;


// Note : may consider using it.unimi.dsi.util.XorShift1024StarRandom;

/**
 * The RandomUtil class is a library of various random-related methods.
 */
public final class RandomUtil {

	// MersenneTwisterFast provides a fast, much "more" random than the linear congruential of the java.util.Random
	// Note 1: it is compatible with standard java.util.Randrom's method and require
	// no mapping
	// See intro at
	// https://cran.r-project.org/web/packages/randtoolbox/vignettes/fullpres.pdf

	// Add XORSHIFT maven artifact
	// May try XorShift1024StarRandom

	// Add two implementation of SIMD-oriented Fast Mersenne Twister PNRG in java.
	// Note 1: they are not compatible with standard java.util.Random's methods,
	// require seeding and re-mapping of methods
	// Note 2: other PRNG in the MT family can be found at
	// https://github.com/zwxadz/SFMT-for-Java

//	private static final SecureRandom sr = new SecureRandom();
	// Note : if okay to block the thread during re-seeding,
	// may use SecureRandom sr = SecureRandom.getInstanceStrong()

//	public static String getAlgorithm() {
//		return sr.getAlgorithm();
//	}

	private static final Random random = ThreadLocalRandom.current();

	private RandomUtil() {}

	public static Random getRandom() {
		return random;
	}

	/**
	 * Returns true if given number is less than a random percentage.
	 *
	 * @param randomLimit the random percentage limit
	 * @return true if random percent is less than percentage limit
	 */
	public static boolean lessThanRandPercent(int randomLimit) {
		int rand = random.nextInt(100) + 1;
		return rand < randomLimit;
	}

	/**
	 * Returns true if given number is less than a random percentage.
	 *
	 * @param randomLimit the random percentage limit
	 * @return true if random percent is less than percentage limit
	 */
	public static boolean lessThanRandPercent(double randomLimit) {
		double rand = random.nextDouble() * 100D;
		return rand < randomLimit;
	}

	/**
	 * Returns a random int number from 0 to (and including) the number given.
	 *
	 * @param ceiling the int limit for the random number
	 * @return the random number
	 */
	public static int getRandomInt(int ceiling) {
		if (ceiling < 0)
			throw new IllegalArgumentException(Msg.getString("RandomUtil.log.ceilingMustBePositive") + ceiling); //$NON-NLS-1$
		return random.nextInt(ceiling + 1);
	}

	/**
	 * Returns a random int number from a given base number to (and including) the
	 * ceiling number given.
	 *
	 * @param base    the minimum number result (can be +ve or -ve)
	 * @param ceiling the maximum number result (can be +ve or -ve)
	 * @return the random number
	 */
	public static int getRandomInt(int base, int ceiling) {
		if (ceiling < base)
			throw new IllegalArgumentException(Msg.getString("RandomUtil.log.ceilingMustGreaterBase")); //$NON-NLS-1$
		return random.nextInt(ceiling - base + 1) + base;
	}

	/**
	 * Returns a random double number from 0 to 1.0.
	 *
	 * @param ceiling the maximum number result
	 * @return the random number
	 */
	public static double getRandomDouble() {
		return random.nextDouble();
	}

	/**
	 * Returns a random double number from 0 to the ceiling number given.
	 *
	 * @param ceiling the maximum number result
	 * @return the random number
	 */
	public static double getRandomDouble(double ceiling) {
		return random.nextDouble() * ceiling;
	}

	/**
	 * Returns a random double number from base to the ceiling number given.
	 *
	 * @param ceiling the maximum number result
	 * @return the random number
	 */
	public static double getRandomDouble(double base, double ceiling) {
		if (ceiling < base)
			throw new IllegalArgumentException(Msg.getString("RandomUtil.log.ceilingMustGreaterBase")); //$NON-NLS-1$
		// Note: switch from using ThreadLocalRandom.current().nextDouble(base, ceiling)
		return random.nextDouble() * (ceiling - base) + base;
	}

	/**
	 * Returns a random double number (-infi to +infi) under Gaussian ("normally") distributed with
	 * mean 0.0 and standard deviation 1.0 from this random number generator's
	 * sequence
	 *
	 * @return the random number
	 */
	public static double getGaussianDouble() {
		return random.nextGaussian();
	}

	/**
	 * Returns a random integer from 1 to the given integer. 1 has twice the chance
	 * of being chosen than 2 and so forth to the given integer.
	 *
	 * @param ceiling the maximum integer result (ceiling > 0)
	 * @return the random integer
	 */
	public static int getRandomRegressionInteger(int ceiling) {

		double totalWeight = 0D;
		double weight = 1D;

		for (int x = 0; x < ceiling; x++) {
			totalWeight += weight;
			weight /= 2D;
		}

		double randWeight = getRandomDouble(totalWeight);

		totalWeight = 0D;
		weight = 1D;
		int result = 0;

		for (int x = 0; x < ceiling; x++) {
			totalWeight += weight;
			weight /= 2D;
			if (randWeight < totalWeight) {
				result = x + 1;
				break;
			}
		}

		return result;
	}

	/**
	 * Returns a random integer from the base to the ceiling. Note: the [base] has
	 * twice the chance of being chosen than [the base + 1] and so forth to the
	 * given integer.
	 *
	 * @param base    the minimum number result
	 * @param ceiling the maximum integer result, ( ceiling > 0 )
	 * @return the random integer
	 */
	public static int getRandomRegressionInteger(int base, int ceiling) {

		double totalWeight = 0D;
		double weight = 1D;

		for (int x = base; x < ceiling; x++) {
			totalWeight += weight;
			weight /= 2D;
		}

		double randWeight = getRandomDouble(totalWeight);

		totalWeight = 0D;
		weight = 1D;
		int result = 0;
		for (int x = base; x < ceiling; x++) {
			totalWeight += weight;
			weight /= 2D;
			if (randWeight < totalWeight) {
				result = x + 1;
				break;
			}
		}

		return result;
	}

	/**
	 * Gets the average value returned from the getRandomRegressionInteger method.
	 *
	 * @param ceiling the maximum integer result, (ceiling > 0)
	 * @return average value.
	 */
	public static double getRandomRegressionIntegerAverageValue(int ceiling) {

		if (ceiling < 1) {
			throw new IllegalArgumentException("Ceiling must be positive");
		}
		double totalProbability = 0D;
		double totalValue = 0D;

		double probability = 1D;
		for (int x = 1; x <= ceiling; x++) {
			totalValue += x * probability;
			totalProbability += probability;
			probability /= 2D;
		}
		if (totalProbability > 0)
			return totalValue / totalProbability;
		else
			return totalValue;
	}

	/**
	 * Gets a random weighted object from a map.
	 *
	 * @param weightedMap a map of objects and their weights as Double values.
	 * @return randomly selected object from the list (or null if empty map).
	 */
	public static <T extends Object> T getWeightedRandomObject(Map<T, Double> weightedMap) {
		if (weightedMap == null) {
			throw new IllegalArgumentException(Msg.getString("RandomUtil.log.weightMapIsNull")); //$NON-NLS-1$
		}

		T result = null;

		// Get the total weight of all the objects in the map.
		double totalWeight = 0D;
		Iterator<Double> i = weightedMap.values().iterator();
		while (i.hasNext()) {
			double weight = i.next();
			if (weight > 0D) {
				totalWeight += weight;
			}
		}

		// Randomly select a weight value.
		double randWeight = getRandomDouble(totalWeight);

		// Determine which object the weight applies to.
		Iterator<T> j = weightedMap.keySet().iterator();
		while (j.hasNext()) {
			T key = j.next();
			double weight = weightedMap.get(key);
			if (weight > 0D) {
				if (randWeight <= weight) {
					result = key;
					break;
				} else
					randWeight -= weight;
			}
		}

		return result;
	}
	
	/**
	 * Gets a random weighted integer object from a map.
	 *
	 * @param weightedMap a map of objects and their weights as Double values.
	 * @return randomly selected object from the list (or null if empty map).
	 */
	public static <T extends Object> T getWeightedIntegerRandomObject(Map<T, Integer> weightedMap) {
		if (weightedMap == null) {
			throw new IllegalArgumentException(Msg.getString("RandomUtil.log.weightMapIsNull")); //$NON-NLS-1$
		}

		T result = null;

		// Get the total weight of all the objects in the map.
		int totalWeight = 0;
		Iterator<Integer> i = weightedMap.values().iterator();
		while (i.hasNext()) {
			int weight = i.next();
			if (weight > 0) {
				totalWeight += weight;
			}
		}

		// Randomly select a weight value.
		int randWeight = getRandomInt(totalWeight);

		// Determine which object the weight applies to.
		Iterator<T> j = weightedMap.keySet().iterator();
		while (j.hasNext()) {
			T key = j.next();
			int weight = weightedMap.get(key);
			if (weight > 0D) {
				if (randWeight <= weight) {
					result = key;
					break;
				} else
					randWeight -= weight;
			}
		}

		return result;
	}
}
