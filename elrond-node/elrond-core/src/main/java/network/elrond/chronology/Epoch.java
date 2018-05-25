package network.elrond.chronology;

import java.util.ArrayList;
import java.util.List;
import network.elrond.consensus.Validator;

/**
 * The Epoch class implements the epoch time frame in which Rounds are being created
 *
 * @author  Elrond Team - JLS
 * @version 1.0
 * @since   2018-05-11
 */

public class Epoch {
    //waiting validators
    private List<Validator> listWaiting;
    //eligible validators (ready to be elected)
    private List<Validator> listEligible;
    //the list of Rounds
    private List<Round> listRounds;

    /**
     * Default constructor
     */
    public Epoch()
    {
        listWaiting = new ArrayList<Validator>();
        listEligible = new ArrayList<Validator>();
        listRounds = new ArrayList<Round>();
    }

    /**
     * Method used to create a new Round object. Note the double referencing
     * @return the newly created Round
     */
    public Round createRound(){
        Round r = new Round(this);
        listRounds.add(r);

        return(r);
    }

    /**
     * Gets the last Round from the list
     * @return the last round as Round
     */
    public Round getLastRound()
    {
        if (listRounds.size() == 0) {
            return (null);
        }

        return (listRounds.get(listRounds.size() - 1));
    }

    /**
     * Gets the eligible validators list
     * @return the eligible validators as List
     */
    public List<Validator> getEligibleList()
    {
        return (listEligible);
    }
}
