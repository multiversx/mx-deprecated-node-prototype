package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.MultiSignatureService;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class ExecutionServiceTest extends BaseBlockchainTest {
    private Blockchain blockchain;
    private Accounts accounts;
    private Logger logger = LoggerFactory.getLogger(ExecutionServiceTest.class);


    @Before
    public void setUp() throws IOException {
        AccountsContext accountsContext = new AccountsContext();
        accountsContext.setDatabasePath("blockchain.account.data-test");

        if (blockchain != null) {
            blockchain.flush();
        }

        blockchain = new Blockchain(getDefaultTestBlockchainContext());
        accounts = new Accounts(accountsContext);
    }

    @After
    public void tearDown() throws IOException {
        accounts.getAccountsPersistenceUnit().close();
    }

    private Block generateUnsignedBlockWithTransactions(PrivateKey privateKeyMint,
                                                        PublicKey publicKeyMint,
                                                        ArrayList<PublicKey> publicKeysWallets,
                                                        ArrayList<String> signers,
                                                        byte[] prevBlockHash,
                                                        long mintValue,
                                                        int numberOfTransactions,
                                                        long valuePerTransaction) throws IOException, ClassNotFoundException {
        TransactionService transactionService = AppServiceProvider.getTransactionService();
        SerializationService serializationService = AppServiceProvider.getSerializationService();
        Transaction tx;
        Block block = new Block();
        ArrayList<byte[]> transactionHashes = new ArrayList<>();
        PublicKey publicKeyReceiver = null;
        String addressSender = Util.getAddressFromPublicKey(publicKeyMint.getValue());
        String addressReceiver;
        String hashStr;
        byte[] hash;
        byte[] appStateHash = new byte[0];
        AccountsContext accountsContext = new AccountsContext();
        accountsContext.setDatabasePath("blockchain.account.data-test-tmp");
        Accounts accountsSandbox = new Accounts(accountsContext);

        for (PublicKey pkWallet : publicKeysWallets) {
            if (!pkWallet.equals(publicKeyMint)) {
                publicKeyReceiver = pkWallet;
                break;
            }
        }

        addressReceiver = Util.getAddressFromPublicKey(publicKeyReceiver.getValue());
        accountsSandbox = initAccounts(accountsSandbox, publicKeysWallets, publicKeyMint, mintValue);

        for (int i = 0; i < numberOfTransactions; i++) {
            tx = new Transaction(addressSender, addressReceiver, BigInteger.valueOf(valuePerTransaction), BigInteger.valueOf(i));
            tx.setPubKey(Util.byteArrayToHexString(publicKeyMint.getValue()));
            tx.setData(new byte[0]);
            transactionService.signTransaction(tx, privateKeyMint.getValue());
            hashStr = serializationService.getHashString(tx);
            hash = serializationService.getHash(tx);
            transactionHashes.add(hash);
            AppServiceProvider.getBlockchainService().put(hashStr, tx, blockchain, BlockchainUnitType.TRANSACTION);
            // execute transactions to get the accountState
            AppServiceProvider.getExecutionService().processTransaction(tx, accountsSandbox);
        }

        block.setAppStateHash(accountsSandbox.getAccountsPersistenceUnit().getRootHash());
        block.setNonce(BigInteger.valueOf(1));
        block.setPrevBlockHash(prevBlockHash);
        block.setListPubKeys(signers);
        block.setListTXHashes(transactionHashes);

        return block;
    }

    private Accounts initAccounts(Accounts accounts, ArrayList<PublicKey> publicKeysWallets, PublicKey publicKeyMint, long mintValue) throws IOException, ClassNotFoundException {
        AccountAddress address;
        AccountState accountState;

        for (PublicKey pk : publicKeysWallets) {
            address = AccountAddress.fromPublicKey(pk);
            accountState = AppServiceProvider.getAccountStateService()
                    .getOrCreateAccountState(address, accounts);
            if (pk.equals(publicKeyMint)) {
                accountState.setBalance(BigInteger.valueOf(mintValue));
            } else {
                accountState.setBalance(BigInteger.valueOf(0));
            }

            AppServiceProvider.getAccountStateService().setAccountState(address, accountState, accounts);
        }
        return accounts;
    }

    /**
     * simulate multi-signing
     * returns an array with two elements first element the commitment as a byte array, the second the signature as a byte array
     */
    private byte[][] multiSignBlock(byte[] blockHashNoSig, ArrayList<String> signersPublicKeysStr, ArrayList<byte[]> signersPrivateKeys, long bitmap) {
        ArrayList<byte[]> signersPublicKeys = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        ArrayList<byte[]> challenges = new ArrayList<>();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] aggregatedSignature = new byte[0];
        int sizeConsensusGroup = signersPublicKeysStr.size();
        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();

        byte[][] result = new byte[2][];

        for (int i = 0; i < sizeConsensusGroup; i++) {
            signersPublicKeys.add(Util.hexStringToByteArray(signersPublicKeysStr.get(i)));
            commitmentSecrets.add(multiSignatureService.computeCommitmentSecret());
            commitments.add(multiSignatureService.computeCommitment(commitmentSecrets.get(i)));
        }

        // aggregate the commitments
        aggregatedCommitment = multiSignatureService.aggregateCommitments(commitments, bitmap);

        // compute challenges and signatures for each signer
        for (int i = 0; i < sizeConsensusGroup; i++) {
            if (0 != ((1 << i) & bitmap)) {
                challenges.add(
                        multiSignatureService.computeChallenge(
                                signersPublicKeys,
                                signersPublicKeys.get(i),
                                aggregatedCommitment,
                                blockHashNoSig,
                                bitmap
                        )
                );

                // compute signature shares
                signatureShares.add(
                        multiSignatureService.computeSignatureShare(
                                challenges.get(i),
                                signersPrivateKeys.get(i),
                                commitmentSecrets.get(i)
                        )
                );
            } else {
                challenges.add(new byte[0]);
                signatureShares.add(new byte[0]);
            }

            aggregatedSignature = multiSignatureService.aggregateSignatures(signatureShares, bitmap);
        }
        result[0] = aggregatedSignature;
        result[1] = aggregatedCommitment;

        return result;
    }

    private Block generateSignedBlockWithTransactions(long MINT_VALUE, int numberOfTransactions, int valuePerTransaction) {
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        SerializationService serializationService = AppServiceProvider.getSerializationService();
        ArrayList<String> signersPubKeysStr = new ArrayList<>();
        ArrayList<byte[]> signersPrivKeys = new ArrayList<>();

        PrivateKey privateKeySender = new PrivateKey();
        PublicKey publicKeySender = new PublicKey(privateKeySender);
        PrivateKey privateKeyReceiver = new PrivateKey();
        PublicKey publicKeyReceiver = new PublicKey(privateKeyReceiver);

        Block block = new Block();
        String blockHashStr = serializationService.getHashString(block);
        byte[] blockHash = serializationService.getHash(block);

        long bitmap;
        byte[][] blockSignature;

        // add a dummy previous block in blockchain
        try {
            blockchainService.put(blockHashStr, block, blockchain, BlockchainUnitType.BLOCK);
        } catch (IOException ex) {
            TestCase.fail("Unexpected exception: " + ex.getMessage());
        }

        // generate and sign a new block
        signersPubKeysStr.add(Util.byteArrayToHexString(publicKeySender.getValue()));
        signersPrivKeys.add(privateKeySender.getValue());
        bitmap = (1 << signersPubKeysStr.size()) - 1;

        try {
            ArrayList<PublicKey> pubKeysWallets = new ArrayList<>();
            pubKeysWallets.add(publicKeyReceiver);
            pubKeysWallets.add(publicKeySender);

            accounts = initAccounts(accounts, pubKeysWallets, publicKeySender, MINT_VALUE);
            block = generateUnsignedBlockWithTransactions(
                    privateKeySender,
                    publicKeySender,
                    pubKeysWallets,
                    signersPubKeysStr,
                    blockHash,
                    MINT_VALUE,
                    numberOfTransactions,
                    valuePerTransaction);
        } catch (Exception ex) {
            TestCase.fail("Unexpected exception: " + ex.getMessage());
        }

        block.setSignature(null);
        block.setCommitment(null);

        // get new block hash
        blockHash = serializationService.getHash(block);

        // multi-sign block
        blockSignature = multiSignBlock(blockHash, signersPubKeysStr, signersPrivKeys, bitmap);
        block.setSignature(blockSignature[0]);
        block.setCommitment(blockSignature[1]);

        return block;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessBlockNullAccounts() {
        Accounts accounts = null;
        Block block = new Block();
        ExecutionService executionService = AppServiceProvider.getExecutionService();
        executionService.processBlock(block, accounts, blockchain);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessBlockNullBlockchain() {
        Block block = new Block();
        Blockchain blockchain = null;
        ExecutionService executionService = AppServiceProvider.getExecutionService();
        executionService.processBlock(block, accounts, blockchain);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessBlockNullBlock() {
        Block block = null;
        ExecutionService executionService = AppServiceProvider.getExecutionService();
        executionService.processBlock(block, accounts, blockchain);
    }

    @Test
    public void testProcessBlockAlreadyInBlockchain() {
        Block block = new Block();
        block.setSignature(null);
        block.setCommitment(null);
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        SerializationService serializationService = AppServiceProvider.getSerializationService();
        String hashBlock;

        //add a nonce different than 0
        // 0 is used for genesis
        block.setNonce(BigInteger.valueOf(10));
        // add block to blockchain
        hashBlock = serializationService.getHashString(block);

        try {
            blockchainService.put(hashBlock, block, blockchain, BlockchainUnitType.BLOCK);
        } catch (IOException ex) {
            TestCase.fail("unexpected exception: " + ex.getMessage());
        }

        ExecutionService executionService = AppServiceProvider.getExecutionService();
        ExecutionReport report = executionService.processBlock(block, accounts, blockchain);

        // waiting for failure due to block already present in blockchain
        if (report.isOk()) {
            TestCase.fail();
        }
    }

    @Test
    public void testProcessPrevBlockNotInBlockchain() {
        Block block = new Block();
        block.setSignature(null);
        block.setCommitment(null);

        //add a nonce different than 0
        // 0 is used for genesis
        block.setNonce(BigInteger.valueOf(10));

        ExecutionService executionService = AppServiceProvider.getExecutionService();
        ExecutionReport report = executionService.processBlock(block, accounts, blockchain);

        // waiting for failure due to missing previous block in blockchain
        if (report.isOk()) {
            TestCase.fail();
        }
    }

    @Test
    public void testProcessBlockConsensusGroupNotTwoThirds() {
        // TODO:
        logger.warn("To be implemented");
    }

    @Test
    public void testProcessBlockConsensusGroupNotValidForRound() {
        // TODO:
        logger.warn("To be implemented");
    }

    @Test
    public void testProcessBlockInvalidTransactionInBlock() {
        Block block = generateSignedBlockWithTransactions(100, 3, 50);
        //execute
        ExecutionService executionService = AppServiceProvider.getExecutionService();
        ExecutionReport report = executionService.processBlock(block, accounts, blockchain);

        // waiting for failure due to missing previous block in blockchain
        if (report.isOk()) {
            TestCase.fail();
        }
    }

    @Test
    public void testProcessBlockOK() {
        Block block = generateSignedBlockWithTransactions(100000, 100, 100);
        //execute
        ExecutionService executionService = AppServiceProvider.getExecutionService();
        ExecutionReport report = executionService.processBlock(block, accounts, blockchain);

        // waiting for failure due to missing previous block in blockchain
        if (!report.isOk()) {
            TestCase.fail();
        }
    }
}
