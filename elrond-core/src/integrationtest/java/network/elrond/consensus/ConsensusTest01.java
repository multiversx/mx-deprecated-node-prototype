package network.elrond.consensus;

import network.elrond.Application;
import network.elrond.ContextCreator;
import network.elrond.ElrondFacade;
import network.elrond.ElrondFacadeImpl;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Block;
import network.elrond.data.BootstrapType;
import network.elrond.data.Transaction;
import network.elrond.p2p.AppP2PManager;
import network.elrond.p2p.P2PChannelName;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.slf4j.Marker;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class ConsensusTest01 {
    private static final Logger logger = LogManager.getLogger(Util.class);

    @Test
    public void consensusTestMain(){
        //Description:
        //   1. start a seeder
        //   2. start a runner
        //   3. emit a controlled set of transaction
        //   4. verify output of blocks

        //only seeder compose transactions (it has the sufficient funds!)

        //the first application should be the seeder's application!
        Application[] applicationsAvailable = new Application[]{
                startSeeder(),
                startRunner("elrond-runner-1", 4001, 4000),
                startRunner("elrond-runner-2", 4002, 4000)
        };

        ElrondFacade facade = new ElrondFacadeImpl();

        //wait to start each node
        ThreadUtil.sleep(10000);

        ArrayBlockingQueue<Block> queueBlocks = AppP2PManager.instance().subscribeToChannel(applicationsAvailable[1], P2PChannelName.BLOCK);

        for (int i = 0; i < 10; i++){
            AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
            Transaction transaction = facade.send(address, BigInteger.valueOf(i), applicationsAvailable[0]);

            sendToLog(Level.ERROR, "Sent tx ", transaction, " to ", Util.byteArrayToHexString(applicationsAvailable[0].getContext().getPublicKey().getValue()));



            ThreadUtil.sleep(4000);
        }

        //stopping apps
        for (Application application:applicationsAvailable){
            application.stop();
        }

        //print blocks
        for (int i = 0; i < queueBlocks.size(); i++){
            Block block = queueBlocks.poll();

            sendToLog(Level.ERROR, block);
        }
    }

    private Application startSeeder(){
        String nodeName = "elrond-seeder";
        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-1").getValue());

        PublicKey pbKey = new PublicKey(new PrivateKey(seedNodeRunnerPrivateKey));
        //Reuploaded
        AppContext context = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);
        context.setStorageBasePath(nodeName);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);

        return(application);
    }

    private Application startRunner(String name, int port, int masterPeerPort) {
        String nodeName = name;
        String masterPeerIpAddress = "127.0.0.1";
        String nodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey(name).getValue());
        //Reuploaded
        AppContext context = ContextCreator.createAppContext(nodeName, nodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);
        context.setStorageBasePath(name);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);
        return (application);
    }

    private void sendToLog(Level level, Object... objects){
        if (objects == null){
            logger.log(level, objects);
        }

        String[] strings = new String[objects.length];

        StringBuilder par = new StringBuilder();

        for (int i = 0; i < objects.length; i++){
            strings[i] = getString(objects[i]);
            par.append("{} ");
        }

        logger.log(level, par.toString(), strings);
    }

    private String getString(Object object){
        if (object == null){
            return(null);
        }

        if (object instanceof Transaction){
            Transaction transaction = (Transaction)object;
            return(String.format("Transaction{hash=%s, nonce=%d, value=%d, sender='%s', receiver='%s'}",
                    Util.getDataEncoded64(AppServiceProvider.getSerializationService().getHash(transaction)),
                    transaction.getNonce(), transaction.getValue(), transaction.getSendAddress(), transaction.getReceiverAddress()));
        }

        if (object instanceof Block){
            Block block = (Block)object;

            StringBuilder stringBuilder = new StringBuilder();
            for (byte[] transactionHash: block.getListTXHashes()){
                if (!stringBuilder.toString().equals("")){
                    stringBuilder.append(", ");
                }

                stringBuilder.append(Util.byteArrayToHexString(transactionHash));
            }

            String leader = "[NO LEADER???]";
            if (block.getListPublicKeys().size() > 0){
                leader = block.getListPublicKeys().get(0);
            }

            return(String.format("Block{hash=%s, nonce=%d, appStateHash='%s', listTXHashes.size=%d, listTX=[%s], roundIndex=%d, timestamp=%d, createdBy='%s'}",
                    Util.getDataEncoded64(AppServiceProvider.getSerializationService().getHash(block)),
                    block.getNonce(), block.getListTXHashes().size(), stringBuilder.toString(), block.getRoundIndex(), block.getTimestamp(),
                    block.getListPublicKeys(), leader));
        }

        return(object.toString());
    }
}
