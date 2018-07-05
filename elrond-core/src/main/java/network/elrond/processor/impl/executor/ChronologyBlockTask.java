package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.*;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.data.SyncState;
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
            Round currentRound = null;

            ChronologyService chronologyService = AppServiceProvider.getChronologyService();

            long genesisTimeStampCached = Long.MIN_VALUE;

            while (state.isStillRunning()) {
                ThreadUtil.sleep(1);

                //check if there is a genesis block (otherwise can not compute current round)
                if (genesisTimeStampCached == Long.MIN_VALUE) {
                    logger.trace("genesis timestamp is not initialized...");

                    boolean isGenesisBlockAbsent = blockchain.getGenesisBlock() == null;

                    if (isGenesisBlockAbsent) {
                        //Periodically (but not to often) push a log message
                        logger.info("No genesis block, can not compute current round! Waiting 1s and retrying...");

                        ThreadUtil.sleep(1000);

                        continue;
                    } else{
                        genesisTimeStampCached = state.getBlockchain().getGenesisBlock().getTimestamp();
                        logger.trace("Cached genesis time stamp as: {}", genesisTimeStampCached);
                    }
                }

                try {
                    SyncState syncState = AppServiceProvider.getBootstrapService().getSyncState(blockchain);

                    if (syncState.isSyncRequired()){
                        continue;
                    }

                    synchronized (state.lockerSyncPropose) {
                        long globalTimeStamp = chronologyService.getSynchronizedTime(state.getNtpClient());

                        currentRound = chronologyService.getRoundFromDateTime(genesisTimeStampCached, globalTimeStamp);

                        computeAndCallStartEndRounds(application, currentRound, globalTimeStamp);
                        computeAndCallRoundState(application, currentRound, globalTimeStamp);
                    }
                } catch (Exception ex){
                    logger.catching(ex);
                }
            }

            logger.traceExit();
        });
        thread.start();
    }

    private void computeAndCallStartEndRounds(Application application, Round currentRound, long globalTimeStamp){
        boolean isFirstRoundTransition = (previousRound == null);
        boolean isRoundTransition = isFirstRoundTransition || (previousRound.getIndex() != currentRound.getIndex());
        boolean existsPreviousRound = (previousRound != null);

        if (isRoundTransition){
            logger.trace("round transition detected!");
            if (existsPreviousRound){
                notifyEventObjects(application, previousRound, RoundState.END_ROUND, globalTimeStamp);
            }

            //start new round
            notifyEventObjects(application, currentRound, RoundState.START_ROUND, globalTimeStamp);

            previousRoundState = RoundState.START_ROUND;
        }

        previousRound = currentRound;
    }

    private void computeAndCallRoundState(Application application, Round currentRound, long globalTimeStamp){
        long startTimeStamp = currentRound.getStartTimeStamp();

        RoundState currentRoundState = AppServiceProvider.getChronologyService().computeRoundState(startTimeStamp, globalTimeStamp);

        boolean isCurrentRoundStateNotDefined = (currentRoundState == null);

        if (isCurrentRoundStateNotDefined){
            return;
        }

        boolean isFirstRoundStateTransition = (previousRoundState == null);
        boolean isRoundStateTransition = isFirstRoundStateTransition || (previousRoundState != currentRoundState);

        if (isRoundStateTransition) {
            logger.trace("round state transition detected!");
            notifyEventObjects(application, currentRound, currentRoundState, globalTimeStamp);
        }

        previousRoundState = currentRoundState;
    }


    private void notifyEventObjects(Application application, Round round, RoundState roundState, long globalTimeStamp){
        SubRound subRound = new SubRound();
        subRound.setRound(round);
        subRound.setRoundState(roundState);
        subRound.setTimeStamp(globalTimeStamp);

        AppState state = application.getState();

        logger.debug("notifyEventObjects event {}, {}, {}", round.toString(), roundState.toString(), globalTimeStamp);

        EventHandler eventHandler = roundState.getEventHandler();
        if (eventHandler != null){
            logger.trace("calling default event handler object (from enum)...");
            try {
                eventHandler.onEvent(state, subRound);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        } else {
            logger.warn("{} does not have an associated event handler!", roundState);
        }
    }
}
