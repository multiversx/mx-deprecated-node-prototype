package network.elrond.chronology;

import java.math.BigInteger;
import java.util.List;

public interface ChronologyService {
    long getRoundTimeMillis();

    boolean isDateTimeInRound(Round round, long dateMs) throws IllegalArgumentException;

    Round getRoundFromDateTime(long genesisRoundTimeStartMilliseconds, long dateMs) throws IllegalArgumentException;

    long getSynchronizedTime(NTPClient ntpClient);

//    List<String> getListNTPServers();
//
//    NTPClient getNtpClient();
}
