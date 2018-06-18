package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.chronology.*;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.service.AppServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChronologyBlockTask extends AbstractBlockTask {

    public static Queue<EventHandler> MAIN_QUEUE = new ConcurrentLinkedQueue<>();
    private Logger logger = LoggerFactory.getLogger(ChronologyBlockTask.class);
    private Round previousRound = null;
    private RoundState previousRoundState = null;

    @Override
    protected void doProcess(Application application) {
        Thread thread = new Thread(() -> {
            Round currentRound = null;

            ChronologyService chronologyService = AppServiceProvider.getChronologyService();

            long genesisTimeStampCached = Long.MIN_VALUE;

            //TESTING PURPOSES!!!
            MAIN_QUEUE.add(new SubRoundEventHandler());

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

                computeAndCallStartEndRounds(application, currentRound);
                computeAndCallRoundState(application, currentRound, currentTimeStamp);
            }
        });

        thread.start();
    }

    private void computeAndCallStartEndRounds(Application application, Round currentRound){
        boolean isFirstRoundTransition = (previousRound == null);
        boolean isRoundTransition = isFirstRoundTransition || (previousRound.getIndex() != currentRound.getIndex());
        boolean existsPreviousRound = (previousRound != null);

        if (isRoundTransition){
            if (existsPreviousRound){
                notifyEventObjects(application, previousRound, RoundState.END_ROUND);
            }

            //start new round
            notifyEventObjects(application, currentRound, RoundState.START_ROUND);
        }

        previousRound = currentRound;
    }

    private void computeAndCallRoundState(Application application, Round currentRound, long currentTime){
        RoundState currentRoundState = computeRoundState(currentRound.getStartTimeStamp(), currentTime);

        boolean isCurrentRoundStateNotDefined = (currentRoundState == null);

        if (isCurrentRoundStateNotDefined){
            return;
        }

        boolean isFirstRoundStateTransition = (previousRoundState == null);
        boolean isRoundStateTransition = isFirstRoundStateTransition || (previousRoundState != currentRoundState);

        if (isRoundStateTransition) {
            notifyEventObjects(application, currentRound, currentRoundState);
        }

        previousRoundState = currentRoundState;
    }


    private void notifyEventObjects(Application application, Round round, RoundState roundState){
        SubRound subRound = new SubRound();
        subRound.setRound(round);
        subRound.setRoundState(roundState);

        logger.info(String.format("ChronologyBlockTask event %s, %s", round.toString(), roundState.toString()));

        for (EventHandler eventHandler:MAIN_QUEUE){
            eventHandler.onEvent(application,this, subRound);
        }
    }

    RoundState computeRoundState(long roundStartTimeStamp, long currentTimeStamp){
        Set<RoundState> setRoundState = RoundState.getEnumSet();

        long cumulatedTime = 0;

        for (RoundState roundState : setRoundState){
            boolean isRoundStateTransitionNotSubrounds = (roundState == RoundState.START_ROUND) || (roundState == RoundState.END_ROUND);

            if (isRoundStateTransitionNotSubrounds){
                continue;
            }

            boolean isCurrentTimeStampInSubRoundInterval = (cumulatedTime <= currentTimeStamp - roundStartTimeStamp) &&
                    (currentTimeStamp - roundStartTimeStamp < cumulatedTime + roundState.getRoundStateDuration());

            if (isCurrentTimeStampInSubRoundInterval){
                return(roundState);
            }

            cumulatedTime += roundState.getRoundStateDuration();
        }

        return (null);
    }
}
