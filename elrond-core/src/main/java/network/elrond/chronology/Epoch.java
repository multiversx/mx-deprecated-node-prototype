package network.elrond.chronology;

import network.elrond.consensus.Validator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Epoch {
    private long dateMsEpochStarts;
    private long epochHeight;
    List<Validator> listWaiting;
    List<Validator> listEligible;

    public Epoch() {
        dateMsEpochStarts = new Date().getTime();
        epochHeight = 0;
        listWaiting = new ArrayList<>();
        listEligible = new ArrayList<>();
    }

    public long getDateMsEpochStarts(){
        return(dateMsEpochStarts);
    }

    public void setDateMsEpochStarts(long dateMsEpochStarts) throws IllegalArgumentException{
        if (dateMsEpochStarts <= 0){
            throw new IllegalArgumentException("dateMsEpochStarts should be a positive number!");
        }

        this.dateMsEpochStarts = dateMsEpochStarts;
    }

    public long getEpochHeight(){
        return (epochHeight);
    }

    public void setEpochHeight(long epochHeight) throws IllegalArgumentException{
        if (epochHeight < 0){
            throw new IllegalArgumentException("epochHeight should not be a negative number!");
        }

        this.epochHeight = epochHeight;
    }

    public List<Validator> getListWaiting(){
        synchronized (listWaiting) {
            return (listWaiting);
        }
    }

    public List<Validator> getListEligible(){
        synchronized (listEligible) {
            return (listEligible);
        }
    }
}
