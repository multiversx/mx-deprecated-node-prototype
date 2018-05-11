package network.elrond.chronology;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import network.elrond.consensus.Validator;

/**
 * The Round class implements the time frame in which a new Block can be committed
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-11
 */

public class Round {
    //te validators list used in block-consensus
    private List<Validator> listValidators;
    //Epoch container
    private Epoch parentEpoch;
    //Round ID (a.k.a height)
    private BigInteger roundHeight;

    /**
     * Explicit constructor
     * @param parentEpoch as Epoch
     */
    public Round(Epoch parentEpoch) {
        listValidators = new ArrayList<Validator>();
        this.parentEpoch = parentEpoch;
        if (this.parentEpoch == null) {
            roundHeight = BigInteger.ZERO;
        } else {
            if (this.parentEpoch.getLastRound() == null) {
                roundHeight = BigInteger.ZERO;
            } else {
                roundHeight = this.parentEpoch.getLastRound().roundHeight.add(BigInteger.ONE);
            }
        }
    }

    /**
     * Gets the parent Epoch (container)
     * @return the container as Epoch
     */
    public Epoch getParentEpoch() {
        return (parentEpoch);
    }

    /**
     * Gets the list of validators used in consensus
     * @return the list of validators as List
     */
    public List<Validator> getListValidators() {
        return (listValidators);
    }

    /**
     * Gets the Round ID (height)
     * @return the Round height as BigInteger
     */
    public BigInteger roundHeight() {
        return (roundHeight);
    }
}
