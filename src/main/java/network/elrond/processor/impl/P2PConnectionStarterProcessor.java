package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.p2p.P2PConnection;
import network.elrond.processor.AppProcessor;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;

public class P2PConnectionStarterProcessor implements AppProcessor {

    @Override
    public void process(Application application) throws IOException {
        AppContext context = application.getContext();
        AppState state = application.getState();

        P2PConnection connection = AppServiceProvider.getP2PBroadcastService().createConnection(context);
        state.setConnection(connection);

    }

}
