package network.elrond.consensus;

import network.elrond.core.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EligibleListValidators {

    public List<Validator> list;
    public int minRating;
    public int maxRating;
    public BigInteger maxStake;

    public EligibleListValidators()
    {
        list = new ArrayList<Validator>();
        maxStake = BigInteger.ZERO;

        minRating = Util.MAX_RATING;
        maxRating = 0;
    }


}
