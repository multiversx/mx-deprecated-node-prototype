package network.elrond.p2p.handlers;

import network.elrond.application.AppState;
import network.elrond.benchmark.StatisticsManager;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatisticsRequestHandler implements RequestHandler<StatisticsManager, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(StatisticsRequestHandler.class);

    @Override
    public StatisticsManager onRequest(AppState state, P2PRequestMessage data) {
        logger.traceEntry("params: {} {}", state, data);

        StatisticsManager statisticsManager = state.getStatisticsManager();
        return logger.traceExit(statisticsManager);
    }
}

