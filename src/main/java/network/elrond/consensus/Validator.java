package network.elrond.consensus;

import java.math.BigInteger;

public class Validator {

    private String pubKey;
    private String ip;
    private ConsensusAnswerType answer;
    private BigInteger stake;
    private int rating;
    /*......*/

    public Validator(String pubKey, String ip, ConsensusAnswerType answer)
    {
        this.pubKey = pubKey;
        this.ip = ip;
        this.answer = answer;
        this.stake = BigInteger.valueOf(0);
        rating = 0;
    }

    public Validator(String pubKey)
    {
        this.pubKey = pubKey;
        this.ip = "127.0.0.1";
        this.answer = ConsensusAnswerType.NOT_ANSWERED;
        this.stake = BigInteger.valueOf(0);
        rating = 0;
    }

    public Validator(Validator src)
    {
        this.pubKey = src.getPubKey();
        this.answer = src.getAnswer();
        this.ip = src.getIP();
        this.stake = src.getStake();
    }

    public String getPubKey()
    {
        return(pubKey);
    }

    public void setPubKey(String pubKey)
    {
        this.pubKey = pubKey;
    }

    public String getIP()
    {
        return (this.ip);
    }

    public void setIP(String ip)
    {
        this.ip = ip;
    }

    public ConsensusAnswerType getAnswer()
    {
        return (answer);
    }

    public void setAnswer(ConsensusAnswerType answer)
    {
        this.answer = answer;
    }

    public BigInteger getStake()
    {
        return (stake);
    }

    public void SetStake(BigInteger stake)
    {
        this.stake = stake;
    }

    public int getRating()
    {
        return (rating);
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return (false);
        }

        if (obj.getClass() != this.getClass())
        {
            return (false);
        }

        return (this.pubKey.equals(((Validator)obj).getPubKey()));
    }

    @Override
    public int hashCode()
    {
        return (pubKey.hashCode());
    }
}
