package network.elrond.consensus;

import java.util.List;

public class PBFTBlock implements PBFT
{
    public void setValidators(List<Validator> listValidators)
    {

    }

    public void getAnswerFromValidator(Validator val, ConsensusAnswerType answer)
    {

    }

    public String runConsensus()
    {
        return(null);
    }

    public ConsensusAnswerType getAnswer()
    {
        return(ConsensusAnswerType.NOT_ANSWERED);
    }

    public ConsensusAnswerType getStatusPBFT()
    {
        return (ConsensusAnswerType.NOT_ANSWERED);
    }
}
