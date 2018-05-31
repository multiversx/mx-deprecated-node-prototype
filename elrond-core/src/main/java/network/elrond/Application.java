package network.elrond;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.processor.AppTasks;

import java.io.IOException;
import java.io.Serializable;


public class Application implements Serializable {

    private AppContext context;

    private AppState state = new AppState();

    public Application(AppContext context) {
        this.context = context;
    }

    public AppContext getContext() {
        return context;
    }

    public void setContext(AppContext context) {
        this.context = context;
    }

    public AppState getState() {
        return state;
    }

    public void setState(AppState state) {
        this.state = state;
    }

    /**
     * Start Elrond application
     *
     * @throws IOException
     */
    public void start() throws IOException {


        // Start P2P communications
        AppTasks.INIT_P2P_CONNECTION.process(this);

        //  Start blockchain
        AppTasks.INIT_BLOCKCHAIN.process(this);

        AppTasks.INITIALIZE_PUBLIC_PRIVATE_KEYS.process(this);

        //  Start accounts
        AppTasks.INIT_ACCOUNTS.process(this);


//        // Start bootstrapping process
//        AppTasks.BOOTSTRAP_SYSTEM.process(this);


        // Intercept P2P transactions
        AppTasks.INTERCEPT_TRANSACTIONS.process(this);
        // Intercept P2P blocks
        AppTasks.INTERCEPT_BLOCKS.process(this);


        // Execute transactions and emit block
        AppTasks.BLOCK_ASSEMBLY_PROCESSOR.process(this);

    }

    /**
     * Stop Elrond application
     */
    public void stop() {
        this.state.setStillRunning(false);
    }


}
