package network.elrond.consensus;

/**
 * The ConsensusAnswerType enum defines the answer type used by validators in consensus block
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-11
 */
public enum ConsensusAnswerType
{
    //the validator agrees on consensus
    AGREE,
    //the validator disagree on consensus
    DISAGREE,
    //the validator did not answer (yet)
    NOT_ANSWERED,
    //the validator is not available
    NOT_AVAILABLE
}

