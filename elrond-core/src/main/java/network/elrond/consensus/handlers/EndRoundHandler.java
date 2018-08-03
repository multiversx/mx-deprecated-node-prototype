package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class EndRoundHandler extends EventHandler {

    private static final Logger logger = LogManager.getLogger(EndRoundHandler.class);

    public EndRoundHandler(long currentRoundIndex){
        super(currentRoundIndex);
    }

    @Override
    public EventHandler execute(AppState state, long genesisTimeStamp) {
        logger.info("Entered EndRoundHandler:" + System.currentTimeMillis() + " on: " + (new Date()).toString());
        while (isStillInRound(state, genesisTimeStamp)){
            ThreadUtil.sleep(10);
        }

        state.getStatisticsManager().updateNetworkStats(state);
        state.getStatisticsManager().processStatistic();

        return new StartRoundHandler(0);
    }
}
