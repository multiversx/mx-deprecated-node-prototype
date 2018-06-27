package network.elrond.processor.impl.interceptor;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.p2p.P2PChannelName;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.sharding.AppShardingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PConsensusInterceptorProcessor extends AbstractChannelTask<String> {
    private static final Logger logger = LogManager.getLogger(P2PConsensusInterceptorProcessor.class);

    @Override
    protected void process(String hash, Application application) {
        logger.traceEntry("params: {} {}", hash, application);
        AppState state = application.getState();
        try {

            AppShardingManager.instance().calculateAndSetRole(state);
            boolean isConsensusMember = AppShardingManager.instance().isLeader() || AppShardingManager.instance().isValidator();
            if (!isConsensusMember) {
                return;
            }



        } catch (Exception ex) {
            logger.catching(ex);
        }

        logger.traceExit();
    }

    @Override
    protected P2PChannelName getChannelName() {
        return P2PChannelName.CONSENSUS;
    }
}
