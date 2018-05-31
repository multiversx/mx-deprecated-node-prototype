package network.elrond.data;

import network.elrond.ExpectedExceptionTest;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainContext;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
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
    private String sendAddress ;
    private String recvAddress ;
    private String pubKey ;
    private BigInteger nonce;
    private BigInteger value;
    private PublicKey publicKeySender;
    private PrivateKey privateKeySender;

    private PublicKey publicKeyReceiver;
    private PrivateKey privateKeyReceiver;

    @Before
    public void SetupTest(){
        sendAddress = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";
        recvAddress = "0x0000000000000000000000000000000000000000";
        pubKey = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
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
        expected(IllegalArgumentException.class, "Transaction cannot be null");

        transactionService.signTransaction(null, new byte[]{});
        Assert.fail();
    }

    @Test
    public void testSignTransactionWithNullPrivateKeyBytesShouldThrowException() {
        expected(IllegalArgumentException.class, "PrivateKeysBytes cannot be null");

        Transaction tx = transactionService.generateTransaction(publicKeySender, publicKeyReceiver, value.longValue(), nonce.longValue());
        transactionService.signTransaction(tx, null);
        Assert.fail();
    }

    @Test
    public void testVerifyTransactionWithNullTransactionShouldThrowException() {
        expected(IllegalArgumentException.class, "Transaction cannot be null");

        transactionService.verifyTransaction(null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNullBlockchainShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "Blockchain cannot be null");

        List<Transaction> transactions = AppServiceProvider.getTransactionService().getTransactions((Blockchain)null, (Block) null);
        transactionService.verifyTransaction(null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNullBlockShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "Block cannot be null");

        List<Transaction> transactions = AppServiceProvider.getTransactionService().getTransactions(new Blockchain(new BlockchainContext()), (Block) null);
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

        transactionService.signTransaction(tx, privateKeySender.getValue());

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
        transactionService.signTransaction(tx, privateKeySender.getValue());

        Assert.assertTrue(transactionService.verifyTransaction(tx));
    }

}
