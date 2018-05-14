package network.elrond.consensus;

import java.util.List;

/**
 * The PBFT interface defines a pBFT way to reach consensus
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-11
 */
public interface PBFT
{
    /**
     * Sets the validators used in consensus
     * @param listValidators to be set
     */
    void setValidators(List<Validator> listValidators);

    /**
     * Set a received answer from a validator
     * @param val is a validator who originates the answer
     * @param answer is the answer to be set
     */
    void setAnswerFromValidator(Validator val, ConsensusAnswerType answer);

    /**
     * Method for running the process of validating the input data on pBFT (at the end this will either agree or disagree)
     */
    void validate();

    /**
     * Gets the answer received from a public key
     * @param strPubKey as String
     * @return the validator answer as ConsensusAnswerType
     */
    ConsensusAnswerType getAnswer(String strPubKey);

    /**
     * Gets the overall status of the pBFT consensus
     * @return should return either Agree or Disagree
     */
    ConsensusAnswerType getStatusPBFT();

}
