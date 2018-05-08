package network.elrond.chronology;

import network.elrond.consensus.Validator;

import java.util.ArrayList;
import java.util.List;

public class Epoch {
    private List<Validator> listWaiting;
    private List<Validator> listEligible;
    private List<Round> listRounds;

    public Epoch()
    {
        listWaiting = new ArrayList<Validator>();
        listEligible = new ArrayList<Validator>();
        listRounds = new ArrayList<Round>();
    }

    public Round createRound(){
        Round r = new Round(this);
        listRounds.add(r);

        return(r);
    }

    public Round getLastRound()
    {
        if (listRounds.size() == 0) {
            return (null);
        }

        return (listRounds.get(listRounds.size() - 1));
    }

    public List<Validator> getEligibleList()
    {
        return (listEligible);
    }
}
