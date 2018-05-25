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

        Blockchain blockchain = new Blockchain(getDefaultTestBlockchainContext());
        Accounts accounts = initAccounts();


        byte[] prevBlockHash = null;
        List<String> blocksHashes = new ArrayList<>();

        PrivateKey key = new PrivateKey("PRIVATE KEY".getBytes());
        PublicKey publicKey = new PublicKey(key);
        String addressFromPublicKey = Util.getAddressFromPublicKey(publicKey.getQ().getEncoded(true));


        for (int i = 0; i < 10; i++) {
            Block block = new DataBlock();
            BigInteger nonce = BigInteger.ONE.add(BigInteger.valueOf(i));
            block.setNonce(nonce);

            if (prevBlockHash != null) {
                block.setPrevBlockHash(prevBlockHash);
            }


            Transaction transaction = new Transaction();
            transaction.setNonce(BigInteger.ZERO.add(BigInteger.valueOf(i)));
            transaction.setReceiverAddress("0xAABBCC0b727509404c8f0ffa2f9e5344744794CC");
            transaction.setSendAddress(addressFromPublicKey);
            transaction.setValue(BigInteger.TEN);
            transaction.setPubKey(Util.byteArrayToHexString(publicKey.getValue()));

            byte[] hash = AppServiceProvider.getSerializationService().getHash(transaction, true);
            block.getListTXHashes().add(hash);

            String hashString = AppServiceProvider.getSerializationService().getHashString(transaction, true);
            AppServiceProvider.getTransactionService().signTransaction(transaction, key.getValue());
            AppServiceProvider.getBlockchainService().put(hashString, transaction, blockchain, BlockchainUnitType.TRANSACTION);


            byte[] blockHash = AppServiceProvider.getSerializationService().getHash(block, true);
            String blockHashString = AppServiceProvider.getSerializationService().getHashString(block, true);
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

        AccountState acc1 = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromHexaString("0xAABBCC0b727509404c8f0ffa2f9e5344744794CC"), accounts);
        TestCase.assertEquals(acc1.getBalance(),BigInteger.valueOf(100));


        AccountState acc2 = AppServiceProvider.getAccountStateService().getAccountState(AccountAddress.fromHexaString("0x940fdf49dd15eb830ea9c65282a47def5982d53a"), accounts);
        TestCase.assertEquals(acc2.getBalance(), BigInteger.valueOf(123456689));


    }

    private Accounts initAccounts() throws IOException, ClassNotFoundException {
        AccountsContext accountContext = new AccountsContext();
        accountContext.setDatabasePath("blockchain.account.data-test");
        Accounts accounts = new Accounts(accountContext);

        AccountAddress address = AccountAddress.fromHexaString("0x940fdf49dd15eb830ea9c65282a47def5982d53a");
        AccountState accountState = AppServiceProvider.getAccountStateService()
                .getOrCreateAccountState(address, accounts);
        accountState.setBalance(BigInteger.valueOf(123456789));

        AppServiceProvider.getAccountStateService().setAccountState(address, accountState, accounts);

        return accounts;
    }
}
