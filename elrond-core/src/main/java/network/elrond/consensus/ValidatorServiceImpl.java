package network.elrond.consensus;

import network.elrond.core.Util;

public class ValidatorServiceImpl implements  ValidatorService{
    /**
     * Computes the Validator score based on stake score, rating score, Util.WEIGHT_RATING_SPOS and
     * Util.WEIGHT_STAKE_SPOS
     * @param val Validator to compute the score
     * @return the overall Validator score as int
     */
    @Override
	public int computeValidatorScore(Validator val) {
        int score = Math.round(val.getScoreRating() * Util.WEIGHT_RATING_SPOS + val.getScoreStake() * Util.WEIGHT_STAKE_SPOS);

        if (score < 0) {
            score = 0;
        }

        if (score > Util.MAX_SCORE) {
            score = Util.MAX_SCORE;
        }

        return (score);
    }
}
