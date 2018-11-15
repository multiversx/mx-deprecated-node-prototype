package network.elrond.chronology;

public interface ChronologyService {
    long getRoundTimeDuration();

    boolean isDateTimeInRound(Round round, long timeStamp) throws IllegalArgumentException;

    Round getRoundFromDateTime(long genesisRoundTimeStamp, long timeStamp) throws IllegalArgumentException;

    long getSynchronizedTime(NTPClient ntpClient);

    RoundState computeRoundState(long roundStartTimeStamp, long currentTimeStamp);

    boolean isStillInRoundState(NTPClient ntpClient, long genesisTimeStamp, long targetRoundIndex, RoundState targetRoundState);
}
