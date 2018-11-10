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
    /**
	 * The validator agrees on consensus.
	 */
    AGREE,
    
    /**
	 * The validator disagree on consensus.
	 */
    DISAGREE,
    
    /**
	 * The validator did not answer (yet).
	 */
    NOT_ANSWERED,
    
    /**
	 * The validator is not available.
	 */
    NOT_AVAILABLE
}

