package network.elrond.consensus;

import java.util.List;

public interface PBFT
{
    void setValidators(List<Validator> listValidators);
    void setAnswerFromValidator(Validator val, ConsensusAnswerType answer);
    void validate();
    ConsensusAnswerType getAnswer(String strPubKey);
    ConsensusAnswerType getStatusPBFT();

}
