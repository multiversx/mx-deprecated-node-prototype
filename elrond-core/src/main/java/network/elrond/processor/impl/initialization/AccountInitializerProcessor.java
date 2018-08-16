package network.elrond.processor.impl.initialization;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.crypto.PrivateKey;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AccountInitializerProcessor implements AppTask {
    private static final Logger logger = LogManager.getLogger(AccountInitializerProcessor.class);

    @Override
    public void process(Application application) {
        logger.traceEntry("params: {}", application);

        AppContext context = application.getContext();
        AppState state = application.getState();
        PrivateKey privateKey = context.getPrivateKey();

        state.setPrivateKey(privateKey);

        logger.traceExit();
    }
}
