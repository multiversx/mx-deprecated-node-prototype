package network.elrond.processor.impl.initialization;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.p2p.*;
import network.elrond.processor.AppTask;
import network.elrond.processor.impl.AbstractChannelTask;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class P2PRequestObjectStarterProcessor implements AppTask {
    private static final Logger logger = LogManager.getLogger(AbstractChannelTask.class);

    @Override
    public void process(Application application) {
        logger.traceEntry("params: {}", application);

        AppState state = application.getState();
        Shard shard = state.getShard();

        P2PConnection connection = state.getConnection();

        for (P2PRequestChannelName requestChannel : P2PRequestChannelName.values()) {
            P2PRequestChannel channel = AppServiceProvider.getP2PRequestService().createChannel(connection, shard, requestChannel);
            RequestHandler requestHandler = requestChannel.getHandler();

            channel.setHandler(request -> requestHandler.onRequest(state, request));
            state.addChannel(channel);
            logger.info("added request handler for {}", requestChannel);
        }
        logger.traceExit();
    }
}
