package network.elrond.consensus;

import network.elrond.core.Util;

import java.math.BigInteger;

public class Validator {

    private String pubKey;
    private String ip;
    private ConsensusAnswerType answer;
    private BigInteger stake;
    private int rating;

    private int scoreStake;
    private int scoreRating;
    /*......*/

    public Validator(String pubKey, String ip, ConsensusAnswerType answer, BigInteger stake, int rating,
                     int scoreStake, int scoreRating){
        this.pubKey = pubKey;
        this.ip = ip;
        this.answer = answer;
        this.stake = stake;
        this.rating = rating;
        this.scoreRating = scoreRating;
        this.scoreStake = scoreStake;
    }

    public Validator(String pubKey, String ip, ConsensusAnswerType answer, BigInteger stake, int rating) {
        this.pubKey = pubKey;
        this.ip = ip;
        this.answer = answer;
        this.stake = stake;
        this.rating = rating;
        scoreRating = 0;
        scoreStake = 0;
    }

    public Validator(String pubKey, String ip, ConsensusAnswerType answer) {
        this.pubKey = pubKey;
        this.ip = ip;
        this.answer = answer;
        this.stake = BigInteger.valueOf(0);
        rating = 0;
        scoreRating = 0;
        scoreStake = 0;
    }

    public Validator(String pubKey) {
        this.pubKey = pubKey;
        this.ip = "127.0.0.1";
        this.answer = ConsensusAnswerType.NOT_ANSWERED;
        this.stake = BigInteger.valueOf(0);
        rating = 0;
        scoreRating = 0;
        scoreStake = 0;
    }

    public Validator(Validator src) {
        if (src == null) {
            this.pubKey = "";
            this.answer = ConsensusAnswerType.NOT_AVAILABLE;
            this.ip = "";
            stake = BigInteger.ZERO;
            rating = 0;
            scoreRating = 0;
            scoreStake = 0;
        } else {
            this.pubKey = src.getPubKey();
            this.answer = src.getAnswer();
            this.ip = src.getIP();
            this.stake = src.getStake();
            this.rating = src.getRating();
            this.scoreRating = src.scoreRating;
            this.scoreStake = src.scoreStake;
        }
    }

    public String getPubKey() {
        return (pubKey);
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getIP() {
        return (this.ip);
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public ConsensusAnswerType getAnswer() {
        return (answer);
    }

    public void setAnswer(ConsensusAnswerType answer) {
        this.answer = answer;
    }

    public BigInteger getStake() {
        return (stake);
    }

    public void setStake(BigInteger stake) {
        this.stake = stake;
    }

    public int getRating() {
        return (rating);
    }

    public void setRating(int rating) {
        this.rating = rating;
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



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return (false);
        }

        if (obj.getClass() != this.getClass()) {
            return (false);
        }

        return (this.pubKey.equals(((Validator) obj).getPubKey()));
    }

    @Override
    public int hashCode() {
        return (pubKey.hashCode());
    }
}
