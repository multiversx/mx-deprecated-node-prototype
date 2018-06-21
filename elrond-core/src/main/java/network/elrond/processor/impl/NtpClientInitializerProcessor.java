package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.chronology.NTPClient;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class NtpClientInitializerProcessor implements AppTask {
    private static final Logger logger = LogManager.getLogger(NtpClientInitializerProcessor.class);

    @Override
    public void process(Application application) throws IOException {
        logger.traceEntry("params: {}", application);

        AppContext context = application.getContext();
        AppState state = application.getState();

        try {
            logger.trace("creating NTP client object...");
            state.setNtpClient(new NTPClient(context.getListNTPServers(), 1000));
        } catch (Exception ex) {
            logger.catching(ex);
        }

        logger.traceExit();
    }
}