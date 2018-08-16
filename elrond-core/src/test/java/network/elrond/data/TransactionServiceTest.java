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

import javafx.util.Pair;

public class TransactionServiceTest extends ExpectedExceptionTest {

    TransactionService transactionService = AppServiceProvider.getTransactionService();
    private BigInteger nonce;
    private BigInteger value;
    private PublicKey publicKeySender;
    private PrivateKey privateKeySender;

    private PublicKey publicKeyReceiver;

    @Before
    public void SetupTest() {
        String sendAddress = "0xa87b8fa28a8476553363a9356aa02635e4a1b033";
        String recvAddress = "0x0000000000000000000000000000000000000000";
        String pubKey = "025f37d20e5b18909361e0ead7ed17c69b417bee70746c9e9c2bcb1394d921d4ae";
        nonce = BigInteger.ZERO;
        value = BigInteger.TEN.pow(8);

        privateKeySender = new PrivateKey();
        publicKeySender = new PublicKey(privateKeySender);

        PrivateKey privateKeyReceiver = new PrivateKey();
        publicKeyReceiver = new PublicKey(privateKeyReceiver);
    }

    @After
    public void TearDown() {

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

        List<Pair<String, Transaction>> transactionHashPairs = AppServiceProvider.getTransactionService().getTransactions(null, null);
        transactionService.verifyTransaction(null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNullBlockShouldThrowException() throws IOException, ClassNotFoundException {
        expected(IllegalArgumentException.class, "block is null");

        List<Pair<String, Transaction>> transactionHashPairs = AppServiceProvider.getTransactionService().getTransactions(new Blockchain(new BlockchainContext()), null);
        Assert.fail();
    }

    @Test
    public void testgetTransactionsWithNewBlockChainShouldReturnZero() throws IOException, ClassNotFoundException {
        List<Pair<String, Transaction>> transactionHashPairs = AppServiceProvider.getTransactionService().getTransactions(new Blockchain(new BlockchainContext()), new Block());
        Assert.assertTrue(transactionHashPairs != null && transactionHashPairs.size() == 0);
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

        Assert.assertTrue(tx.getSignature() != null && tx.getSignature().length > 0);
        Assert.assertTrue(tx.getChallenge() != null && tx.getChallenge().length > 0);
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
