package network.elrond;

import network.elrond.application.AppContext;

import java.util.Scanner;

public class SeedNodeRunner {


    public static void main(String[] args) throws Exception {

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setStorageBasePath("./master");
        context.setNodeName("master-node");


        Application app = new Application(context);
        app.start();


        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            if (input.nextLine().equals("exit")) {
                app.stop();
            }
        }

    }
}
