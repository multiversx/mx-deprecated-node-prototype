package network.elrond.consensus;

import java.util.List;

public interface PBFT
{
    void setValidators(List<Validator> listValidators);
    void getAnswerFromValidator(Validator val, ConsensusAnswerType answer);
    void validate();
    ConsensusAnswerType getAnswer(String strPubKey);
    ConsensusAnswerType getStatusPBFT();

}
