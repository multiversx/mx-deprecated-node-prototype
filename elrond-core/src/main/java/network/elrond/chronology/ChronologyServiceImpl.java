package network.elrond.chronology;

import network.elrond.core.Util;

public class ChronologyServiceImpl implements ChronologyService {
    private final long roundTimeDuration;

    public ChronologyServiceImpl(){
        roundTimeDuration = 4000; //4 seconds
    }

    public ChronologyServiceImpl(long roundTimeDuration) throws IllegalArgumentException{
        Util.check(roundTimeDuration > 0, "roundTimeDuration must be a strict positive number!");

        this.roundTimeDuration = roundTimeDuration;
    }

    public long getRoundTimeDuration(){
        return(roundTimeDuration);
    }

    public boolean isDateTimeInRound(Round round, long timeStamp) throws IllegalArgumentException{
        Util.check(round != null, "round should not be null!");

        return((round.getStartTimeStamp() <= timeStamp) && (timeStamp < round.getStartTimeStamp() + roundTimeDuration));
    }

    public Round getRoundFromDateTime(long genesisRoundTimeStamp, long timeStamp) throws IllegalArgumentException{
        long delta = timeStamp - genesisRoundTimeStamp;

        Util.check(timeStamp >= genesisRoundTimeStamp, "genesisRoundTimeStamp should be lower or equal to dateMillis!");

        Round r = new Round();
        r.setIndex(delta / roundTimeDuration);
        r.setStartTimeStamp(genesisRoundTimeStamp + r.getIndex() * roundTimeDuration);

        return(r);
    }

    public long getSynchronizedTime(NTPClient ntpClient){
        if (ntpClient != null){
            return(ntpClient.currentTimeMillis());
        }

        return(System.currentTimeMillis());
    }
}
