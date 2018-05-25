package network.elrond.consensus;

import network.elrond.core.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The EligibleListValidators class implements a node-type
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */

public class EligibleListValidators {
    //the validators list
    public List<Validator> list;
    //minim rating found in the list
    public int minRating;
    //maximum rating found in the list
    public int maxRating;
    //maximum stake found in the list
    public BigInteger maxStake;

    /**
     * Implict constructor
     */
    public EligibleListValidators()
    {
        list = new ArrayList<Validator>();
        maxStake = BigInteger.ZERO;

        minRating = Util.MAX_SCORE;
        maxRating = 0;
    }
}
