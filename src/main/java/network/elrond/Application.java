package network.elrond;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.processor.AppProcessors;

import java.io.IOException;
import java.io.Serializable;


public class Application implements Serializable {


    private AppState state = new AppState();
    private AppContext context;

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

    public void start() throws IOException {

        // Start P2P communications
        AppProcessors.BOOTSTRAP_P2P_CONNECTION.process(this);

        //  Start blockchain
        AppProcessors.BOOTSTRAP_BLOCKCHAIN.process(this);

        //  Start accounts
        AppProcessors.BOOTSTRAP_ACCOUNTS.process(this);

        // Intercept P2P transactions
        AppProcessors.P2P_TRANSACTIONS_INTERCEPTOR.process(this);

        // Intercept P2P blocks
        AppProcessors.P2P_BLOCKS_INTERCEPTOR.process(this);

        // Start bootstrapping process
        AppProcessors.BOOTSTRAP_SYSTEM.process(this);

    }

    public void stop() {
        this.state.setStillRunning(false);
    }


}
