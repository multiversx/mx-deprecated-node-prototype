package network.elrond;

import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;

import java.math.BigInteger;

public class NodeRunner {


    public static void main(String[] args) throws Exception {


        String nodeName = "elrond-node-2";
        Integer port = 4001;
        Integer masterPeerPort = 31201;
        String masterPeerIpAddress = "192.168.11.131";
        String privateKey = "1df1e9456051e43cfa612ecafcfd145cc06c1fb64d7499ef34696ff16b82cbc2";

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress(masterPeerIpAddress);
        context.setMasterPeerPort(masterPeerPort);
        context.setPort(port);
        context.setStorageBasePath(nodeName);
        context.setNodeName(nodeName);
        PrivateKey privateKey1 = new PrivateKey(privateKey);
        PublicKey publicKey = new PublicKey(privateKey1);

        context.setPrivateKey(privateKey1);
        String mintAddress = Util.getAddressFromPublicKey(publicKey.getValue());
        context.setStrAddressMint(mintAddress);
        context.setValueMint(BigInteger.valueOf(21000000));


        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);


        Thread thread = new Thread(() -> {

            do {

                AccountAddress address = AccountAddress.fromHexString("03d880b1194236b1af84da37dae2bb207762a40cfc98576cb722025c945644ac7e");
                //facade.send(address, BigInteger.TEN, application);
                System.out.println(facade.getBalance(address, application));
                ThreadUtil.sleep(2000);


            } while (true);

        });
        thread.start();


    }
}
