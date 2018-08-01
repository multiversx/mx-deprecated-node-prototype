package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.Round;
import network.elrond.chronology.RoundState;
import network.elrond.consensus.handlers.StartRoundHandler;
import network.elrond.consensus.handlers.SyncRoundHandler;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.processor.AppTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChronologyBlockTask implements AppTask {
    private static final Logger logger = LogManager.getLogger(ChronologyBlockTask.class);

    private Round previousRound = null;
    private RoundState previousRoundState = null;

    @Override
    public void process(Application application) {
        AppState state = application.getState();
        Util.check(state != null, "state != null");
        Util.check(state.getBlockchain() != null, "blockchain != null");

        Blockchain blockchain = state.getBlockchain();
        String nodeName = application.getContext().getNodeName();

        Thread thread = new Thread(() -> {
            logger.traceEntry();

            long genesisTimeStampCached = Long.MIN_VALUE;

            EventHandler currentSubRound = new StartRoundHandler(0);

            while (state.isStillRunning()) {
                //check if there is a genesis block (otherwise can not compute current round)
                if (genesisTimeStampCached == Long.MIN_VALUE) {
                    logger.trace("genesis timestamp is not initialized...");

                    boolean isGenesisBlockAbsent = blockchain.getGenesisBlock() == null;

                    if (isGenesisBlockAbsent) {
                        //Periodically (but not to often) push a log message
                        logger.info("No genesis block, can not compute current round! Waiting 1s and retrying...");

                        try {
                            AppServiceProvider.getBootstrapService().fetchNetworkBlockIndex(state.getBlockchain());

                            SyncRoundHandler syncRoundHandler = new SyncRoundHandler(0);
                            syncRoundHandler.execute(state, 0);


                        } catch (Exception ex) {
                            logger.catching(ex);
                        }

                        ThreadUtil.sleep(1000);

                        continue;
                    } else {
                        genesisTimeStampCached = state.getBlockchain().getGenesisBlock().getTimestamp();
                        logger.trace("Cached genesis time stamp as: {}", genesisTimeStampCached);
                    }
                }

                try {
                    currentSubRound = currentSubRound.execute(state, genesisTimeStampCached);
                } catch (Exception ex) {
                    logger.catching(ex);
                    ThreadUtil.sleep(100);
                }
            }
        });

        thread.start();
    }
}
