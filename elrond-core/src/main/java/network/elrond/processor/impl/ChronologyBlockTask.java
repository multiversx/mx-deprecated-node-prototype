package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.chronology.*;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.data.SynchronizationRequirement;
import network.elrond.p2p.AppP2PManager;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.AppTask;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChronologyBlockTask<T> implements AppTask {

    public static Queue<EventHandler> MAIN_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Logger logger = LogManager.getLogger(ChronologyBlockTask.class);

    private Round previousRound = null;
    private RoundState previousRoundState = null;

    @Override
    public void process(Application application) {
        ArrayBlockingQueue<T> queueTransactionHashes = AppP2PManager.instance().subscribeToChannel(application, P2PChannelName.TRANSACTION);

        AppState state = application.getState();
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

                    boolean isGenesisBlockAbsent = (application == null) || (application.getState() == null) ||
                            (application.getState().getBlockchain() == null) || (application.getState().getBlockchain().getGenesisBlock() == null);

                    if (isGenesisBlockAbsent) {
                        //Periodically (but not to often) push a log message
                        logger.info("No genesis block, can not compute current round! Waiting 1s and retrying...");

                        ThreadUtil.sleep(1000);

                        continue;
                    } else{
                        genesisTimeStampCached = application.getState().getBlockchain().getGenesisBlock().getTimestamp();
                        logger.trace(String.format("Cached genesis time stamp as: {}", genesisTimeStampCached));
                    }
                }

                try {
                    SynchronizationRequirement synchronizationRequirement = AppServiceProvider.getBootstrapService().getSynchronizationRequirement(blockchain);

                    if (synchronizationRequirement.isSyncRequired()){
                        continue;
                    }

                    synchronized (state.lockerSyncPropose) {
                        long currentTimeStamp = chronologyService.getSynchronizedTime(state.getNtpClient());

                        currentRound = chronologyService.getRoundFromDateTime(genesisTimeStampCached, currentTimeStamp);

                        computeAndCallStartEndRounds(application, currentRound, currentTimeStamp, queueTransactionHashes);
                        computeAndCallRoundState(application, currentRound, currentTimeStamp, queueTransactionHashes);

                    }
                } catch (Exception ex){
                    logger.catching(ex);
                }
            }

            logger.traceExit();
        });
        thread.setPriority(7);
        thread.start();
    }

    private void computeAndCallStartEndRounds(Application application, Round currentRound, long referenceTimeStamp, ArrayBlockingQueue<T> queueTransactionHashes){
        boolean isFirstRoundTransition = (previousRound == null);
        boolean isRoundTransition = isFirstRoundTransition || (previousRound.getIndex() != currentRound.getIndex());
        boolean existsPreviousRound = (previousRound != null);

        if (isRoundTransition){
            logger.trace("round transition detected!");
            if (existsPreviousRound){
                notifyEventObjects(application, previousRound, RoundState.END_ROUND, referenceTimeStamp, queueTransactionHashes);
            }

            //start new round
            notifyEventObjects(application, currentRound, RoundState.START_ROUND, referenceTimeStamp, queueTransactionHashes);
        }

        previousRound = currentRound;
    }

    private void computeAndCallRoundState(Application application, Round currentRound, long currentTime, ArrayBlockingQueue<T> queueTransactionHashes){
        RoundState currentRoundState = AppServiceProvider.getChronologyService().computeRoundState(currentRound.getStartTimeStamp(), currentTime);

        boolean isCurrentRoundStateNotDefined = (currentRoundState == null);

        if (isCurrentRoundStateNotDefined){
            return;
        }

        boolean isFirstRoundStateTransition = (previousRoundState == null);
        boolean isRoundStateTransition = isFirstRoundStateTransition || (previousRoundState != currentRoundState);

        if (isRoundStateTransition) {
            logger.trace("round state transition detected!");
            notifyEventObjects(application, currentRound, currentRoundState, currentTime, queueTransactionHashes);
        }

        previousRoundState = currentRoundState;
    }


    private void notifyEventObjects(Application application, Round round, RoundState roundState, long referenceTimeStamp, ArrayBlockingQueue<T> queueTransactionHashes){
        SubRound subRound = new SubRound();
        subRound.setRound(round);
        subRound.setRoundState(roundState);
        subRound.setTimeStamp(referenceTimeStamp);

        application.getState().getConsensusStateHolder().setCurrentRoundIndex(round.getIndex());
        application.getState().getConsensusStateHolder().setCurrentRoundState(roundState);

        logger.debug("notifyEventObjects event {}, {}, {}", round.toString(), roundState.toString(), referenceTimeStamp);

        if (roundState.getEventHandler() != null){
            logger.trace("calling default event handler object (from enum)...");
            roundState.getEventHandler().onEvent(application, this, subRound, queueTransactionHashes);
        }

        logger.trace("calling {} registered objects...", MAIN_QUEUE.size());
        for (EventHandler eventHandler:MAIN_QUEUE){
            eventHandler.onEvent(application,this, subRound, queueTransactionHashes);
        }
    }


}
