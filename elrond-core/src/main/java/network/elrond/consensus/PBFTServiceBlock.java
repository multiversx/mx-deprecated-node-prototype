package network.elrond.consensus;

import java.util.List;
import java.util.ArrayList;
import network.elrond.core.Util;

/**
 * The PBFTBlock class implements PBFT interface to reach consensus on a block
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-14
 */
public class PBFTServiceBlock implements PBFTService {
    //list of validators used in the consensus
    private List<Validator> listValidators;

    /**
     * Implicit constructor
     */
    public PBFTServiceBlock() {
        listValidators = new ArrayList<Validator>();
    }

    /**
     * Sets the validators list by copying each validator
     * @param listValidators to be set
     */
    public void setValidators(List<Validator> listValidators) {
        this.listValidators.clear();

        for (int i = 0; i < listValidators.size(); i++) {
            this.listValidators.add(new Validator(listValidators.get(i)));
        }
    }

    /**
     * Set a received answer from a validator
     * @param val is a validator who originates the answer
     * @param answer is the answer to be set
     */
    public void setAnswerFromValidator(Validator val, ConsensusAnswerType answer) {
        if (listValidators.contains(val)) {
            listValidators.get(listValidators.indexOf(val)).setAnswer(answer);
        }
    }

    /**
     * Gets the list of validators used in consensus
     * @return the list of validators as List
     */
    public List<Validator> getListValidators() {
        return (listValidators);
    }

    /**
     * Method for running the process of validating the input data on pBFT (at the end this will either agree or disagree)
     */
    public void validate() {
        //...bla bla bla, re-run tx's, suppose to agree
        setAnswerFromValidator(new Validator(Util.CRT_PUB_KEY), ConsensusAnswerType.AGREE);

    }

    /**
     * ets the answer received from a public key
     * @param strPubKey as String
     * @return the validator answer as ConsensusAnswerType
     */
    public ConsensusAnswerType getAnswer(String strPubKey) {
        Validator v = null;

        int idx = listValidators.indexOf(new Validator(strPubKey));

        if (idx < 0) {
            return (ConsensusAnswerType.NOT_AVAILABLE);
        }

        return (listValidators.get(idx).getAnswer());
    }

    /**
     * Gets the overall status of the pBFT consensus
     * @return either Agree or Disagree
     */
    public ConsensusAnswerType getStatusPBFT() {
        ConsensusAnswerType result = ConsensusAnswerType.DISAGREE;

        int prop = (listValidators.size() * 2 / 3) + 1;

        for (int i = 0; i < listValidators.size(); i++) {
            if (listValidators.get(i).getAnswer() == ConsensusAnswerType.AGREE) {
                prop--;
            }
        }

        if (prop <= 0) {
            return (ConsensusAnswerType.AGREE);
        } else {
            return (ConsensusAnswerType.DISAGREE);
        }
    }
}
