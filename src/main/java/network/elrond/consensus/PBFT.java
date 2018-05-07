package network.elrond.consensus;

import java.util.List;

public interface PBFT
{
    void setValidators(List<Validator> listValidators);
    void getAnswerFromValidator(Validator val, ConsensusAnswerType answer);
    String runConsensus();
    ConsensusAnswerType getAnswer();
    ConsensusAnswerType getStatusPBFT();
}
