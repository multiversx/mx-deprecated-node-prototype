package network.elrond.chronology;

import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class ChronologyServiceImpl implements ChronologyService {
    private final long roundTimeDuration;
    private static final Logger logger = LogManager.getLogger(ChronologyServiceImpl.class);
    private long referenceRoundIndex = -1;
    private long referenceRoundTimestamp;
    private NTPClient ntpClient = null;

    public ChronologyServiceImpl() {
        roundTimeDuration = 4000; //4 seconds
    }

    public ChronologyServiceImpl(long roundTimeDuration) throws IllegalArgumentException {
        Util.check(roundTimeDuration > 0, "roundTimeDuration must be a strict positive number!");

        this.roundTimeDuration = roundTimeDuration;
    }

    public long getRoundTimeDuration() {
        return (roundTimeDuration);
    }

    public void setReferenceRound(long referenceRoundTimestamp, long referenceRoundIndex) {
        this.referenceRoundTimestamp = referenceRoundTimestamp;
        this.referenceRoundIndex = referenceRoundIndex;
    }

    public boolean isDateTimeInRound(Round round, long timeStamp) throws IllegalArgumentException {
        Util.check(round != null, "round should not be null!");

        return ((round.getStartTimeStamp() <= timeStamp) && (timeStamp < round.getStartTimeStamp() + roundTimeDuration));
    }

    public Round getRoundFromDateTime(long timeStamp) throws IllegalArgumentException {
        logger.traceEntry("params:{}", timeStamp);
        Round r;
        Util.check(timeStamp >= referenceRoundTimestamp, "referenceRoundTimestamp should be lower or equal to dateMillis!");

        if (referenceRoundIndex != -1) {
            long delta = timeStamp - referenceRoundTimestamp;

            r = new Round();
            r.setIndex((delta / roundTimeDuration) + referenceRoundIndex);
            r.setStartTimeStamp(referenceRoundTimestamp + (r.getIndex() - referenceRoundIndex) * roundTimeDuration);
        } else {
            // in case referenceRoundTimestamp not yet set
            r = new Round();
            r.setStartTimeStamp(timeStamp);
            r.setIndex(0);
            referenceRoundTimestamp = timeStamp;
            referenceRoundIndex = 0;
        }

        return logger.traceExit(r);
    }

    public long getSynchronizedTime() {
        logger.traceEntry();
        if (ntpClient != null) {
            return (ntpClient.currentTimeMillis());
        }

        logger.trace("NTP client is null, returning system's clock.");
        return logger.traceExit(System.currentTimeMillis());
    }

    public RoundState computeRoundState(long roundStartTimeStamp, long currentTimeStamp) {
        logger.traceEntry("params: {} {}", roundStartTimeStamp, currentTimeStamp);
        Set<RoundState> setRoundState = RoundState.getEnumSet();

        long cumulatedTime = 0;

        for (RoundState roundState : setRoundState) {
            boolean isRoundStateTransitionNotSubrounds = (roundState == RoundState.START_ROUND) || (roundState == RoundState.END_ROUND);

            if (isRoundStateTransitionNotSubrounds) {
                continue;
            }

            boolean isCurrentTimeStampInSubRoundInterval = (cumulatedTime <= currentTimeStamp - roundStartTimeStamp) &&
                    (currentTimeStamp - roundStartTimeStamp < cumulatedTime + roundState.getRoundStateDuration());

            if (isCurrentTimeStampInSubRoundInterval) {
                return logger.traceExit(roundState);
            }

            cumulatedTime += roundState.getRoundStateDuration();
        }

        logger.trace("Round state not found!");
        return logger.traceExit((RoundState) null);
    }

    public void setNtpClient(NTPClient ntpClient) {
        this.ntpClient = ntpClient;
    }

    public NTPClient getNtpClient() {
        return ntpClient;
    }
}
