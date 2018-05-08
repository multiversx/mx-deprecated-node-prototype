package network.elrond.consensus;

import java.util.List;
import java.util.ArrayList;

public class PBFTBlock implements PBFT
{
    private List<Validator> listValidators;

    public PBFTBlock()
    {
        listValidators = new ArrayList<Validator>();
    }

    public void setValidators(List<Validator> listValidators)
    {
        this.listValidators.clear();

        for (int i = 0; i < listValidators.size(); i++)
        {
            this.listValidators.add(new Validator(listValidators.get(i)));
        }
    }

    public void getAnswerFromValidator(Validator val, ConsensusAnswerType answer)
    {
        if (listValidators.contains(val))
        {
            listValidators.get(listValidators.indexOf(val)).setAnswer(answer);
        }
    }

    public List<Validator> getListValidators()
    {
        return (listValidators);
    }

    public void validate()
    {
        //...bla bla bla, re-run tx's, suppose to agree
        getAnswerFromValidator(new Validator(ConsensusUtil.CRT_PUB_KEY), ConsensusAnswerType.AGREE);

    }

    public ConsensusAnswerType getAnswer(String strPubKey)
    {
        Validator v = null;

        int idx = listValidators.indexOf(new Validator(strPubKey));

        if (idx < 0)
        {
            return (ConsensusAnswerType.NOT_AVAILABLE);
        }

        return(listValidators.get(idx).getAnswer());
    }

    public ConsensusAnswerType getStatusPBFT()
    {
        ConsensusAnswerType result = ConsensusAnswerType.DISAGREE;

        int prop = (listValidators.size() * 2 / 3) + 1;

        for (int i = 0; i < listValidators.size(); i++) {
            if (listValidators.get(i).getAnswer() == ConsensusAnswerType.AGREE) {
                prop--;
            }
        }

        if (prop <= 0)
        {
            return (ConsensusAnswerType.AGREE);
        }
        else
        {
            return (ConsensusAnswerType.DISAGREE);
        }
    }
}
