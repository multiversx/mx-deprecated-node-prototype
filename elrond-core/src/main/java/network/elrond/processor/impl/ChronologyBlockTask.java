package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.chronology.ChronologyService;
import network.elrond.chronology.Round;
import network.elrond.chronology.SubRound;
import network.elrond.chronology.RoundState;
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

            long genesisTime = Long.MIN_VALUE;

            AppState state = application.getState();
            while (state.isStillRunning()) {
                ThreadUtil.sleep(1);

                //check if there is a genesis block (otherwise can not compute current round)
                if (genesisTime == Long.MIN_VALUE) {
                    boolean isGenesisBlockAbsent = (application == null) || (application.getState() == null) ||
                            (application.getState().getBlockchain() == null) || (application.getState().getBlockchain().getGenesisBlock() == null);

                    if (isGenesisBlockAbsent) {
                        //Periodically (but not to often) push a log message
                        logger.warn("No genesis block, can not compute current round! Waiting 1s and retrying...");

                        ThreadUtil.sleep(1000);

                        continue;
                    } else{
                        genesisTime = application.getState().getBlockchain().getGenesisBlock().getTimestamp();
                    }
                }

                long currentTime = chronologyService.getSynchronizedTime(state.getNtpClient());

                currentRound = chronologyService.getRoundFromDateTime(genesisTime, currentTime);

                computeAndCallStartEndRounds(application, currentRound);
                computeAndCallRoundState(application, currentRound, currentTime);
            }
        });

        thread.start();
    }

    private void computeAndCallStartEndRounds(Application application, Round currentRound){
        if ((previousRound == null) || (previousRound.getIndex() != currentRound.getIndex())){
            //round transition

            if (previousRound != null){
                //close last round
                notifyEventObjects(application, previousRound, RoundState.END_ROUND);
            }

            //start new round
            notifyEventObjects(application, currentRound, RoundState.START_ROUND);
        }

        previousRound = currentRound;
    }

    private void computeAndCallRoundState(Application application, Round currentRound, long currentTime){
        RoundState currentRoundState = computeRoundState(currentRound.getStartRoundMillis(), currentTime);

        if (currentRoundState == null){
            //nothing
            return;
        }

        if ((previousRoundState == null) || (previousRoundState != currentRoundState)) {
            //round state transition
            notifyEventObjects(application, currentRound, currentRoundState);
        }

        previousRoundState = currentRoundState;
    }


    private void notifyEventObjects(Application application, Round round, RoundState roundState){
        SubRound subRound = new SubRound();
        subRound.setRound(round);
        subRound.setRoundState(roundState);

        for (EventHandler eventHandler:MAIN_QUEUE){
            eventHandler.onEvent(application,this, subRound);
        }
    }

    RoundState computeRoundState(long roundStartTimeStamp, long currentTimeStamp){
        Set<RoundState> setRoundState = RoundState.getEnumSet();

        long cumulatedTime = 0;

        for (RoundState roundState : setRoundState){
            boolean isRoundStartOrRoundEnd = (roundState == RoundState.START_ROUND) || (roundState == RoundState.END_ROUND);

            if (isRoundStartOrRoundEnd){
                continue;
            }

            boolean isCurrentTimeStampInSubRoundInterval = (cumulatedTime <= currentTimeStamp - roundStartTimeStamp) &&
                    (currentTimeStamp - roundStartTimeStamp < cumulatedTime + roundState.getRoundStateMillis());

            if (isCurrentTimeStampInSubRoundInterval){
                return(roundState);
            }

            cumulatedTime += roundState.getRoundStateMillis();
        }

        return (null);
    }


}
