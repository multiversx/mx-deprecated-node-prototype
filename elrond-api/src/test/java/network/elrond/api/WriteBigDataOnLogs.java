package network.elrond.api;
import network.elrond.core.ThreadUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WriteBigDataOnLogs {
    private static Logger logger = LogManager.getLogger(WriteBigDataOnLogs.class);

    public void testWrite() {
        String dataPrep = prepData();
        int percent = 0;
        int oldPercent = 0;
        for (int i = 0; i < 10000; i++) {
            logger.error("Write benchmark {}: {}", i, dataPrep);
            percent = i / 100;
            if (percent != oldPercent){
                System.out.println(String.format("Progress: %d percent", percent));
                oldPercent = percent;
            }
        }
        ThreadUtil.sleep(500000);
    }

    private String prepData(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[START SEQ] ");
        for (int i = 0; i < 700; i++){
            stringBuilder.append("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }
        stringBuilder.append(" [END SEQ]");
        return(stringBuilder.toString());
    }
}