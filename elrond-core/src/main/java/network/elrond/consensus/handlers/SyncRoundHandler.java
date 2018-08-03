package network.elrond.consensus.handlers;

import network.elrond.application.AppState;
import network.elrond.chronology.ChronologyService;
import network.elrond.core.EventHandler;
import network.elrond.core.ThreadUtil;
import network.elrond.data.BootstrapService;
import network.elrond.data.ExecutionReport;
import network.elrond.data.SyncState;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SyncRoundHandler extends EventHandler {
    private static final Logger logger = LogManager.getLogger(SyncRoundHandler.class);

    public SyncRoundHandler(long currentRoundIndex){
        super(currentRoundIndex);
    }

    public EventHandler execute(AppState state, long genesisTimeStamp) {
        ChronologyService chronologyService = AppServiceProvider.getChronologyService();
        BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();

        logger.debug("Round: {}, subRound: {}> initialized!", currentRoundIndex, this.getClass().getName());

        SyncState syncState = bootstrapService.getSyncState(state.getBlockchain());

        if (!syncState.isValid()){
            ThreadUtil.sleep(100);
            logger.warn("SyncState not valid! Retrying...");
            return this;
        }

        logger.debug("Round: {}, subRound: {}> network height: {}, local height: {}!", currentRoundIndex, this.getClass().getName(),
                syncState.getRemoteBlockIndex(), syncState.getLocalBlockIndex());

        if (syncState.isSyncRequired()){
            ExecutionReport report = AppServiceProvider.getBootstrapService().synchronize(syncState.getLocalBlockIndex(), syncState.getRemoteBlockIndex(), state);
            logger.debug("Sync result: {}", report);

            if (!report.isOk()){
                ThreadUtil.sleep(100);
            }

            return this;
        } else {
            if (!isStillInRound(state, genesisTimeStamp)){
                logger.debug("Genesis: {}, current: {}, computed round: {}", genesisTimeStamp, state.getNtpClient().currentTimeMillis(),
                        chronologyService.getRoundFromDateTime(genesisTimeStamp, state.getNtpClient().currentTimeMillis()).getIndex());
                return new StartRoundHandler(0);
            }

            return new AssemblyBlockHandler(currentRoundIndex);
        }
    }
}
