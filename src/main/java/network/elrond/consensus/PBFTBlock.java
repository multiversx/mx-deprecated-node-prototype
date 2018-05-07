package network.elrond.consensus;

import java.util.List;

public class PBFTBlock implements PBFT
{
    public void SetValidators(List<Validator> listValidators)
    {

    }

    public void GetAnswerFromValidator(Validator val, ConsensusAnswerType answer)
    {

    }

    public String RunConsensus()
    {
        return(null);
    }

    public ConsensusAnswerType GetAnswer()
    {
        return(ConsensusAnswerType.NOT_ANSWERED);
    }

    public ConsensusAnswerType GetStatusPBFT()
    {
        return (ConsensusAnswerType.NOT_ANSWERED);
    }



}
