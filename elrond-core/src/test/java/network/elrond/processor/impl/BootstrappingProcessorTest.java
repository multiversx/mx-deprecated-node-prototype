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
import network.elrond.processor.AppProcessors;
import network.elrond.processor.impl.BootstrappingProcessor;
import network.elrond.service.AppServiceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BootstrappingProcessorTest {
    private Application app;

    SerializationService serializationService = AppServiceProvider.getSerializationService();
    TransactionService transactionService = AppServiceProvider.getTransactionService();
    BootstrapService bootstrapService = AppServiceProvider.getBootstrapService();
    AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

    @Before
    public void setup() throws Exception{
        AppContext context = new AppContext();
        context.setMasterPeerIpAddress("127.0.0.1");
        context.setMasterPeerPort(4000);
        context.setPort(4001 /*+ new Random().nextInt(10000)*/);
        context.setPeerId(0);
        context.setBootstrapType(BootstrapType.START_FROM_SCRATCH);
        context.setStorageBasePath("test");

        app = new Application(context);
        AppState state = app.getState();
        state.setStillRunning(false);

        app.start();
    }

    @After
    public void teardown() throws Exception{
        app.stop();
    }

    @Test
    public void bootstrapMethodsTest() throws Exception{
        AppState state = app.getState();

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
        TestCase.assertEquals(Util.BIG_INT_MIN_ONE, bootstrapService.getMaxBlockSizeLocal(state.getBlockchain()));
        TestCase.assertEquals(Util.BIG_INT_MIN_ONE, bootstrapService.getMaxBlockSizeNetwork(state.getConnection()));


        //test 1: test start from scratch
        ExecutionReport executionReport = bootstrappingProcessor.startFromScratch(app);

        TestCase.assertEquals(true, executionReport.isOk());
        TestCase.assertEquals(BigInteger.ZERO, bootstrapService.getMaxBlockSizeLocal(state.getBlockchain()));
        TestCase.assertEquals(BigInteger.ZERO, bootstrapService.getMaxBlockSizeNetwork(state.getConnection()));

        //test 2: test bootstrapping
        //create a new block besides genesis and put it on DHT then try to bootstrap

        PrivateKey pvk1 = new PrivateKey("random1");
        PublicKey pbk1 = new PublicKey(pvk1);

        PrivateKey pvk2 = new PrivateKey("random2");
        PublicKey pbk2 = new PublicKey(pvk2);

        Block blk1 = new DataBlock();
        blk1.setNonce(BigInteger.ONE);

        Transaction trx1 = new Transaction();
        trx1.setNonce(BigInteger.ZERO);
        trx1.setValue(BigInteger.valueOf(1));
        trx1.setSendAddress(Util.getAddressFromPublicKey(pbk1.getValue()));
        trx1.setReceiverAddress(Util.getAddressFromPublicKey(pbk2.getValue()));
        trx1.setPubKey(Util.byteArrayToHexString(pbk1.getValue()));
        transactionService.signTransaction(trx1, pvk1.getValue());

        //put tx on wire
        AppServiceProvider.getP2PObjectService().putJSONencoded(trx1, serializationService.getHashString(trx1, true), state.getConnection());

        List<byte[]> listTxHash = new ArrayList<>();
        listTxHash.add(AppServiceProvider.getSerializationService().getHash(trx1, true));
        blk1.setListTXHashes(listTxHash);

        //put block on wire
        AppServiceProvider.getP2PObjectService().putJSONencoded(blk1, serializationService.getHashString(blk1, true), state.getConnection());

        //put block hash height and block height on wire
        bootstrapService.setBlockHashFromHeightNetwork(blk1.getNonce(), serializationService.getHashString(blk1, true), state.getConnection());
        bootstrapService.setMaxBlockSizeNetwork(blk1.getNonce(), state.getConnection());

        //mint
        AccountState acsSender = accountStateService.getOrCreateAccountState(AccountAddress.fromPublicKey(pbk1), state.getAccounts());
        //mint 100 ERDs
        acsSender.setBalance(BigInteger.TEN.pow(10));
        accountStateService.setAccountState(trx1.getSendAccountAddress(), acsSender, state.getAccounts()); // PMS

        //Now network is loaded, try to bootstrap
        executionReport = bootstrappingProcessor.bootstrap(app, BigInteger.ZERO, bootstrapService.getMaxBlockSizeNetwork(state.getConnection()));

        TestCase.assertEquals(true, executionReport.isOk());
        TestCase.assertEquals(BigInteger.valueOf(1), bootstrapService.getMaxBlockSizeLocal(state.getBlockchain()));
    }


    public void bootstrapMethods2() throws Exception{
        AppState state = app.getState();

        BootstrappingProcessor bootstrappingProcessor = new BootstrappingProcessor();
        bootstrappingProcessor.process(app);


        ExecutionReport executionReport = bootstrappingProcessor.rebuildFromDisk(app, bootstrapService.getMaxBlockSizeLocal(state.getBlockchain()));

        TestCase.assertEquals(BigInteger.valueOf(1), bootstrapService.getMaxBlockSizeNetwork(state.getConnection()));
    }
}
