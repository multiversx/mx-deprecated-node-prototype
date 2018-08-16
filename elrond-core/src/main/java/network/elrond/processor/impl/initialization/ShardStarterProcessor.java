package network.elrond.processor.impl.initialization;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.processor.AppTask;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ShardStarterProcessor implements AppTask {
    private static final Logger logger = LogManager.getLogger(ShardStarterProcessor.class);

    @Override
    public void process(Application application) {
        logger.traceEntry("params: {}", application);

        AppState state = application.getState();
        Shard shard = AppServiceProvider.getShardingService().getShard(state.getPublicKey().getValue());
        state.setShard(shard);
        logger.traceExit();
    }
}
