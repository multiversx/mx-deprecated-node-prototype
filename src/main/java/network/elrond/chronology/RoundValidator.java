package network.elrond.chronology;

import network.elrond.consensus.Validator;
import network.elrond.core.Util;

public class RoundValidator extends Validator {

    private int scoreStake;
    private int scoreRating;

    public RoundValidator(Validator src) {
        super(src);

        scoreRating = 0;
        scoreStake = 0;
    }

    public RoundValidator Clone() {
        RoundValidator rv = new RoundValidator((Validator) this);
        rv.setScoreRating(this.scoreRating);
        rv.setScoreStake(this.scoreStake);

        return (rv);
    }

    public int getScore() {
        int score = Math.round(getScoreRating() * Util.WEIGHT_RATING_SPOS + getScoreStake() * Util.WEIGHT_STAKE_SPOS);

        if (score < 0) {
            score = 0;
        }

        if (score > Util.MAX_RATING) {
            score = Util.MAX_RATING;
        }

        return (score);
    }

    public int getScoreRating() {
        if (scoreRating < 0) {
            return (0);
        } else if (scoreRating > Util.MAX_RATING) {
            return (Util.MAX_RATING);
        } else {
            return (scoreRating);
        }
    }

    public void setScoreRating(int scoreRating)
    {
        this.scoreRating = scoreRating;
    }

    public int getScoreStake() {
        if (scoreStake < 0) {
            return (0);
        } else if (scoreStake > Util.MAX_RATING) {
            return (Util.MAX_RATING);
        } else {
            return (scoreStake);
        }
    }

    public void setScoreStake(int scoreStake)
    {
        this.scoreStake = scoreStake;
    }
}
