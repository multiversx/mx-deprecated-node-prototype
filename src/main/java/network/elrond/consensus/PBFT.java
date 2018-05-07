package network.elrond.consensus;

import java.util.List;

public interface PBFT {

    void SetValidators(List<Validator> listValidators);
    void GetAnswerFromValidator(Validator val, ConsensusAnswerType answer);
    String RunConsensus();
    ConsensusAnswerType GetAnswer();
    ConsensusAnswerType GetStatusPBFT();
}
