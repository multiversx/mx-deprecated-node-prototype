package network.elrond.chronology;

import network.elrond.account.AbstractPersistenceUnit;
import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class ChronologyServiceImpl implements ChronologyService {
    private final long roundTimeDuration;

    private static final Logger logger = LogManager.getLogger(ChronologyServiceImpl.class);

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
        logger.traceEntry("params: {} {}", genesisRoundTimeStamp, timeStamp);
        long delta = timeStamp - genesisRoundTimeStamp;

        Util.check(timeStamp >= genesisRoundTimeStamp, "genesisRoundTimeStamp should be lower or equal to dateMillis!");

        Round r = new Round();
        r.setIndex(delta / roundTimeDuration);
        r.setStartTimeStamp(genesisRoundTimeStamp + r.getIndex() * roundTimeDuration);

        return logger.traceExit(r);
    }

    public long getSynchronizedTime(NTPClient ntpClient){
        logger.traceEntry();
        if (ntpClient != null){
            return(ntpClient.currentTimeMillis());
        }

        logger.trace("NTP client is null, returning system's clock.");
        return logger.traceExit(System.currentTimeMillis());
    }

    public RoundState computeRoundState(long roundStartTimeStamp, long currentTimeStamp){
        logger.traceEntry("params: {} {}", roundStartTimeStamp, currentTimeStamp);
        Set<RoundState> setRoundState = RoundState.getEnumSet();

        long cumulatedTime = 0;

        for (RoundState roundState : setRoundState){
            boolean isRoundStateTransitionNotSubrounds = (roundState == RoundState.START_ROUND) || (roundState == RoundState.END_ROUND);

            if (isRoundStateTransitionNotSubrounds){
                continue;
            }

            boolean isCurrentTimeStampInSubRoundInterval = (cumulatedTime <= currentTimeStamp - roundStartTimeStamp) &&
                    (currentTimeStamp - roundStartTimeStamp < cumulatedTime + roundState.getRoundStateDuration());

            if (isCurrentTimeStampInSubRoundInterval){
                return logger.traceExit(roundState);
            }

            cumulatedTime += roundState.getRoundStateDuration();
        }

        logger.trace("Round state not found!");
        return logger.traceExit((RoundState)null);
    }
}
