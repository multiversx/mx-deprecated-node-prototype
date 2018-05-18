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
        AppProcessors.P2P_CONNECTION_STARTER.process(this);

        // Intercept P2P transactions
        AppProcessors.P2P_TRANSACTIONS_INTERCEPTOR.process(this);

        // Intercept P2P blocks
        AppProcessors.P2P_BLOCKS_INTERCEPTOR.process(this);

        //process blocks
        AppProcessors.BLOCKS_PROCESSOR.process(this);
    }

    public void stop() {
        this.state.setStillRunning(false);
    }


}
