package network.elrond.consensus;

import network.elrond.core.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The EligibleListValidators class implements a node-type
 *
 * @author Elrond Team - JLS
 * @version 1.0
 * @since 2018-05-14
 */

public class EligibleListValidators {
	// the validators listToTable
	private final List<Validator> validators = new ArrayList<>();
	// minimum rating found in the listToTable
	private int minRating;
	// maximum rating found in the listToTable
	private int maxRating;
	// maximum stake found in the listToTable
	private BigInteger maxStake;

	public EligibleListValidators() {
		maxStake = BigInteger.ZERO;

		minRating = Util.MAX_SCORE;
		maxRating = 0;
	}

	public void addValidator(Validator validator) {
		validators.add(validator);
	}

	/**
	 * Returns a deep copied list of validators. It returns a list with copies
	 * of each validator.
	 * 
	 * @param src
	 *            listToTable to be copied
	 * @return new listToTable of Validators
	 */
	public List<Validator> getValidatorListCopy() {
		List<Validator> copy = new ArrayList<Validator>();

		for (Validator srcValidator : validators) {
			copy.add(new Validator(srcValidator));
		}

		return copy;
	}

	public int getNrValidators() {
		return validators.size();
	}

	public Iterable<Validator> getValidators() {
		return validators;
	}

	public int getMinRating() {
		return minRating;
	}

	public void setMinRating(int minRating) {
		this.minRating = minRating;
	}

	public int getMaxRating() {
		return maxRating;
	}

	public void setMaxRating(int maxRating) {
		this.maxRating = maxRating;
	}

	public BigInteger getMaxStake() {
		return maxStake;
	}

	public void setMaxStake(BigInteger maxStake) {
		this.maxStake = maxStake;
	}

}
