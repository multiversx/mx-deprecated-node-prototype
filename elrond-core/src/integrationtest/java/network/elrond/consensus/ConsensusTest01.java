package network.elrond.consensus;

import network.elrond.Application;
import network.elrond.ContextCreator;
import network.elrond.ElrondFacade;
import network.elrond.ElrondFacadeImpl;
import network.elrond.account.AccountAddress;
import network.elrond.application.AppContext;
import network.elrond.blockchain.BlockchainService;
import network.elrond.core.ThreadUtil;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.Block;
import network.elrond.data.BootstrapType;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ConsensusTest01 {
    private static final Logger logger = LogManager.getLogger(Util.class);

    @Test
    public void testConsensus() throws Exception{
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

        //not wise build list
        List<Block> listBlocks = new ArrayList<>();
        Thread thrTrackBlocks = new Thread(() -> {
            while (applicationsAvailable[0].getState().isStillRunning()) {
                ThreadUtil.sleep(100);
                buildBlockList(applicationsAvailable[1], listBlocks);
            }
        });
        thrTrackBlocks.start();



        for (int i = 0; i < 10; i++){
            AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
            Transaction transaction = facade.send(address, BigInteger.valueOf(i), applicationsAvailable[0]);

            sendToLog(Level.ERROR, "Sent tx ", transaction, " to ", Util.byteArrayToHexString(applicationsAvailable[0].getState().getPublicKey().getValue()));



            ThreadUtil.sleep(4000);
        }

        //settle down
        ThreadUtil.sleep(10000);

        //stopping apps
        for (Application application:applicationsAvailable){
            application.stop();
        }

        thrTrackBlocks.join();

        //print blocks
        for (int i = 0; i < listBlocks.size(); i++){
            Block block = listBlocks.get(i);

            sendToLog(Level.ERROR, block);
        }
    }

    private void buildBlockList(Application application, List<Block> listaBlocks){
        if (application.getState().getBlockchain().getGenesisBlock() != null){
            if (!listaBlocks.contains(application.getState().getBlockchain().getGenesisBlock())){
                listaBlocks.add(application.getState().getBlockchain().getGenesisBlock());
            }
        }

        if (application.getState().getBlockchain().getCurrentBlock() != null){
            if (!listaBlocks.contains(application.getState().getBlockchain().getCurrentBlock())){
                listaBlocks.add(application.getState().getBlockchain().getCurrentBlock());
            }
        }
    }

    private Application startSeeder() throws Exception{
        String nodeName = "elrond-seeder";
        Util.changeLogsPath("logs-" + nodeName);

        Integer port = 4000;
        Integer masterPeerPort = 4000;
        String masterPeerIpAddress = "127.0.0.1";
        String seedNodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey("elrond-node-1").getValue());

        PublicKey pbKey = new PublicKey(new PrivateKey(seedNodeRunnerPrivateKey));
        //Reuploaded
        AppContext context = ContextCreator.createAppContext(nodeName, seedNodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, nodeName);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);

        sendToLog(Level.ERROR,"Started ", nodeName);
        return(application);
    }

    private Application startRunner(String name, int port, int masterPeerPort) throws Exception {
        Util.changeLogsPath("logs-" + name);
        String masterPeerIpAddress = "127.0.0.1";
        String nodeRunnerPrivateKey = Util.byteArrayToHexString(new PrivateKey(name).getValue());
        //Reuploaded
        AppContext context = ContextCreator.createAppContext(name, nodeRunnerPrivateKey, masterPeerIpAddress, masterPeerPort, port,
                BootstrapType.START_FROM_SCRATCH, name);

        ElrondFacade facade = new ElrondFacadeImpl();

        Application application = facade.start(context);
        sendToLog(Level.ERROR,"Started {}", name);
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
                    transaction.getNonce(), transaction.getValue(), transaction.getSenderAddress(), transaction.getReceiverAddress()));
        }

        if (object instanceof Block){
            Block block = (Block)object;

            StringBuilder stringBuilder = new StringBuilder();
            for (byte[] transactionHash: block.getListTXHashes()){
                if (!stringBuilder.toString().equals("")){
                    stringBuilder.append(", ");
                }

                stringBuilder.append(Util.getDataEncoded64(transactionHash));
            }

            String leader = "[NO LEADER???]";
            if (block.getListPublicKeys().size() > 0){
                leader = block.getListPublicKeys().get(0);
            }

            return(String.format("Block{hash=%s, nonce=%d, appStateHash='%s', listTXHashes.size=%d, listTX=[%s], roundIndex=%d, timestamp=%d, createdBy='%s'}",
                    Util.getDataEncoded64(AppServiceProvider.getSerializationService().getHash(block)),
                    block.getNonce(), Util.getDataEncoded64(block.getAppStateHash()), BlockUtil.getTransactionsCount(block),
                    stringBuilder.toString(), block.getRoundIndex(), block.getTimestamp(), block.getListPublicKeys(), leader));
        }

        return(object.toString());
    }

    @Test
    public void testStartSeeder() throws Exception{
        Application seeder = startSeeder();
        //seeder.getState().getConsensusData().nodeName = seeder.getContext().getNodeName();

        ElrondFacadeImpl facade = new ElrondFacadeImpl();



        Thread thrSeed = new Thread(()->{


            int value = 1;

            List<String> listTransactionsGenerated = new ArrayList<String>();
            List<String> listTransactionsProcessed = new ArrayList<String>();
            Block lastBlock = null;
            while (seeder.getState().isStillRunning()) {

                Block blk = seeder.getState().getBlockchain().getCurrentBlock();

                if (lastBlock != blk){
                    List<byte[]> listTx = blk.getListTXHashes();

                    for (int i = 0; i < listTx.size(); i++){
                        String hash = Util.getDataEncoded64(listTx.get(i));

                        if (listTransactionsGenerated.contains(hash)){
                            listTransactionsGenerated.remove(hash);
                            listTransactionsProcessed.add(hash);
                        }
                    }

                    lastBlock = blk;
                }

                if ((listTransactionsProcessed.size() >= 3000) && (listTransactionsGenerated.size() == 0)){
                    logger.log(Level.ERROR, "Tx generated: {}, tx processed: {}", listTransactionsGenerated.size(), listTransactionsProcessed.size());
                    break;
                }

                if ((seeder.getState().getTransactionPool().size() > 2000) ||
                        (listTransactionsProcessed.size() >= 3000)){
                    ThreadUtil.sleep(1);
                    continue;
                }

                if (value % 100 == 0) {
                    logger.log(Level.ERROR, "Generating...Tx generated: {}, tx processed: {}", listTransactionsGenerated.size(), listTransactionsProcessed.size());
                }

                AccountAddress address = AccountAddress.fromHexString(Util.TEST_ADDRESS);
                Transaction transaction = facade.send(address, BigInteger.valueOf(value), seeder);

                if (transaction == null) {
                    continue;
                }

                String hash = AppServiceProvider.getSerializationService().getHashString(transaction);
                listTransactionsGenerated.add(hash);
                value++;
            }
            logger.log(Level.ERROR, "dOnE...Tx generated: {}, tx processed: {}", listTransactionsGenerated.size(), listTransactionsProcessed.size());
        });
        //thrSeed.setPriority(2);
        thrSeed.start();

        while (true){
            ThreadUtil.sleep(1000);
        }


    }

    @Test
    public void testStartNode1() throws Exception{
        Application seeder = startRunner("runner-1", 4001, 4000);
        //seeder.getState().getConsensusData().nodeName = seeder.getContext().getNodeName();
        while (true){
            ThreadUtil.sleep(100);
        }
    }

    @Test
    public void testStartNode2() throws Exception{
        Application seeder = startRunner("runner-2", 4002, 4000);
        //seeder.getState().getConsensusData().nodeName = seeder.getContext().getNodeName();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        while (true){
            ThreadUtil.sleep(100);
        }
    }

}
