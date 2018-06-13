package network.elrond.chronology;

import java.math.BigInteger;

public interface ChronologyService {
    long getMilisecondsInEpoch();

    boolean isDateTimeInEpoch(Epoch epoch, long dateMs) throws NullPointerException;

    Round getRoundFromDateTime(Epoch epoch, long dateMs) throws NullPointerException, IllegalArgumentException;

    Epoch generateNewEpoch(Epoch previousEpoch) throws NullPointerException;
}
