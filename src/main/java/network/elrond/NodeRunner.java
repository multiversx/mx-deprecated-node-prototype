package network.elrond;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.service.AppServiceProvider;

import java.util.Random;
import java.util.Scanner;

public class NodeRunner {


    public static void main(String[] args) throws Exception {

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setPeerId(0);


        Application app = new Application(context);
        app.start();


        Thread thread = new Thread(() -> {

            AppState state = app.getState();

            do {
                P2PBroadcastChanel chanel = state.getChanel("TRANSACTIONS");
                AppServiceProvider.getP2PBroadcastService().publishToChannel(chanel, new Random().nextInt());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            } while (state.isStillRunning());

        });
        thread.start();


        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            if (input.nextLine().equals("exit")) {
                app.stop();
            }
        }

    }
}
