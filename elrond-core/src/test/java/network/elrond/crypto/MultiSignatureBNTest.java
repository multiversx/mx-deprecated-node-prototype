package network.elrond.crypto;

import junit.framework.TestCase;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MultiSignatureBNTest {

    @Test
    public void testComputeCommitmentSecretNotZero() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitmentSecret = signatureService.computeCommitmentSecret();
        BigInteger secret = new BigInteger(commitmentSecret);

        TestCase.assertNotSame(BigInteger.ZERO, secret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeCommitmentNullSecret() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitmentSecret = null;

        signatureService.computeCommitment(commitmentSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeCommitmentEmptySecret() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitmentSecret = new byte[0];

        signatureService.computeCommitment(commitmentSecret);
    }

    @Test
    public void testComputeCommitmentHash() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitment = signatureService.computeCommitment(signatureService.computeCommitmentSecret());
        byte[] commitmentHash = signatureService.computeCommitmentHash(commitment);

        TestCase.assertFalse(Arrays.equals(commitment, commitmentHash));
    }

    @Test
    public void testValidateCommitmentValid() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitment = signatureService.computeCommitment(signatureService.computeCommitmentSecret());
        byte[] commitmentHash = signatureService.computeCommitmentHash(commitment);

        TestCase.assertTrue(signatureService.validateCommitment(commitment, commitmentHash));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregateCommitmentsNullCommitments() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 3;
        ArrayList<byte[]> commitments = null;

        signatureService.aggregateCommitments(commitments, bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregateCommitmentsEmptyCommitments() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 3;
        ArrayList<byte[]> commitments = new ArrayList<>();

        signatureService.aggregateCommitments(commitments, bitmap);
    }

    @Test
    public void testAggregatedCommitment() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> commitments = new ArrayList<>();

        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));

        TestCase.assertEquals("02534d4371d6ea9f8b856a632e4e31d784eec9120b3252080702d872c696012289",
                Util.byteArrayToHexString(signatureService.aggregateCommitments(commitments, bitmap)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeNullPublicKeys() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = null;
        byte[] publicKey = new PublicKey(new PrivateKey()).getValue();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment;
        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));

        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);
        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, "hello".getBytes(), bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeEmptyPublicKeys() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        byte[] publicKey = new PublicKey(new PrivateKey()).getValue();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment;
        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));

        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);
        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, "hello".getBytes(), bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeNullPublicKey() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        byte[] publicKey = null;
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment;

        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));
        publicKeys.add(new PublicKey(new PrivateKey()).getValue());
        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);

        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, "hello".getBytes(), bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeNullAggregatedCommitment() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        byte[] publicKey = new PublicKey(new PrivateKey()).getValue();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment;

        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));
        publicKeys.add(new PublicKey(new PrivateKey()).getValue());
        publicKeys.add(new PublicKey((new PrivateKey())).getValue());
        publicKeys.add(publicKey);
        aggregatedCommitment = null;

        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, "hello".getBytes(), bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeEmptyAggregatedCommitment() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        byte[] publicKey = new PublicKey(new PrivateKey()).getValue();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment = new byte[0];

        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));
        publicKeys.add(new PublicKey(new PrivateKey()).getValue());
        publicKeys.add(new PublicKey((new PrivateKey())).getValue());
        publicKeys.add(publicKey);

        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, "hello".getBytes(), bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeNullMessage() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        byte[] publicKey = new PublicKey(new PrivateKey()).getValue();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] message = null;

        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));
        publicKeys.add(new PublicKey(new PrivateKey()).getValue());
        publicKeys.add(new PublicKey((new PrivateKey())).getValue());
        publicKeys.add(publicKey);

        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);
        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, message, bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeChallengeEmptyMessage() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        long bitmap = 0b111;
        ArrayList<byte[]> publicKeys = new ArrayList<>();
        byte[] publicKey = new PublicKey(new PrivateKey()).getValue();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] message = "".getBytes();

        commitments.add(Util.hexStringToByteArray("02181b4df800671642e3df9a953a29a4f571acc1bf0714ed5ae714a9804d97079f"));
        commitments.add(Util.hexStringToByteArray("02e8196913323fbb7a34d9455b778e877e1d1fa0205b5949504e55a2d999931366"));
        commitments.add(Util.hexStringToByteArray("02ef67409f09053060e79d8ad5b1fe60690b5eaa35b67f071ca111a0a7edeb6b38"));
        publicKeys.add(new PublicKey(new PrivateKey()).getValue());
        publicKeys.add(new PublicKey((new PrivateKey())).getValue());
        publicKeys.add(publicKey);

        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);
        signatureService.computeChallenge(publicKeys, publicKey, aggregatedCommitment, message, bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeSignatureShareNullChallenge() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] challenge = null;
        byte[] privateKey = new PrivateKey().getValue();
        byte[] commitmentSecret = signatureService.computeCommitmentSecret();

        signatureService.computeSignatureShare(challenge, privateKey, commitmentSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeSignatureShareEmptyChallenge() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] challenge = new byte[0];
        byte[] privateKey = new PrivateKey().getValue();
        byte[] commitmentSecret = signatureService.computeCommitmentSecret();

        signatureService.computeSignatureShare(challenge, privateKey, commitmentSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeSignatureShareNullPrivateKey() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] privateKey = null;
        byte[] commitmentSecret = signatureService.computeCommitmentSecret();

        signatureService.computeSignatureShare(challenge, privateKey, commitmentSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeSignatureShareEmptyPrivateKey() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitmentSecret = signatureService.computeCommitmentSecret();
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] privateKey = new byte[0];

        signatureService.computeSignatureShare(challenge, privateKey, commitmentSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeSignatureShareNullCommitmentSecret() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] privateKey = new PrivateKey().getValue();;
        byte[] commitmentSecret = null;

        signatureService.computeSignatureShare(challenge, privateKey, commitmentSecret);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComputeSignatureShareEmptyCommitmentSecret() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        byte[] commitmentSecret = new byte[0];
        byte[] challenge = SHA3Helper.sha3("dummy challenge".getBytes());
        byte[] privateKey = new PrivateKey().getValue();

        signatureService.computeSignatureShare(challenge, privateKey, commitmentSecret);
    }

    @Test
    public void testVerifySignatureShareOK() {
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        ArrayList<byte[]> signers = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        byte[] privateKey = new PrivateKey().getValue();
        byte[] publicKey = new PublicKey(new PrivateKey(privateKey)).getValue();
        byte[] challenge;
        byte[] aggregatedCommitment;
        long bitmap = 0;
        byte[] message = "Hello Elrond".getBytes();
        byte[] signature;

        signers.add(publicKey);
        signers.add(new PublicKey(new PrivateKey()).getValue());
        signers.add(new PublicKey(new PrivateKey()).getValue());

        for(int i = 0; i< signers.size(); i++) {
            bitmap = (bitmap << 1) | 1;
            commitmentSecrets.add(signatureService.computeCommitmentSecret());
            commitments.add(signatureService.computeCommitment(commitmentSecrets.get(i)));
        }

        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);
        challenge = signatureService.computeChallenge(signers, publicKey, aggregatedCommitment, message, bitmap);
        signature = signatureService.computeSignatureShare(challenge, privateKey, commitmentSecrets.get(0));
        TestCase.assertTrue(
                signatureService.verifySignatureShare(
                        signers,
                        publicKey,
                        signature,
                        aggregatedCommitment,
                        commitments.get(0),
                        message,
                        bitmap));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregateSignaturesNullSignatureShares(){
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        ArrayList<byte[]> signatureShares = null;
        long bitmap = 0b111;

        signatureService.aggregateSignatures(signatureShares, bitmap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAggregateSignaturesEmptySignatureShares(){
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        long bitmap = 0b111;

        signatureService.aggregateSignatures(signatureShares, bitmap);
    }

    @Test
    public void testAggregateSignaturesOK(){
        MultiSignatureService signatureService = AppServiceProvider.getMultiSignatureService();
        ArrayList<byte[]> signers = new ArrayList<>();
        ArrayList<byte[]> privateKeys = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        ArrayList<byte[]> challenges = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] message = "Hello Elrond".getBytes();
        byte[] aggregatedSignature;
        long bitmap = 0b111;

        privateKeys.add(Util.hexStringToByteArray("00f0d3f52e126349dc8523c6c91385222fcf4c60a9df9214bfdf8844896a75998b"));
        privateKeys.add(Util.hexStringToByteArray("3ecc42f09822043b8a32fef767e780c3f370fdf9f014ee15d44af642f8f08646"));
        privateKeys.add(Util.hexStringToByteArray("00d171f06d911ba66ad35c2c800f9864c7e22e3ac5b149db7f3712e31c74cda568"));

        commitmentSecrets.add(Util.hexStringToByteArray("3f9d94eaa0edf5652b5f738255a59970aa2c78a5d828a0ebc82b9eb3b1109696"));
        commitmentSecrets.add(Util.hexStringToByteArray("1fc5f4cd690f50be2442899d85e8971fbf2d49fb8a6a679cfc86bacb9a8f7f24"));
        commitmentSecrets.add(Util.hexStringToByteArray("54892cd9275b92d169b376b44281af5611b7c6b4f9c7cce45d478df4761dba36"));

        for(int i=0; i< commitmentSecrets.size(); i++) {
            signers.add(new PublicKey(new PrivateKey(privateKeys.get(i))).getValue());
            commitments.add(signatureService.computeCommitment(commitmentSecrets.get(i)));
        }

        aggregatedCommitment = signatureService.aggregateCommitments(commitments, bitmap);

        for(int i=0; i< signers.size(); i++){
            challenges.add(signatureService.computeChallenge(signers, signers.get(0), aggregatedCommitment, message, bitmap));
            signatureShares.add(signatureService.computeSignatureShare(challenges.get(i), privateKeys.get(i), commitmentSecrets.get(i)));
        }

        aggregatedSignature = signatureService.aggregateSignatures(signatureShares, bitmap);
        TestCase.assertEquals("0090abcf904cc8be814e73f19e302f3b889e3b4267b6127682d610aa5379157bc4",
                Util.byteArrayToHexString(aggregatedSignature));
    }

    @Test
    public void testSignVerify() {
        // e.g 21 maximum signers
        final int CONSENSUS_GROUP_SIZE = 21;
        final int MASK_BITMAP = (1 << CONSENSUS_GROUP_SIZE) - 1;
        final int CONSENSUS_MALICIOUS = ((CONSENSUS_GROUP_SIZE) - 3) / 3;
        final int SIGNING_ROUNDS = 20;

        MultiSignatureService multiSignatureService = AppServiceProvider.getMultiSignatureService();
        ArrayList<ECKeyPair> signers = new ArrayList<>();
        ArrayList<byte[]> signersPublicKeys = new ArrayList<>();
        ArrayList<byte[]> commitmentSecrets = new ArrayList<>();
        ArrayList<byte[]> commitments = new ArrayList<>();
        ArrayList<byte[]> challenges = new ArrayList<>();
        ArrayList<byte[]> signatureShares = new ArrayList<>();
        byte[] aggregatedCommitment;
        byte[] aggregatedSignature;
        byte[] msg_to_sign = "World’s First High Throughput Blockchain Platform Implementing Adaptive State Sharding and Secure Proof of Stake".getBytes();
        byte[] msg_to_sign_hash = SHA3Helper.sha3(msg_to_sign);
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
                signersPublicKeys.add(ecKeyPair.getPublicKey().getValue());
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
                                    signers.get(i).getPrivateKey().getValue(),
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
