package network.elrond;

import net.tomp2p.dht.FuturePut;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Transaction;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.p2p.P2PBroadcastServiceImpl;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

public class NodeProducerTX {

    public static void main(String[] args) throws Exception {

        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001);
        context.setPeerId(0);


        context.setEmitter(true);
        Application app = new Application(context);
        app.start();

        PrivateKey pvKey = new PrivateKey();
        PublicKey pbKey = new PublicKey(pvKey);

        Random rdm = new Random(10);

        Thread thread = new Thread(() -> {

            AppState state = app.getState();

            do {
                P2PBroadcastChanel chanel = state.getChanel("TRANSACTIONS");

                Transaction tx = new Transaction();

                byte[] buff = new byte[5];
                rdm.nextBytes(buff);

                tx.setData(buff);
                tx.setPubKey(Util.byteArrayToHexString(pbKey.getEncoded()));
                tx.setSendAddress(Util.getAddressFromPublicKey(pbKey.getEncoded()));
                tx.setRecvAddress("0x0000000000000000000000000000000000000000");
                tx.setNonce(BigInteger.ZERO);
                tx.setValue(BigInteger.TEN.pow(8)); //1 ERD

                tx.signTransaction(pvKey.getValue().toByteArray());

                String strHash = new String(Base64.encode(tx.getHash()));

                try {
                    FuturePut fp = P2PBroadcastServiceImpl.instance().put(chanel, strHash, tx.encodeJSON());
                    if (fp.isSuccess()) {
                        LoggerFactory.getLogger(NodeProducerTX.class).info("Put tx hash: " + strHash);
                        P2PBroadcastServiceImpl.instance().publishToChannel(chanel, "H:" + strHash);
                        //System.out.println(NodeProducerTX.class + " INFO Put tx hash: " + strHash);
                        //
                    } else {
                        //System.out.println(NodeProducerTX.class + " INFO Put tx hash: " + strHash);
                        LoggerFactory.getLogger(NodeProducerTX.class).error("Error placing tx! hash: " + strHash + "");
                    }

                } catch (Exception ex){

                }
              try {
                    Thread.sleep(10);
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
