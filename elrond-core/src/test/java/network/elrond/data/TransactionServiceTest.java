package network.elrond.data;

import network.elrond.ExpectedExceptionTest;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.model.Block;
import network.elrond.data.model.Transaction;
import network.elrond.data.service.TransactionService;
import network.elrond.service.AppServiceProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class TransactionServiceTest extends ExpectedExceptionTest {

    TransactionService transactionService = AppServiceProvider.getTransactionService();
    private BigInteger nonce;
    private BigInteger value;
    private PublicKey publicKeySender;
    private PrivateKey privateKeySender;

    private PublicKey publicKeyReceiver;
    private PrivateKey privateKeyReceiver;

    @Before
    public void SetupTest(){
        nonce = BigInteger.ZERO;
        value = BigInteger.TEN.pow(8);

        privateKeySender = new PrivateKey();
        publicKeySender = new PublicKey(privateKeySender);

        privateKeyReceiver = new PrivateKey();
        publicKeyReceiver = new PublicKey(privateKeyReceiver);
    }

    @After
    public void TearDown(){

    }

    @Test
    public void testSignTransactionWithNullTransactionShouldThrowException() {
        expected(IllegalArgumentException.class, "transaction is null");

        transactionService.signTransaction(null, new byte[]{}, new byte[]{});
        Assert.fail();
    }

    @Test
    public void testSignTransactionWithNullPrivateKeyBytesShouldThrowException() {
        expected(IllegalArgumentException.class, "privateKeysBytes is null");

        Transaction tx = transactionService.generateTransaction(publicKeySender, publicKeyReceiver, value.longValue(), nonce.longValue());
        transactionService.signTransaction(tx, null, new byte[]{});
        Assert.fail();
    }

    @Test
    public void testVerifyTransactionWithNullTransactionShouldThrowException() {
        expected(IllegalArgumentException.class, "transaction is null");

        transactionService.verifyTransaction(null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNullBlockchainShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "blockchain is null");

        List<Transaction> transactions = AppServiceProvider.getTransactionService().getTransactions((Blockchain)null, (Block) null);
        transactionService.verifyTransaction(null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNullBlockShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "block is null");

        AppServiceProvider.getTransactionService().getTransactions(new Blockchain(new BlockchainContext()), (Block) null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNewBlockChainShouldReturnZero() throws IOException, ClassNotFoundException {
        List<Transaction> transactions = AppServiceProvider.getTransactionService().getTransactions(new Blockchain(new BlockchainContext()), new Block());
        Assert.assertTrue(transactions!=null && transactions.size() == 0);
    }


    @Test
    public void testSignTransactionSetsSignatureAndChallenge() {
        Transaction tx = transactionService.generateTransaction(publicKeySender, publicKeyReceiver, value.longValue(), nonce.longValue());

        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) i;
        }
        tx.setData(buff);
        //tx.setPubKey(Util.byteArrayToHexString(pbKey.getValue()));

        transactionService.signTransaction(tx, privateKeySender.getValue(), publicKeySender.getValue());

        Assert.assertTrue(tx.getSignature()!=null && tx.getSignature().length > 0);
        Assert.assertTrue(tx.getChallenge()!=null && tx.getChallenge().length > 0);
    }

    @Test
    public void testVerifyTransactionShouldComputeSameSignature() {
        Transaction tx = transactionService.generateTransaction(publicKeySender, publicKeyReceiver, value.longValue(), nonce.longValue());

        byte[] buff = new byte[5];
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) i;
        }
        tx.setData(buff);
        transactionService.signTransaction(tx, privateKeySender.getValue(), publicKeySender.getValue());

        Assert.assertTrue(transactionService.verifyTransaction(tx));
    }

}
