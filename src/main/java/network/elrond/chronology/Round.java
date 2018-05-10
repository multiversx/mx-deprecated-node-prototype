package network.elrond.chronology;

import network.elrond.consensus.Validator;
import network.elrond.core.Util;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Round {

    private List<Validator> listValidators;
    private Epoch parentEpoch;
    private BigInteger roundHeight;

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

    public Epoch getParentEpoch() {
        return (parentEpoch);
    }

    public List<Validator> getListValidators() {
        return (listValidators);
    }

    public BigInteger roundHeight() {
        return (roundHeight);
    }
}
