package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;

import java.math.BigInteger;

public class SeedNodeRunner {


    public static void main(String[] args) throws Exception {


        String nodeName = "elrond-node-1";
        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String privateKey = "026c00d83e0dc47e6b626ed6c42f636b";

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(masterPeerIpAddress);
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setNodeName(nodeName);
        context.setStorageBasePath(nodeName);
        PrivateKey privateKey1 = new PrivateKey(privateKey);
        PublicKey publicKey = new PublicKey(privateKey1);

        context.setPrivateKey(privateKey1);
        String mintAddress = Util.getAddressFromPublicKey(publicKey.getValue());
        context.setStrAddressMint(mintAddress);
        context.setValueMint(Util.VALUE_MINTING);


        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);


        Thread thread = new Thread(() -> {

            do {

                AccountAddress address = AccountAddress.fromHexString("0326e7875aadaba270ae93ec40ef4706934d070eb21c9acad4743e31289fa4ebc7");
//                facade.send(address, BigInteger.TEN, application);
                System.out.println(facade.getBalance(address, application));

                ThreadUtil.sleep(10);


            } while (true);

        });
        thread.start();

    }
}
