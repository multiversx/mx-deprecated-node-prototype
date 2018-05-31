package network.elrond.processor.impl;

import junit.framework.TestCase;
import network.elrond.Application;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.AccountStateService;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.BlockchainPersistenceUnit;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.*;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BootstrappingProcessorTest {
    SerializationService serializationService = AppServiceProvider.getSerializationService();
    TransactionService transactionService = AppServiceProvider.getTransactionService();
    BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

    @Test
    public void bootstrapMethodsTest() throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setNodeName("0");
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        context.setStorageBasePath("test");
        context.setPrivateKey(new PrivateKey("test"));

        PrivateKey pvKeyRandom = new PrivateKey("RANDOM STUFF THAT'S JUST RANDOM");
        context.setStrAddressMint(Util.getAddressFromPublicKey(new PublicKey(pvKeyRandom).getValue()));
        context.setValueMint(BigInteger.TEN);

        Application app  = new Application(context);
        AppState state = app.getState();
        state.setStillRunning(false);
        app.start();


        BootstrappingProcessor bootstrappingProcessor = new BootstrappingProcessor();
        bootstrappingProcessor.process(app);

        //delete stored data
        for (BlockchainUnitType blockchainUnitType : BlockchainUnitType.values()) {
            BlockchainPersistenceUnit<Object, Object> blockchainPersistenceUnit = state.getBlockchain().getUnit(blockchainUnitType);

            if (blockchainPersistenceUnit == null){
                continue;
            }

            blockchainPersistenceUnit.destroyAndReCreate();
        }

        //test that initial values are minus one
        TestCase.assertEquals(Util.BIG_INT_MIN_ONE, bootstrapService.getMaxBlockSize(LocationType.LOCAL, state.getBlockchain()));
        TestCase.assertEquals(Util.BIG_INT_MIN_ONE, bootstrapService.getMaxBlockSize(LocationType.NETWORK, state.getBlockchain()));


        //test 1: test start from scratch
        ExecutionReport executionReport = bootstrapService.startFromScratch(app);

        TestCase.assertEquals(true, executionReport.isOk());
        TestCase.assertEquals(BigInteger.ZERO, bootstrapService.getMaxBlockSize(LocationType.LOCAL, state.getBlockchain()));
        TestCase.assertEquals(BigInteger.ZERO, bootstrapService.getMaxBlockSize(LocationType.NETWORK, state.getBlockchain()));

        //test 2: test bootstrapping
        //create a new block besides genesis and put it on DHT then try to bootstrap

        PrivateKey pvk1 = new PrivateKey("random1");
        PublicKey pbk1 = new PublicKey(pvk1);

        PrivateKey pvk2 = new PrivateKey("random2");
        PublicKey pbk2 = new PublicKey(pvk2);

        Block blk1 = new DataBlock();
        blk1.setNonce(BigInteger.ONE);

        Transaction trx1 = transactionService.generateTransaction(pbk1, pbk2, 1, 0);
        //trx1.setPubKey(Util.byteArrayToHexString(pbk1.getValue()));
        transactionService.signTransaction(trx1, pvk1.getValue());

        //put tx on wire
        AppServiceProvider.getP2PObjectService().putJsonEncoded(trx1, serializationService.getHashString(trx1), state.getConnection());

        List<byte[]> listTxHash = new ArrayList<>();
        listTxHash.add(AppServiceProvider.getSerializationService().getHash(trx1));
        blk1.setListTXHashes(listTxHash);

        //put block on wire
        AppServiceProvider.getP2PObjectService().putJsonEncoded(blk1, serializationService.getHashString(blk1), state.getConnection());

        //put block hash height and block height on wire
        bootstrapService.setBlockHashWithHeight(LocationType.NETWORK, blk1.getNonce(), serializationService.getHashString(blk1), state.getBlockchain());
        bootstrapService.setMaxBlockSize(LocationType.NETWORK, blk1.getNonce(), state.getBlockchain());

        //mint
        AccountState acsSender = accountStateService.getOrCreateAccountState(AccountAddress.fromPublicKey(pbk1), state.getAccounts());
        //mint 100 ERDs
        acsSender.setBalance(BigInteger.TEN.pow(10));
        accountStateService.setAccountState(AccountAddress.fromHexaString(trx1.getSendAddress()), acsSender, state.getAccounts()); // PMS

        //Now network is loaded, try to bootstrap
        executionReport = bootstrapService.bootstrap(app, BigInteger.ZERO, bootstrapService.getMaxBlockSize(LocationType.NETWORK, state.getBlockchain()));

        TestCase.assertEquals(true, executionReport.isOk());
        TestCase.assertEquals(BigInteger.valueOf(1), bootstrapService.getMaxBlockSize(LocationType.LOCAL, state.getBlockchain()));


        app.stop();

    }


//    public void bootstrapMethods2() throws Exception{
//        AppState state = app.getState();
//
//        BootstrappingProcessor bootstrappingProcessor = new BootstrappingProcessor();
//        bootstrappingProcessor.process(app);
//
//
//        ExecutionReport executionReport = bootstrappingProcessor.rebuildFromDisk(app, bootstrapService.getMaxBlockSizeLocal(state.getBlockchain()));
//
//        TestCase.assertEquals(BigInteger.valueOf(1), bootstrapService.getMaxBlockSizeNetwork(state.getConnection()));
//    }

}
