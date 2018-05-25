package network.elrond.consensus;

import network.elrond.core.Util;

import java.math.BigInteger;

/**
 * The Validator class implements the node that can be part of consensus
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class Validator {
    //public key as String
    private String pubKey;
    //node's IP address
    private String ip;
    //the answer used in consensus
    private ConsensusAnswerType answer;
    //locked stake as sERDs
    private BigInteger stake;
    //rating value
    private int rating;

    //computed stake score
    private int scoreStake;
    //computed rating score
    private int scoreRating;
    /*......*/

    /**
     * Explicit constructor
     * @param pubKey public key as String
     * @param ip node's IP address
     * @param answer node's answer in consensus
     * @param stake node's locked stake as sERDs
     * @param rating node's rating value
     */
    public Validator(String pubKey, String ip, ConsensusAnswerType answer, BigInteger stake, int rating,
                     int scoreStake, int scoreRating){
        this(pubKey, ip, answer, stake, rating);
        this.scoreRating = scoreRating;
        this.scoreStake = scoreStake;
    }

    /**
     * Explicit constructor
     * @param pubKey public key as String
     * @param ip node's IP address
     * @param answer node's answer in consensus
     * @param stake node's locked stake as sERDs
     * @param rating node's rating value
     */
    public Validator(String pubKey, String ip, ConsensusAnswerType answer, BigInteger stake, int rating) {
        this(pubKey, ip, answer);
        this.stake = stake;
        this.rating = rating;
        scoreRating = 0;
        scoreStake = 0;
    }

    /**
     * Explicit constructor
     * @param pubKey public key as String
     * @param ip node's IP address
     * @param answer node's answer in consensus
     */
    public Validator(String pubKey, String ip, ConsensusAnswerType answer) {
        if(pubKey == null || pubKey.isEmpty()){
            throw new IllegalArgumentException("pubkey cannot be null!");
        }
        this.pubKey = pubKey;
        this.ip = ip;
        this.answer = answer;
        this.stake = BigInteger.valueOf(0);
        rating = 0;
        scoreRating = 0;
        scoreStake = 0;
    }

    /**
     * Explicit constructor
     * @param pubKey public key as String
     */
    public Validator(String pubKey) {
        this.pubKey = pubKey;
        this.ip = "127.0.0.1";
        this.answer = ConsensusAnswerType.NOT_ANSWERED;
        this.stake = BigInteger.valueOf(0);
        rating = 0;
        scoreRating = 0;
        scoreStake = 0;
    }

    /**
     * Copy constructor
     * @param src Validator to be copied
     */
    public Validator(Validator src) {
        if (src == null) {
            throw new IllegalArgumentException("src cannot be null");
        }
        this.pubKey = src.getPubKey();
        this.answer = src.getAnswer();
        this.ip = src.getIP();
        this.stake = src.getStake();
        this.rating = src.getRating();
        this.scoreRating = src.scoreRating;
        this.scoreStake = src.scoreStake;

    }

    /**
     * Gets the public key as String
     * @return the public key
     */
    public String getPubKey() {
        return (pubKey);
    }

    /**
     * Sets the public key
     * @param pubKey to be set
     */
    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    /**
     * Gets the IP address
     * @return the IP address as String
     */
    public String getIP() {
        return (this.ip);
    }

    /**
     * Sets the IP address
     * @param ip to be set
     */
    public void setIP(String ip) {
        this.ip = ip;
    }

    /**
     * Gets the answer
     * @return the answer as ConsensusAnswerType
     */
    public ConsensusAnswerType getAnswer() {
        return (answer);
    }

    /**
     * Sets the answer
     * @param answer to be set
     */
    public void setAnswer(ConsensusAnswerType answer) {
        this.answer = answer;
    }

    /**
     * Gets the locked stake as sERDs
     * @return the stake as BigInteger
     */
    public BigInteger getStake() {
        return (stake);
    }

    /**
     * Sets the locked stake
     * @param stake to set
     */
    public void setStake(BigInteger stake) {
        this.stake = stake;
    }

    /**
     * Gets the rating value
     * @return the rating value as int
     */
    public int getRating() {
        return (rating);
    }

    /**
     * Sets the rating value
     * @param rating to be set
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Gets the rating score
     * @return rating score as int
     */
    public int getScoreRating() {
        if (scoreRating < 0) {
            return (0);
        } else if (scoreRating > Util.MAX_SCORE) {
            return (Util.MAX_SCORE);
        } else {
            return (scoreRating);
        }
    }

    /**
     * Sets the rating score
     * @param scoreRating to be set
     */
    public void setScoreRating(int scoreRating)
    {
        this.scoreRating = scoreRating;
    }

    /**
     * Gets the stake score
     * @return stake score as int
     */
    public int getScoreStake() {
        if (scoreStake < 0) {
            return (0);
        } else if (scoreStake > Util.MAX_SCORE) {
            return (Util.MAX_SCORE);
        } else {
            return (scoreStake);
        }
    }

    /**
     * Sets the stake score
     * @param scoreStake to be set
     */
    public void setScoreStake(int scoreStake)
    {
        this.scoreStake = scoreStake;
    }

    /**
     * Method will equal another Validator based on the pubKey field
     * @param obj the Validator to be equaled
     * @return -1, 0, 1 based on the pubKey equalisation
     */
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

    /**
     * Method will output the pubKey hash (not this object's hash)
     * @return the pubKey hash
     */
    @Override
    public int hashCode() {
        return (pubKey.hashCode());
    }
}
