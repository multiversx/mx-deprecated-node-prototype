package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.chronology.*;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.processor.AppTask;
import network.elrond.processor.AppTasks;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChronologyBlockTask implements AppTask {

    public static Queue<EventHandler> MAIN_QUEUE = new ConcurrentLinkedQueue<>();
    private Logger logger = LoggerFactory.getLogger(ChronologyBlockTask.class);
    private Round previousRound = null;
    private RoundState previousRoundState = null;

    @Override
    public void process(Application application) {
        Thread thread = new Thread(() -> {
            Round currentRound = null;

            ChronologyService chronologyService = AppServiceProvider.getChronologyService();

            long genesisTimeStampCached = Long.MIN_VALUE;

            AppState state = application.getState();
            while (state.isStillRunning()) {
                ThreadUtil.sleep(1);

                //check if there is a genesis block (otherwise can not compute current round)
                if (genesisTimeStampCached == Long.MIN_VALUE) {
                    boolean isGenesisBlockAbsent = (application == null) || (application.getState() == null) ||
                            (application.getState().getBlockchain() == null) || (application.getState().getBlockchain().getGenesisBlock() == null);

                    if (isGenesisBlockAbsent) {
                        //Periodically (but not to often) push a log message
                        logger.warn("No genesis block, can not compute current round! Waiting 1s and retrying...");

                        ThreadUtil.sleep(1000);

                        continue;
                    } else{
                        genesisTimeStampCached = application.getState().getBlockchain().getGenesisBlock().getTimestamp();
                        logger.info(String.format("Cached genesis time stamp as: %d", genesisTimeStampCached));
                    }
                }

                long currentTimeStamp = chronologyService.getSynchronizedTime(state.getNtpClient());

                currentRound = chronologyService.getRoundFromDateTime(genesisTimeStampCached, currentTimeStamp);

                computeAndCallStartEndRounds(application, currentRound, currentTimeStamp);
                computeAndCallRoundState(application, currentRound, currentTimeStamp);
            }
        });
        thread.start();
    }

    private void computeAndCallStartEndRounds(Application application, Round currentRound, long referenceTimeStamp){
        boolean isFirstRoundTransition = (previousRound == null);
        boolean isRoundTransition = isFirstRoundTransition || (previousRound.getIndex() != currentRound.getIndex());
        boolean existsPreviousRound = (previousRound != null);

        if (isRoundTransition){
            if (existsPreviousRound){
                notifyEventObjects(application, previousRound, RoundState.END_ROUND, referenceTimeStamp);
            }

            //start new round
            notifyEventObjects(application, currentRound, RoundState.START_ROUND, referenceTimeStamp);
        }

        previousRound = currentRound;
    }

    private void computeAndCallRoundState(Application application, Round currentRound, long currentTime){
        RoundState currentRoundState = AppServiceProvider.getChronologyService().computeRoundState(currentRound.getStartTimeStamp(), currentTime);

        boolean isCurrentRoundStateNotDefined = (currentRoundState == null);

        if (isCurrentRoundStateNotDefined){
            return;
        }

        boolean isFirstRoundStateTransition = (previousRoundState == null);
        boolean isRoundStateTransition = isFirstRoundStateTransition || (previousRoundState != currentRoundState);

        if (isRoundStateTransition) {
            notifyEventObjects(application, currentRound, currentRoundState, currentTime);
        }

        previousRoundState = currentRoundState;
    }


    private void notifyEventObjects(Application application, Round round, RoundState roundState, long referenceTimeStamp){
        SubRound subRound = new SubRound();
        subRound.setRound(round);
        subRound.setRoundState(roundState);
        subRound.setTimeStamp(referenceTimeStamp);

        logger.info(String.format("ChronologyBlockTask event %s, %s", round.toString(), roundState.toString()));

        //calling default event handler object
        if (roundState.getEventHandler() != null){
            roundState.getEventHandler().onEvent(application, this, subRound);
        }

        //calling registered objects
        for (EventHandler eventHandler:MAIN_QUEUE){
            eventHandler.onEvent(application,this, subRound);
        }
    }


}
