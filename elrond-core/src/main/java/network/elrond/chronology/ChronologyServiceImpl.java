package network.elrond.chronology;

import java.math.BigInteger;

public class ChronologyServiceImpl implements ChronologyService {
    private final long roundsInEpochs;
    private final long roundTimeSeconds;

    public ChronologyServiceImpl(){
        roundsInEpochs = 28800;
        roundTimeSeconds = 4;
    }

    public ChronologyServiceImpl(long roundsInEpochs, long roundTimeSeconds){
        this.roundsInEpochs = roundsInEpochs;
        this.roundTimeSeconds = roundTimeSeconds;
    }

    public long getMilisecondsInEpoch(){
        return(roundsInEpochs * roundTimeSeconds * 100);
    }

    public boolean isDateTimeInEpoch(Epoch epoch, long dateMs) throws NullPointerException{
        if (epoch == null){
            throw new NullPointerException("epoch should not be null!");
        }

        return((epoch.getDateMsEpochStarts() <= dateMs) && (dateMs < epoch.getDateMsEpochStarts() + getMilisecondsInEpoch()));
    }

    public Round getRoundFromDateTime(Epoch epoch, long dateMs) throws NullPointerException, IllegalArgumentException{
        if (epoch == null){
            throw new NullPointerException("epoch should not be null!");
        }

        if (!isDateTimeInEpoch(epoch, dateMs)){
            throw new IllegalArgumentException(String.format("Parameter supplied %d does not belong to the supplied epoch [%d - %d)", dateMs,
                    epoch.getDateMsEpochStarts(), epoch.getDateMsEpochStarts() + getMilisecondsInEpoch()));
        }

        Round r = new Round();
        //(dateMs - epoch.dateMsEpochStarts) / roundTimeSeconds / 100;
        r.setRoundHeight((dateMs - epoch.getDateMsEpochStarts()) / roundTimeSeconds / 100);
        r.setLastRoundInEpoch(r.getRoundHeight() == (roundsInEpochs - 1));
        return(r);
    }

    public Epoch generateNewEpoch(Epoch previousEpoch) throws NullPointerException {
        if (previousEpoch == null){
            throw new NullPointerException("Parameter previousEpoch should not be null!");
        }

        Epoch newEpoch = new Epoch();
        newEpoch.setDateMsEpochStarts(previousEpoch.getDateMsEpochStarts() + getMilisecondsInEpoch());
        newEpoch.setEpochHeight(previousEpoch.getEpochHeight() + 1);
        //copy previous eligible list into the new epoch's eligible list
        synchronized (previousEpoch.listEligible){
            for (int i = 0; i < previousEpoch.listEligible.size(); i++) {
                newEpoch.listEligible.add(previousEpoch.listEligible.get(i));
            }
        }
        //copy previous waiting list into the new epoch's eligible list
        synchronized (previousEpoch.listWaiting){
            for (int i = 0; i < previousEpoch.listWaiting.size(); i++) {
                newEpoch.listEligible.add(previousEpoch.listWaiting.get(i));
            }
        }

        return(newEpoch);
    }
}
