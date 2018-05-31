package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;

import java.math.BigInteger;
import java.util.Scanner;

public class SeedNodeRunner {


    public static void main(String[] args) throws Exception {

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4000);
        context.setStorageBasePath("./master");
        context.setNodeName("master-node");
        PrivateKey privateKey1 = new PrivateKey("026c00d83e0dc47e6b626ed6c42f636b");
        PublicKey publicKey = new PublicKey(privateKey1);

        context.setPrivateKey(privateKey1);
        String mintAddress = Util.getAddressFromPublicKey(publicKey.getValue());
        context.setStrAddressMint(mintAddress);

        context.setValueMint(Util.VALUE_MINTING);


        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);


        Thread thread = new Thread(() -> {

            do {

                AccountAddress address = AccountAddress.fromHexaString("0326e7875aadaba270ae93ec40ef4706934d070eb21c9acad4743e31289fa4ebc7");
                facade.send(address, BigInteger.TEN, application);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            } while (true);

        });
        thread.start();


        @SuppressWarnings("resource")
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            if (input.nextLine().equals("exit")) {
                facade.stop(application);
            }
        }

    }
}
