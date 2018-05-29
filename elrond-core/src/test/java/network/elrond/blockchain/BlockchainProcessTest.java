package network.elrond.blockchain;

import junit.framework.TestCase;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.data.BaseBlockchainTest;
import network.elrond.data.Block;
import network.elrond.data.DataBlock;
import network.elrond.data.Transaction;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlockchainProcessTest extends BaseBlockchainTest {


    @Test
    public void testProcessBlock() throws Exception {
        PrivateKey privateKeySender = new PrivateKey("PRIVATE KEY".getBytes());
        PublicKey publicKeySender = new PublicKey(privateKeySender);
        PrivateKey privateKeyReceiver = new PrivateKey("PRIVATE KEY2".getBytes());
        PublicKey publicKeyReceiver = new PublicKey(privateKeyReceiver);

        Blockchain blockchain = new Blockchain(getDefaultTestBlockchainContext());
        Accounts accounts = initAccounts(publicKeySender);

        byte[] prevBlockHash = null;
        List<String> blocksHashes = new ArrayList<>();

        String senderAddress = Util.getAddressFromPublicKey(publicKeySender.getValue());
        String receiverAddress = Util.getAddressFromPublicKey(publicKeyReceiver.getValue());

        for (int i = 0; i < 10; i++) {
            Block block = new DataBlock();
            BigInteger nonce = BigInteger.ONE.add(BigInteger.valueOf(i));
            block.setNonce(nonce);

            if (prevBlockHash != null) {
                block.setPrevBlockHash(prevBlockHash);
            }


            Transaction transaction = new Transaction(senderAddress,
                    receiverAddress,
                    BigInteger.TEN, BigInteger.ZERO.add(BigInteger.valueOf(i)));
            transaction.setPubKey(Util.byteArrayToHexString(publicKeySender.getValue()));

            byte[] hash = AppServiceProvider.getSerializationService().getHash(transaction);
            block.getListTXHashes().add(hash);

            String hashString = AppServiceProvider.getSerializationService().getHashString(transaction);
            AppServiceProvider.getTransactionService().signTransaction(transaction, privateKeySender.getValue());
            AppServiceProvider.getBlockchainService().put(hashString, transaction, blockchain, BlockchainUnitType.TRANSACTION);


            byte[] blockHash = AppServiceProvider.getSerializationService().getHash(block);
            String blockHashString = AppServiceProvider.getSerializationService().getHashString(block);
            AppServiceProvider.getBlockchainService().put(blockHashString, block, blockchain, BlockchainUnitType.BLOCK);

            blocksHashes.add(blockHashString);

            prevBlockHash = blockHash;
        }

        // Flush memory and read from database engine
        blockchain.flush();
        accounts.flush();


        for (String blockHash : blocksHashes) {
            Block block = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK);
            TestCase.assertTrue(AppServiceProvider.getExecutionService().processBlock(block, accounts, blockchain).isOk());
        }

        System.out.println("SenderAccountState Account state" + AccountAddress.fromPublicKey(publicKeySender));
        AccountState senderAccountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromPublicKey(publicKeySender), accounts);
        TestCase.assertEquals(senderAccountState.getBalance(), BigInteger.valueOf(123456689));

        System.out.println("ReceiverAccountState AccountAddress" + AccountAddress.fromPublicKey(publicKeyReceiver));
        AccountState receiverAccountState = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromPublicKey(publicKeyReceiver), accounts);
        TestCase.assertEquals(receiverAccountState.getBalance(),BigInteger.valueOf(100));




    }

    private Accounts initAccounts(PublicKey publicKey) throws IOException, ClassNotFoundException {
        AccountsContext accountContext = new AccountsContext();
        accountContext.setDatabasePath("blockchain.account.data-test");
        Accounts accounts = new Accounts(accountContext);

        AccountAddress address = AccountAddress.fromPublicKey(publicKey);
        AccountState accountState = AppServiceProvider.getAccountStateService()
                .getOrCreateAccountState(address, accounts);
        accountState.setBalance(BigInteger.valueOf(123456789));

        AppServiceProvider.getAccountStateService().setAccountState(address, accountState, accounts);

        System.out.println("Initial Account address" + address.toString());

        return accounts;
    }
}
