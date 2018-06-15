package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.chronology.NTPClient;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.processor.AppTask;

import java.io.IOException;

public class NtpClientInitializerProcessor implements AppTask {

    @Override
    public void process(Application application) throws IOException {

        AppContext context = application.getContext();
        AppState state = application.getState();

        try {
            state.setNtpClient(new NTPClient(context.getListNTPServers(), 1000));
        } catch (Exception ex) {
            System.out.println("Error while instantiating ntpClient!");
            ex.printStackTrace();
        }

    }
}