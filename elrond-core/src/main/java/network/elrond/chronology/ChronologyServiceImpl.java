package network.elrond.chronology;

import network.elrond.core.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class ChronologyServiceImpl implements ChronologyService {
    private final long roundTimeDuration;

    private static final Logger logger = LogManager.getLogger(ChronologyServiceImpl.class);

    public ChronologyServiceImpl() {
        roundTimeDuration = 5000; //4 seconds
    }

    public ChronologyServiceImpl(long roundTimeDuration) throws IllegalArgumentException {
        Util.check(roundTimeDuration > 0, "roundTimeDuration must be a strict positive number!");

        this.roundTimeDuration = roundTimeDuration;
    }

    @Override
	public long getRoundTimeDuration() {
        return (roundTimeDuration);
    }

    @Override
	public boolean isDateTimeInRound(Round round, long timeStamp) throws IllegalArgumentException {
        Util.check(round != null, "round should not be null!");

        return ((round.getStartTimeStamp() <= timeStamp) && (timeStamp < round.getStartTimeStamp() + roundTimeDuration));
    }

    @Override
	public Round getRoundFromDateTime(long genesisRoundTimeStamp, long timeStamp) throws IllegalArgumentException {
        logger.traceEntry("params: {} {}", genesisRoundTimeStamp, timeStamp);
        long delta = timeStamp - genesisRoundTimeStamp;

        Util.check(timeStamp >= genesisRoundTimeStamp, "genesisRoundTimeStamp should be lower or equal to dateMillis!");

		long roundIndex = delta / roundTimeDuration;
		Round r = new Round(
				roundIndex,
				genesisRoundTimeStamp + roundIndex * roundTimeDuration);

        return logger.traceExit(r);
    }

    @Override
	public long getSynchronizedTime(NTPClient ntpClient) {
        logger.traceEntry();
        if (ntpClient != null) {
            return logger.traceExit(ntpClient.currentTimeMillis());
        }

        logger.trace("NTP client is null, returning system's clock.");
        return logger.traceExit(System.currentTimeMillis());
    }

    @Override
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

    @Override
	public synchronized boolean isStillInRoundState(NTPClient ntpClient, long genesisTimeStamp, long targetRoundIndex, RoundState targetRoundState) {
        logger.traceEntry("params: {} {} {} {}", ntpClient, genesisTimeStamp, targetRoundIndex, targetRoundState);
        Util.check(ntpClient != null, "NTP client should not be null!");

        long currentTimeStamp = ntpClient.currentTimeMillis();

        Round computedRound = getRoundFromDateTime(genesisTimeStamp, currentTimeStamp);

        boolean isRoundMismatch = computedRound.getIndex() != targetRoundIndex;

        if (isRoundMismatch) {
            logger.debug("Round mismatch genesisTimeStamp: {}, currentTimeStamp: {}, target roundIndex: {}, computed roundIndex: {} ",
                    genesisTimeStamp, currentTimeStamp, targetRoundIndex, computedRound.getIndex());
            return logger.traceExit(false);
        }

        RoundState computedRoundState = computeRoundState(computedRound.getStartTimeStamp(), currentTimeStamp);

        if (computedRoundState == null) {
            logger.debug("State round mismatch roundStartTimeStamp: {}, currentTimeStamp: {}, target roundState: {}, computed roundState: {} ",
                    computedRound.getStartTimeStamp(), currentTimeStamp, targetRoundState.name(), null);
            return logger.traceExit(false);
        }

        boolean isRoundStateMismatch = !computedRoundState.equals(targetRoundState);

        if (isRoundStateMismatch) {
            logger.debug("State round mismatch roundStartTimeStamp: {}, currentTimeStamp: {}, target roundState: {}, computed roundState: {} ",
                    computedRound.getStartTimeStamp(), currentTimeStamp, targetRoundState.name(), computedRoundState.name());
            return logger.traceExit(false);
        }

        return (logger.traceExit(true));
    }
}
