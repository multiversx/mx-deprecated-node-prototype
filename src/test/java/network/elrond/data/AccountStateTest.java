package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class AccountStateTest {
    AccountStateService acServ = AppServiceProvider.getAccountStateService();
    TransactionService tsServ = AppServiceProvider.getTransactionService();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testAccountStatesSerDeser() {
        //AccountState->JSON->AccountState

        AccountState accountState = new AccountState();
        accountState.setBalance(BigInteger.valueOf(1));
        accountState.setValidatorLockedStake(BigInteger.valueOf(2));
        accountState.setNonce(BigInteger.valueOf(3));
        accountState.setValidatorReputation(4);
        accountState.setValidatorShardNo(5);

        String strTest = acServ.encodeJSON(accountState);
        logger.info(strTest);

        AccountState accountState1 = acServ.decodeJSON(strTest);
        logger.info(acServ.encodeJSON(accountState1));

        TestCase.assertEquals(strTest, acServ.encodeJSON(accountState1));
    }

    @Test
    public void testAccountStatesExecute1Tx() {
        //execute a single transaction

        //Test null
        try {
            AccountState[] accnts = acServ.executeTransaction(null);
            TestCase.assertEquals("NOK", "ERR - should have thrown");
        } catch (Exception ex) {
            TestCase.assertEquals(ex.getMessage(), "NULL transaction object!");
        }

        PrivateKey pvKeySender = new PrivateKey();
        PublicKey pbKeySender = new PublicKey(pvKeySender);

        PrivateKey pvKeyRecv = new PrivateKey();
        PublicKey pbKeyRecv = new PublicKey(pvKeyRecv);

        Transaction tx = new Transaction();
        tx.setNonce(BigInteger.ZERO);
        //2 ERDs
        tx.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(2)));
        tx.setSendAddress(Util.getAddressFromPublicKey(pbKeySender.getEncoded()));
        tx.setRecvAddress(Util.getAddressFromPublicKey(pbKeyRecv.getEncoded()));
        tx.setPubKey(Util.byteArrayToHexString(pbKeySender.getEncoded()));

        tsServ.signTransaction(tx, pvKeySender.getValue());

        tx.setNonce(BigInteger.ONE);

        //Test tampered tx
        try {
            AccountState[] accnts = acServ.executeTransaction(tx);
            TestCase.assertEquals("NOK", "ERR - should have thrown");
        } catch (Exception ex) {
            TestCase.assertEquals(true, ex.getMessage().contains("Invalid transaction! tx hash: "));
        }

        tx.setNonce(BigInteger.ZERO);
        tsServ.signTransaction(tx, pvKeySender.getValue());
        //test balance less than value
        try {
            AccountState[] accnts = acServ.executeTransaction(tx);
            TestCase.assertEquals("NOK", "ERR - should have thrown");
        } catch (Exception ex) {
            TestCase.assertEquals(true, ex.getMessage().contains("Invalid transaction! Will result in negative balance! tx hash: "));
        }

        //Test tx nonce mismatch
        AccountState acsSender = acServ.getCreateAccount(Util.getAddressFromPublicKey(pbKeySender.getEncoded()));
        //mint 100 ERDs
        acsSender.setBalance(BigInteger.TEN.pow(10));
        tx.setNonce(BigInteger.ONE);
        tsServ.signTransaction(tx, pvKeySender.getValue());
        try {
            AccountState[] accnts = acServ.executeTransaction(tx);
            TestCase.assertEquals("NOK", "ERR - should have thrown");
        } catch (Exception ex) {
            TestCase.assertEquals(true, ex.getMessage().contains("Invalid transaction! Nonce mismatch! tx hash: "));
        }

        //output 2 accounts
        tx.setNonce(BigInteger.ZERO);
        tsServ.signTransaction(tx, pvKeySender.getValue());
        try {
            AccountState[] accnts = acServ.executeTransaction(tx);

            logger.info(acServ.encodeJSON(accnts[0]));
            logger.info(acServ.encodeJSON(accnts[1]));

            //sender
            TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(98)), accnts[1].getBalance());
            TestCase.assertEquals(BigInteger.ONE, accnts[1].getNonce());
            //receiver
            TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(2)), accnts[0].getBalance());

        } catch (Exception ex) {
            TestCase.assertEquals("NOK", ex.getMessage());
        }
    }

    @Test
    public void testAccountStatesExecuteAccTxs(){

        PrivateKey pv1 = new PrivateKey();
        PublicKey pb1 = new PublicKey(pv1);

        PrivateKey pv2 = new PrivateKey();
        PublicKey pb2 = new PublicKey(pv2);

        PrivateKey pv3 = new PrivateKey();
        PublicKey pb3 = new PublicKey(pv3);

        PrivateKey pv4 = new PrivateKey();
        PublicKey pb4 = new PublicKey(pv4);

        Transaction tx1 = new Transaction();
        tx1.setNonce(BigInteger.ZERO);
        //2 ERDs
        tx1.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(2)));
        tx1.setSendAddress(Util.getAddressFromPublicKey(pb1.getEncoded()));
        tx1.setRecvAddress(Util.getAddressFromPublicKey(pb2.getEncoded()));
        tx1.setPubKey(Util.byteArrayToHexString(pb1.getEncoded()));
        tsServ.signTransaction(tx1, pv1.getValue());

        Transaction tx2 = new Transaction();
        tx2.setNonce(BigInteger.ZERO);
        //3 ERDs
        tx2.setValue(BigInteger.valueOf(10).pow(8).multiply(BigInteger.valueOf(3)));
        tx2.setSendAddress(Util.getAddressFromPublicKey(pb3.getEncoded()));
        tx2.setRecvAddress(Util.getAddressFromPublicKey(pb4.getEncoded()));
        tx2.setPubKey(Util.byteArrayToHexString(pb3.getEncoded()));
        tsServ.signTransaction(tx2, pv3.getValue());

        //minting
        AccountState acs1 = acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded()));
        //200 ERDs
        acs1.setBalance(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(2)));
        AccountState acs3 = acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded()));
        //300 ERDs
        acs3.setBalance(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(3)));

        try {
            acServ.executeTransactionAccumulatingData(tx1);
            acServ.executeTransactionAccumulatingData(tx2);
        } catch (Exception ex){
            TestCase.assertEquals("NOK", ex.getMessage());
        }

        //print
        logger.info("INITIAL:");
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded()))));
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded()))));
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded()))));
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded()))));

        //no changes in actual account states mem
        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(2)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.ZERO,
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(3)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.ZERO,
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded())).getBalance());

        //rollback
        acServ.doRollBackLastAccumulatedData();

        //no changes in actual account states mem
        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(2)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.ZERO,
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.TEN.pow(10).multiply(BigInteger.valueOf(3)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.ZERO,
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded())).getBalance());

        try {
            acServ.executeTransactionAccumulatingData(tx1);
            acServ.executeTransactionAccumulatingData(tx2);
        } catch (Exception ex){
            TestCase.assertEquals("NOK", ex.getMessage());
        }

        acServ.doCommitLastAccumulatedData();

        //print
        logger.info("CHANGED:");
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded()))));
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded()))));
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded()))));
        logger.info(acServ.encodeJSON(acServ.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded()))));

        //changes occurred
        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(198)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.valueOf(1),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb1.getEncoded())).getNonce());

        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(2)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb2.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(297)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getBalance());
        TestCase.assertEquals(BigInteger.valueOf(1),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb3.getEncoded())).getNonce());

        TestCase.assertEquals(BigInteger.TEN.pow(8).multiply(BigInteger.valueOf(3)),
                acServ.getCreateAccount(Util.getAddressFromPublicKey(pb4.getEncoded())).getBalance());
    }
}
