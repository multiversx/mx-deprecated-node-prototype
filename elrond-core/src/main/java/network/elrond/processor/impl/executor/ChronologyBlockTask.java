package network.elrond.processor.impl.executor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.chronology.*;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.processor.AppTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChronologyBlockTask implements AppTask {

    public static Queue<EventHandler> MAIN_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Logger logger = LogManager.getLogger(ChronologyBlockTask.class);

    private Round previousRound = null;
    private RoundState previousRoundState = null;

    @Override
    public void process(Application application) {
        Thread thread = new Thread(() -> {
            logger.traceEntry();
            Round currentRound = null;

            ChronologyService chronologyService = AppServiceProvider.getChronologyService();

            long genesisTimeStampCached = Long.MIN_VALUE;

            AppState state = application.getState();
            while (state.isStillRunning()) {
                ThreadUtil.sleep(1);

                //check if there is a genesis block (otherwise can not compute current round)
                if (genesisTimeStampCached == Long.MIN_VALUE) {
                    logger.trace("genesis timestamp is not initialized...");

                    boolean isGenesisBlockAbsent = (application == null) || (application.getState() == null) ||
                            (application.getState().getBlockchain() == null) || (application.getState().getBlockchain().getGenesisBlock() == null);

                    if (isGenesisBlockAbsent) {
                        //Periodically (but not to often) push a log message
                        logger.info("No genesis block, can not compute current round! Waiting 1s and retrying...");

                        ThreadUtil.sleep(1000);

                        continue;
                    } else{
                        genesisTimeStampCached = application.getState().getBlockchain().getGenesisBlock().getTimestamp();
                        logger.trace(String.format("Cached genesis time stamp as: %d", genesisTimeStampCached));
                    }
                }

                long currentTimeStamp = chronologyService.getSynchronizedTime();

                currentRound = chronologyService.getRoundFromDateTime(genesisTimeStampCached, currentTimeStamp);

                computeAndCallStartEndRounds(currentRound, currentTimeStamp);
                computeAndCallRoundState(currentRound, currentTimeStamp);
            }

            logger.traceExit();
        });
        thread.start();
    }

    private void computeAndCallStartEndRounds(Round currentRound, long referenceTimeStamp){
        boolean isFirstRoundTransition = (previousRound == null);
        boolean isRoundTransition = isFirstRoundTransition || (previousRound.getIndex() != currentRound.getIndex());
        boolean existsPreviousRound = (previousRound != null);

        if (isRoundTransition){
            logger.trace("round transition detected!");
            if (existsPreviousRound){
                notifyEventObjects(previousRound, RoundState.END_ROUND, referenceTimeStamp);
            }

            //start new round
            notifyEventObjects(currentRound, RoundState.START_ROUND, referenceTimeStamp);
        }

        previousRound = currentRound;
    }

    private void computeAndCallRoundState(Round currentRound, long currentTime){
        RoundState currentRoundState = AppServiceProvider.getChronologyService().computeRoundState(currentRound.getStartTimeStamp(), currentTime);

        boolean isCurrentRoundStateNotDefined = (currentRoundState == null);

        if (isCurrentRoundStateNotDefined){
            return;
        }

        boolean isFirstRoundStateTransition = (previousRoundState == null);
        boolean isRoundStateTransition = isFirstRoundStateTransition || (previousRoundState != currentRoundState);

        if (isRoundStateTransition) {
            logger.trace("round state transition detected!");
            notifyEventObjects(currentRound, currentRoundState, currentTime);
        }

        previousRoundState = currentRoundState;
    }


    private void notifyEventObjects(Round round, RoundState roundState, long referenceTimeStamp){
        SubRound subRound = new SubRound();
        subRound.setRound(round);
        subRound.setRoundState(roundState);
        subRound.setTimeStamp(referenceTimeStamp);

        logger.trace("ChronologyBlockTask event {}, {}", round.toString(), roundState.toString());

        if (roundState.getEventHandler() != null){
            logger.trace("calling default event handler object (from enum)...");
            roundState.getEventHandler().onEvent(this, subRound);
        }

        logger.trace("calling %d registered objects...", MAIN_QUEUE.size());
        for (EventHandler eventHandler:MAIN_QUEUE){
            eventHandler.onEvent(this, subRound);
        }
    }


}
