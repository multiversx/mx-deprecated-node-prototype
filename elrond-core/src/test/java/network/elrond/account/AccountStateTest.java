package network.elrond.account;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.SerializationService;
import network.elrond.data.Transaction;
import network.elrond.data.ExecutionService;
import network.elrond.data.TransactionService;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AccountStateTest {


    TransactionService transactionService = AppServiceProvider.getTransactionService();
    AccountStateService accountStateService = AppServiceProvider.getAccountStateService();
    SerializationService serializationService = AppServiceProvider.getSerializationService();
    ExecutionService transactionExecutionService = AppServiceProvider.getExecutionService();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testAccountStatesSerDeser() {
        //AccountState->JSON->AccountState

        AccountState accountState = new AccountState();
        accountState.setBalance(BigInteger.valueOf(1));
        accountState.setNonce(BigInteger.valueOf(3));


        String strTest = serializationService.encodeJSON(accountState);
        logger.info(strTest);

        AccountState accountState1 = serializationService.decodeJSON(strTest, AccountState.class);
        logger.info(serializationService.encodeJSON(accountState1));

        TestCase.assertEquals(strTest, serializationService.encodeJSON(accountState1));
    }

    @Test
    public void testAccountStatesExecute1Tx() throws Exception {
        //execute a single transaction

        AccountsContext context = new AccountsContext();
        context.setDatabasePath("test");
        Accounts accounts = new Accounts(context);


        TestCase.assertFalse(transactionExecutionService.processTransaction(null, accounts).isOk());

        PrivateKey pvKeySender = new PrivateKey();
        PublicKey pbKeySender = new PublicKey(pvKeySender);

        PrivateKey pvKeyRecv = new PrivateKey();
        PublicKey pbKeyRecv = new PublicKey(pvKeyRecv);

        Transaction tx = new Transaction();
        tx.setNonce(BigInteger.ZERO);
        //2 ERDs
        tx.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(2)));
        tx.setSendAddress(Util.getAddressFromPublicKey(pbKeySender.getValue()));
        tx.setReceiverAddress(Util.getAddressFromPublicKey(pbKeyRecv.getValue()));
        tx.setPubKey(Util.byteArrayToHexString(pbKeySender.getValue()));

        transactionService.signTransaction(tx, pvKeySender.getValue());

        tx.setNonce(BigInteger.ONE);

        //Test tampered tx

        TestCase.assertFalse(transactionExecutionService.processTransaction(tx, accounts).isOk());


        tx.setNonce(BigInteger.ZERO);
        transactionService.signTransaction(tx, pvKeySender.getValue());
        //test balance less than value
        TestCase.assertFalse(transactionExecutionService.processTransaction(tx, accounts).isOk());


        //Test tx nonce mismatch

        AccountState acsSender = accountStateService.getOrCreateAccountState(AccountAddress.fromPublicKey(pbKeySender), accounts);

        //mint 100 ERDs
        acsSender.setBalance(BigInteger.TEN.pow(10));
        accountStateService.setAccountState(tx.getSendAccountAddress(), acsSender, accounts); // PMS

        tx.setNonce(BigInteger.ONE);
        transactionService.signTransaction(tx, pvKeySender.getValue());
        TestCase.assertFalse(transactionExecutionService.processTransaction(tx, accounts).isOk());


        //output 2 accounts
        tx.setNonce(BigInteger.ZERO);
        transactionService.signTransaction(tx, pvKeySender.getValue());
        try {

            TestCase.assertTrue(transactionExecutionService.processTransaction(tx, accounts).isOk());

            AccountState senderAccount = accountStateService.getAccountState(tx.getSendAccountAddress(), accounts);
            AccountState reciverAccount = accountStateService.getAccountState(tx.getReceiverAccountAddress(), accounts);


            logger.info(AppServiceProvider.getSerializationService().encodeJSON(senderAccount));

            logger.info(AppServiceProvider.getSerializationService().encodeJSON(reciverAccount));

            //sender
            TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(98)), senderAccount.getBalance());
            TestCase.assertEquals(BigInteger.ONE, senderAccount.getNonce());
            //receiver
            TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(2)), reciverAccount.getBalance());

        } catch (Exception ex) {
            TestCase.assertEquals("NOK", ex.getMessage());
        }
    }

    @Test
    public void testMintingAccountState() throws Exception{

        AccountStateService accountStateService = AppServiceProvider.getAccountStateService();

        AccountsContext context = new AccountsContext();
        context.setDatabasePath("testAccounts");
        Accounts accounts = new Accounts(context);

        AccountState asRecv = accountStateService.getAccountState(new AccountAddress(Util.PUBLIC_KEY_MINTING.getValue()), accounts);
        assertNotEquals( "Not expected null ", null, asRecv);
        assertEquals( "Expected balance " + Util.VALUE_MINTING.toString(10), Util.VALUE_MINTING, asRecv.getBalance());

        asRecv = accountStateService.getAccountState(AccountAddress.fromHexaString(Util.getAddressFromPublicKey(Util.PUBLIC_KEY_MINTING.getValue())), accounts);
        assertNotEquals( "Not expected null ", null, asRecv);
        assertEquals( "Expected balance " + Util.VALUE_MINTING.toString(10), Util.VALUE_MINTING, asRecv.getBalance());
    }

//    @Test
//    public void testAccountStatesExecuteAccTxs() {
//
//        PrivateKey pv1 = new PrivateKey();
//        PublicKey pb1 = new PublicKey(pv1);
//
//        PrivateKey pv2 = new PrivateKey();
//        PublicKey pb2 = new PublicKey(pv2);
//
//        PrivateKey pv3 = new PrivateKey();
//        PublicKey pb3 = new PublicKey(pv3);
//
//        PrivateKey pv4 = new PrivateKey();
//        PublicKey pb4 = new PublicKey(pv4);
//
//        Transaction tx1 = new Transaction();
//        tx1.setNonce(BigInteger.ZERO);
//        //2 ERDs
//        tx1.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(2)));
//        tx1.setSendAddress(Util.getAddressFromPublicKey(pb1.getEncoded()));
//        tx1.setReceiverAddress(Util.getAddressFromPublicKey(pb2.getEncoded()));
//        tx1.setPubKey(Util.byteArrayToHexString(pb1.getEncoded()));
//        transactionService.signTransaction(tx1, pv1.getValue());
//
//        Transaction tx2 = new Transaction();
//        tx2.setNonce(BigInteger.ZERO);
//        //3 ERDs
//        tx2.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(3)));
//        tx2.setSendAddress(Util.getAddressFromPublicKey(pb3.getEncoded()));
//        tx2.setReceiverAddress(Util.getAddressFromPublicKey(pb4.getEncoded()));
//        tx2.setPubKey(Util.byteArrayToHexString(pb3.getEncoded()));
//        transactionService.signTransaction(tx2, pv3.getValue());
//
//        //minting
//        AccountState acs1 = accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded()));
//        //200 ERDs
//        acs1.setBalance(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(2)));
//        AccountState acs3 = accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded()));
//        //300 ERDs
//        acs3.setBalance(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(3)));
//
//        try {
//            accountStateService.executeTransactionAccumulatingData(tx1);
//            accountStateService.executeTransactionAccumulatingData(tx2);
//        } catch (Exception ex) {
//            TestCase.assertEquals("NOK", ex.getMessage());
//        }
//
//        //print
//        logger.info("INITIAL:");
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded()))));
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded()))));
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded()))));
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded()))));
//
//        //no changes in actual account states mem
//        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(2)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.ZERO,
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(3)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.ZERO,
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded())).getBalance());
//
//        //rollback
//        accountStateService.doRollBackLastAccumulatedData();
//
//        //no changes in actual account states mem
//        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(2)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.ZERO,
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(3)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.ZERO,
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded())).getBalance());
//
//        try {
//            accountStateService.executeTransactionAccumulatingData(tx1);
//            accountStateService.executeTransactionAccumulatingData(tx2);
//        } catch (Exception ex) {
//            TestCase.assertEquals("NOK", ex.getMessage());
//        }
//
//        accountStateService.doCommitLastAccumulatedData();
//
//        //print
//        logger.info("CHANGED:");
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded()))));
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded()))));
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded()))));
//        logger.info(AppServiceProvider.getSerializationService().encodeJSON(accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded()))));
//
//        //changes occurred
//        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(198)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.valueOf(1),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getNonce());
//
//        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(2)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(297)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getBalance());
//        TestCase.assertEquals(BigInteger.valueOf(1),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getNonce());
//
//        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(3)),
//                accountStateService.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded())).getBalance());
//    }
}
