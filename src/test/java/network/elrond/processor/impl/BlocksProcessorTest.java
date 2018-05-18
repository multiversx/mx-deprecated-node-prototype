package network.elrond.processor.impl;

import net.tomp2p.dht.FuturePut;
import network.elrond.Application;
import network.elrond.NodeRunnerInjector;
import network.elrond.application.AppContext;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.p2p.P2PBroadcastChanel;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class BlocksProcessorTest {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testBlocksProcessor() throws IOException {
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

        P2PBroadcastChanel channelTxs = app.getState().getChanel("TRANSACTIONS");
        P2PBroadcastChanel channelBlks = app.getState().getChanel("BLOCKS");

        TransactionService txServ = AppServiceProvider.getTransactionService();
        BlockService blkServ = AppServiceProvider.getBlockService();

        //define block
        Block blk = new DataBlock();
        blk.setShard(0);
        blk.setNonce(BigInteger.ONE);

        //create 10 transactions, add them to the block and broadcast them
        for (int i = 0; i < 10; i++){
            Transaction tx = new Transaction();
            tx.setPubKey(Util.byteArrayToHexString(pbKey.getEncoded()));
            tx.setSendAddress(Util.getAddressFromPublicKey(pbKey.getEncoded()));
            tx.setRecvAddress("0x0000000000000000000000000000000000000000");
            tx.setNonce(BigInteger.ZERO);
            tx.setValue(BigInteger.TEN.pow(8).add(BigInteger.valueOf(i))); //1 ERD

            txServ.signTransaction(tx, pvKey.getValue());
            blk.addTransaction(tx);

            String strHash = txServ.getHashAsString(tx, true);

            FuturePut fp = AppServiceProvider.getP2PObjectService().put(channelTxs.getConnection(),
                    strHash,
                    txServ.encodeJSON(tx, true));
            if (fp.isSuccess()) {
                logger.info("Put tx hash: " + strHash);
                AppServiceProvider.getP2PBroadcastService().publishToChannel(channelTxs, "H:" + strHash);
            }
        }

        String strHashBlk = blkServ.getHashAsString(blk,false);



        for (int i = 0; i < 10; i++) {
            logger.info("Blk cache size: " + app.getState().syncDataBlk.size());

            if (i == 3){
                //broadcast block
                FuturePut fpblk = AppServiceProvider.getP2PObjectService().put(channelBlks.getConnection(),strHashBlk,
                        blkServ.encodeJSON(blk, false));
                if (fpblk.isSuccess()){
                    logger.info("Put blk hash: " + strHashBlk);
                }
            }

            Object[] blks = app.getState().syncDataBlk.getValues().toArray();

            for (int j = 0; j < blks.length; j++){
                logger.info("Found blk hash: " + blkServ.getHashAsString((Block)blks[j], false) + " solved? " +
                        ((Block)blks[j]).getIsSolved());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
