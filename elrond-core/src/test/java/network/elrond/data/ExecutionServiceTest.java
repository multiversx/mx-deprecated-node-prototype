package network.elrond.data;

import junit.framework.TestCase;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.core.Util;
import network.elrond.crypto.MultiSignatureService;
import network.elrond.crypto.PrivateKey;
import network.elrond.crypto.PublicKey;
import network.elrond.service.AppServiceProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class ExecutionServiceTest extends BaseBlockchainTest{
    Blockchain blockchain;

    @Before
    public void setUp() throws IOException {
        if (blockchain != null) {
            blockchain.flush();
        }

        blockchain = new Blockchain(getDefaultTestBlockchainContext());
    }

    private Block generatBlockWithTransactions(PrivateKey privateKeySenderTx,
                                               PublicKey publicKeySenderTx,
                                               PublicKey publicKeyReceiver,
                                               ArrayList<String> signers,
                                               byte[] prevBlockHash,
                                               int numberOfTransactions,
                                               long valuePerTransaction) throws IOException {
        TransactionService transactionService = AppServiceProvider.getTransactionService();
        SerializationService serializationService = AppServiceProvider.getSerializationService();
        Transaction tx = new Transaction();
        Block block = new Block();
        ArrayList<byte[]> transactionHashes = new ArrayList<>();
        String addressSender = Util.getAddressFromPublicKey(publicKeySenderTx.getValue());
        String addressReceiver = Util.getAddressFromPublicKey(publicKeyReceiver.getValue());
        byte[] hash;

        for(int i = 0; i < numberOfTransactions; i++) {
            tx.setNonce(BigInteger.valueOf(i));
            tx.setPubKey(Util.byteArrayToHexString(publicKeySenderTx.getValue()));
            tx.setSendAddress(addressSender);
            tx.setReceiverAddress(addressReceiver);
            tx.setData(new byte[0]);
            tx.setValue(BigInteger.valueOf(valuePerTransaction));
            transactionService.signTransaction(tx, privateKeySenderTx.getValue());
            hash = serializationService.getHash(tx, true);
            transactionHashes.add(hash);
            AppServiceProvider.getBlockchainService().put(hash, tx, blockchain, BlockchainUnitType.TRANSACTION);
        }

        block.setNonce(BigInteger.valueOf(1));
        block.setPrevBlockHash(prevBlockHash);
        block.setListPubKeys(signers);
        block.setListTXHashes(transactionHashes);

        return block;
    }

    /**
     *   get the signature
     *   returns an array with two elements first element the commitment as a byte array, the second the signature as a byte array
     */
    private byte[][] multiSignBlock(byte[] blockHashNoSig, ArrayList<String> signersPublicKeysStr, ArrayList<byte[]> signersPrivateKeys, long bitmap) {
        ArrayList<byte[]> signersPublicKeys = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        ArrayList<byte[]> challenges = new ArrayList<>();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] aggregatedSignature = new byte[0];
        int sizeConsensusGroup = signersPublicKeys.size();
        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();

        byte[][] result = new byte[2][];

        for(int i = 0; i < sizeConsensusGroup; i++) {
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
        result[0] = aggregatedCommitment;
        result[1] = aggregatedSignature;

        return result;
    }

    @Test
    public void testProcessBlockAlreadyInBlockchain() {
        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
        TransactionService transactionService = AppServiceProvider.getTransactionService();
        PrivateKey privateKeySender = new PrivateKey();
        PublicKey publicKeySender = new PublicKey(privateKeySender);
        PrivateKey privateKeyReceiver = new PrivateKey();
        PublicKey publicKeyReceiver = new PublicKey(privateKeyReceiver);
        ArrayList<String> signersPubKeysStr = new ArrayList<>();
        ArrayList<byte[]> signersPrivKeys = new ArrayList<>();
        byte[] blockHash = AppServiceProvider.getSerializationService().getHash(new Block(), false);
        long bitmap;
        byte[][] blockSignature;
        Block block = new Block();

        signersPubKeysStr.add(Util.byteArrayToHexString(publicKeySender.getValue()));
        signersPrivKeys.add(privateKeySender.getValue());

        bitmap = (1 << signersPubKeysStr.size()) - 1;

        try {
            block = generatBlockWithTransactions(privateKeySender, publicKeySender, publicKeyReceiver, signersPubKeysStr, blockHash, 100, 100);
        } catch (IOException ex) {
            TestCase.fail("Unexpected exception: "+ ex.getMessage());
        }

        byte[] blockHashNoSig = AppServiceProvider.getSerializationService().getHash(new Block(), false);

        // multi-sign block
        blockSignature = multiSignBlock(blockHashNoSig, signersPubKeysStr, signersPrivKeys, bitmap);

        block.setSig1(blockSignature[0]);
        block.setSig2(blockSignature[1]);

        blockHash = AppServiceProvider.getSerializationService().getHash(new Block(), true);
        //AppServiceProvider.getBlockchainService().put();
    }
}
