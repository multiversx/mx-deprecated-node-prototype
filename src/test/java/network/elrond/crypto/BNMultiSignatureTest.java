package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class BNMultiSignatureTest {

    @Test
    public void testSignVerify() {
        // e.g 21 maximum signers
        final int CONSENSUS_GROUP_SIZE = 21;
        final int MASK_BITMAP = (1 << CONSENSUS_GROUP_SIZE) - 1;
        final int CONSENSUS_MALICIOUS = ((CONSENSUS_GROUP_SIZE) - 3) / 3;
        final int SIGNING_ROUNDS = 50;

        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();
        ArrayList<ECKeyPair> signers = new ArrayList<>();
        ArrayList<PublicKey> signersPublicKeys = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        ArrayList<byte[]> challenges = new ArrayList<>();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] aggregatedSignature;
        byte[] msg_to_sign = "World’s First High Throughput Blockchain Platform Implementing Adaptive State Sharding and Secure Proof of Stake".getBytes();
        byte[] msg_to_sign_hash = Util.SHA3.digest(msg_to_sign);
        Random rand = new Random();
        int random;
        int chosen;
        ECKeyPair ecKeyPair;
        long bitmap;

        //different multi signing rounds
        for (int sIdx = 0; sIdx < SIGNING_ROUNDS; sIdx++) {
            signers.clear();
            signersPublicKeys.clear();
            commitmentSecrets.clear();
            commitments.clear();
            challenges.clear();
            signatureShares.clear();
            bitmap = MASK_BITMAP;

            System.out.println("Start multi-signing process");

            for (int pubKeyIdx = 0; pubKeyIdx < CONSENSUS_GROUP_SIZE; pubKeyIdx++) {
                ecKeyPair = new ECKeyPair();
                signers.add(ecKeyPair);
                signersPublicKeys.add(ecKeyPair.getPublicKey());
            }

            // generate a bitmap for (2/3) + 1 signers out of all consensus members
            for (int chIdx = 0; chIdx < CONSENSUS_MALICIOUS; chIdx++) {
                // getAccountState random 15 out of 21
                random = rand.nextInt(CONSENSUS_GROUP_SIZE);
                chosen = (1 << random) ^ MASK_BITMAP;
                while ((bitmap & chosen) == bitmap) {
                    random = rand.nextInt(CONSENSUS_GROUP_SIZE);
                    chosen = (1 << random) ^ MASK_BITMAP;
                }
                bitmap &= chosen;
            }

            System.out.println("bitmap: " + Long.toBinaryString(bitmap));

            // start multi-signing process
            // compute commitment secrets and commitments for each signer
            for (int i = 0; i < CONSENSUS_GROUP_SIZE; i++) {
                commitmentSecrets.add(multiSignatureService.computeCommitmentSecret());
                commitments.add(multiSignatureService.computeCommitment(commitmentSecrets.get(i)));
            }

            // aggregate the commitments
            aggregatedCommitment = multiSignatureService.aggregateCommitments(commitments, bitmap);
            System.out.println("aggregated commitment: " + Util.byteArrayToHexString(aggregatedCommitment));

            // compute challenges and signatures for each signer
            for (int i = 0; i < CONSENSUS_GROUP_SIZE; i++) {
                if (0 != ((1 << i) & bitmap)) {
                    challenges.add(
                            multiSignatureService.computeChallenge(
                                    signersPublicKeys,
                                    signersPublicKeys.get(i),
                                    aggregatedCommitment,
                                    msg_to_sign_hash,
                                    bitmap
                            )
                    );

                    // compute signature shares
                    signatureShares.add(
                            multiSignatureService.computeSignatureShare(
                                    challenges.get(i),
                                    signers.get(i).getPrivateKey(),
                                    commitmentSecrets.get(i)
                            )
                    );

                    // verify each of the shares, for test only
                    // in algorithm only check aggregated signature
                    TestCase.assertTrue(multiSignatureService.verifySignatureShare(
                            signersPublicKeys,
                            signersPublicKeys.get(i),
                            signatureShares.get(i),
                            aggregatedCommitment,
                            commitments.get(i),
                            msg_to_sign_hash,
                            bitmap));

                } else {
                    challenges.add(new byte[0]);
                    signatureShares.add(new byte[0]);
                }
            }
            // aggregate signature
            aggregatedSignature = multiSignatureService.aggregateSignatures(signatureShares, bitmap);
            System.out.println("aggregated signature: " + Util.byteArrayToHexString(aggregatedSignature));
            System.out.println();
            TestCase.assertTrue(multiSignatureService.verifyAggregatedSignature(
                    signersPublicKeys,
                    aggregatedSignature,
                    aggregatedCommitment,
                    msg_to_sign_hash,
                    bitmap));
        }
    }
}
