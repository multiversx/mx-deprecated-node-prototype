package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.processor.AppTask;

import java.io.IOException;

public class AccountInitializerProcessor implements AppTask {

    @Override
    public void process(Application application) throws IOException {

        AppContext context = application.getContext();
        AppState state = application.getState();
        PrivateKey privateKey = context.getPrivateKey();

        state.setPrivateKey(privateKey);
        state.setPublicKey(new PublicKey(privateKey.getValue()));
    }
}
