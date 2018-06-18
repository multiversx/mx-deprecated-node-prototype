package network.elrond.chronology;

import network.elrond.core.Util;

import java.util.Arrays;
import java.util.List;

public class ChronologyServiceImpl implements ChronologyService {
    private final long roundTimeMillis;

//    private final List<String> listNTPServers = Arrays.asList("time.windows.com","time-a.nist.gov");
//    private NTPClient ntpClient = null;

    public ChronologyServiceImpl(){
        roundTimeMillis = 4000; //4 seconds
//        try {
//            ntpClient = new NTPClient(listNTPServers, 1000);
//        } catch (Exception ex) {
//            System.out.println("Error while instantiating ntpClient!");
//            ex.printStackTrace();
//        }
    }

    public ChronologyServiceImpl(long roundTimeMillis) throws IllegalArgumentException{
        Util.check(roundTimeMillis > 0, "roundTimeMillis must be a strict positive number!");

        this.roundTimeMillis = roundTimeMillis;
//        try {
//            ntpClient = new NTPClient(listNTPServers, 1000);
//        } catch (Exception ex) {
//            System.out.println("Error while instantiating ntpClient!");
//            ex.printStackTrace();
//        }
    }

    public long getRoundTimeMillis(){
        return(roundTimeMillis);
    }

    public boolean isDateTimeInRound(Round round, long dateMillis) throws IllegalArgumentException{
        Util.check(round != null, "round should not be null!");

        return((round.getStartRoundMillis() <= dateMillis) && (dateMillis < round.getStartRoundMillis() + roundTimeMillis));
    }

    public Round getRoundFromDateTime(long genesisRoundTimeStartMillis, long dateMillis) throws IllegalArgumentException{
        long delta = dateMillis - genesisRoundTimeStartMillis;

        Util.check(dateMillis >= genesisRoundTimeStartMillis, "genesisRoundTimeStartMillis should be lower or equal to dateMillis!");

        Round r = new Round();
        r.setIndex(delta / roundTimeMillis);
        r.setStartRoundMillis(genesisRoundTimeStartMillis + r.getIndex() * roundTimeMillis);

        return(r);
    }

    public long getSynchronizedTime(NTPClient ntpClient){
        if (ntpClient != null){
            return(ntpClient.currentTimeMillis());
        }

        return(System.currentTimeMillis());
    }

//    public List<String> getListNTPServers(){
//        return (listNTPServers);
//    }
//
//    public NTPClient getNtpClient(){
//        return(ntpClient);
//    }

}
