package network.elrond;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.processor.AppTasks;
import network.elrond.processor.impl.SynchronizationBlockTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;


public class Application implements Serializable {

    private static final Logger logger = LogManager.getLogger(Application.class);

    private AppContext context;

    private AppState state = new AppState();

    public Application(AppContext context) {
        logger.traceEntry("params: {}", context);
        if (context == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Context cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.context = context;
        logger.traceExit();
    }

    public AppContext getContext() {
        return context;
    }

    public void setContext(AppContext context) {
        logger.traceEntry("params: {}", context);
        if (context == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Context cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.context = context;
        logger.traceExit();
    }

    public AppState getState() {
        return state;
    }

    public void setState(AppState state) {
        logger.traceEntry("params: {}", state);
        if (state == null) {
            IllegalArgumentException ex = new IllegalArgumentException("State cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.state = state;
        logger.traceExit();
    }

    /**
     * Start Elrond application
     *
     * @throws IOException
     */
    public void start() throws IOException {
        logger.traceEntry();

        logger.trace("Starting P2P communications...");
        AppTasks.INIT_P2P_CONNECTION.process(this);

        logger.trace("Starting blockchain...");
        AppTasks.INIT_BLOCKCHAIN.process(this);

        logger.trace("Starting private-public keys processor...");
        AppTasks.INITIALIZE_PUBLIC_PRIVATE_KEYS.process(this);

        logger.trace("Starting accounts...");
        AppTasks.INIT_ACCOUNTS.process(this);

        logger.trace("Starting bootstrapping processor...");
        AppTasks.BLOCKCHAIN_BOOTSTRAP.process(this);

        logger.trace("Starting blockchain synchronization...");
        AppTasks.BLOCKCHAIN_SYNCRONIZATION.process(this);

        logger.trace("Intercept P2P transactions...");
        AppTasks.INTERCEPT_TRANSACTIONS.process(this);

        logger.trace("Intercept P2P receipts...");
        AppTasks.INTERCEPT_RECEIPTS.process(this);

        logger.trace("Intercept P2P blocks...");
        AppTasks.INTERCEPT_BLOCKS.process(this);

        //logger.trace("Execute transactions and emit blocks...");
        //AppTasks.BLOCK_ASSEMBLY_PROCESSOR.process(this);

        logger.trace("Init NTP client...");
        AppTasks.NTP_CLIENT_INITIALIZER.process(this);
        
        logger.trace("Start chronology processor...");
        AppTasks.CHRONOLOGY.process(this);
    }

    /**
     * Stop Elrond application
     */
    public void stop() {
        this.state.setStillRunning(false);
        this.state.getConnection().getDht().shutdown();
        this.state.getConnection().getPeer().shutdown();
    }


}
