package network.elrond.crypto;


import network.elrond.core.Util;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class implementing Belare and Neven Multi-signature
 */
public class BNMultiSignature implements MultiSignature {
    private static SecureRandom secureRandom;
    private BigInteger commitmentSecret;
    private Signature signature;

    static {
        byte[] seed;
        secureRandom = new SecureRandom();
        seed = secureRandom.generateSeed(32);
        secureRandom.setSeed(seed);
    }

    /**
     * Calculate random commitment
     *
     * @return commitment as a byte array
     */
    @Override
    public byte[] computeCommitment() {
        // choose a random r (commitment secret) in interval[2, n-1], where n is the order of the curve
        byte[] r = new byte[32];
        secureRandom.nextBytes(r);
        commitmentSecret = new BigInteger(1, r);
        ECPoint basePointG;
        ECPoint commitmentPointR;

        // make sure k is not 0
        while (commitmentSecret.equals(BigInteger.ZERO)) {
            r = Util.SHA3.digest(r);
            commitmentSecret = new BigInteger(1, r);
        }

        //remove possible extra byte for unsigned
        commitmentSecret = new BigInteger(r);

        // compute commitment R = r*G
        basePointG = PrivateKey.getEcParameters().getG();
        commitmentPointR = basePointG.multiply(commitmentSecret);

        return commitmentPointR.getEncoded(true);
    }

    /**
     * Computes the commitment Hash
     *
     * @param commitment the commitment as a byte array
     * @return commitment hash as a byte array
     */
    @Override
    public byte[] computeCommitmentHash(byte[] commitment) {
        // Hash function needs to be different than what is used
        // for challenge so use SHA256 for commitment
        return Util.SHA256.digest(commitment);
    }

    /**
     * Verifies the commitmentHash is resulted from commitment
     *
     * @param commitment     commitment as a byte array
     * @param commitmentHash the commitment hash as a byte array
     * @return true if commitmentHash is the Hash of commitment, false otherwise
     */
    @Override
    public boolean validateCommitment(byte[] commitment, byte[] commitmentHash) {
        byte[] computedHash = Util.SHA256.digest(commitment);
        return Arrays.equals(computedHash, commitmentHash);
    }

    /**
     * Calculates the challenge according to Belare Naveen multi-signature algorithm:
     * H1(<L'>||Xi||R||m), where H1 is a Hashing function, e.g Sha3, Xi is the public key,
     * R is the aggregated commitment, and m is the message.
     *
     * @param signers           the list of signers's (consensus group's) public keys
     * @param commitment        the list of commitments for each member of the consensus group
     * @param publicKey         own public key
     * @param message           the message to be signed
     * @param bitmapCommitments commitment mask (byte), bit is 1 if corresponding signer participates in signing
     *                          or 0 otherwise
     * @return the challenge as a byte array
     */
    public byte[] computeChallenge(ArrayList<PublicKey> signers,
                                   ArrayList<byte[]> commitment,
                                   PublicKey publicKey,
                                   byte[] message,
                                   byte bitmapCommitments) {
        int idx = 0;
        int factor = 0;
        byte[] challenge = new byte[0];
        ECPoint aggregatedCommit = null;
        X9ECParameters ecParameters = PrivateKey.getEcParameters();

        // computing <L'> as concatenation of participating signers public keys
        for (PublicKey key : signers) {
            factor = (1 << idx) & bitmapCommitments;

            if (0 != factor) {
                // concatenate the public keys
                challenge = Util.concatenateArrays(challenge, key.getEncoded());

                // aggregate the commits
                if (null == aggregatedCommit) {
                    aggregatedCommit = ecParameters.getCurve().decodePoint(commitment.get(idx));
                } else {
                    aggregatedCommit.add(ecParameters.getCurve().decodePoint(commitment.get(idx)));
                }
            }
            idx++;
        }

        // do rest of concatenation <L'> || public key
        challenge = Util.concatenateArrays(challenge, publicKey.getQ().getEncoded(true));
        // <L'> || public key || R
        challenge = Util.concatenateArrays(challenge, aggregatedCommit.getEncoded(true));
        // <L'> || public key || R || m
        challenge = Util.concatenateArrays(challenge, message);
        // compute hash
        challenge = Util.SHA3.digest(challenge);

        return challenge;
    }

    /**
     * Calculates the signature share associated to this private key according to formula:
     * s = ri - challenge * xi, where ri is the private part of the commitment, xi is own
     * private key, and challenge is the calculated
     *
     * @param challenge  the calculated challenge associated with own public key
     * @param privateKey the own private key
     * @return the signature share
     */
    @Override
    public byte[] computeSignatureShare(byte[] challenge, PrivateKey privateKey) {
        BigInteger sigShare;
        BigInteger challengeInt = new BigInteger(challenge);
        BigInteger privateKeyInt = new BigInteger(privateKey.getValue());

        sigShare = commitmentSecret.subtract(challengeInt.multiply(privateKeyInt));
        return sigShare.toByteArray();
    }

    /**
     * Aggregates the signature shares according to the participating signers
     *
     * @param signatureShares the list of signature shares
     * @param bitmapSigners   the participating signers as a bitmap (byte)
     * @return the aggregated signature
     */
    @Override
    public byte[] AggregateSignatures(ArrayList<byte[]> signatureShares, byte bitmapSigners) {
        byte idx = 0;
        BigInteger aggregatedSignature = BigInteger.ZERO;

        for (byte[] signature : signatureShares) {
            if (0 != ((1 << idx) & bitmapSigners)) {
                aggregatedSignature = aggregatedSignature.add(new BigInteger(signature));
            }
            idx++;
        }
        return aggregatedSignature.toByteArray();
    }

    /**
     * Verifies a multi-signature as below:
     * s*G == R + sum(H1(<L'> || Xi || R || m)*Xi*Bitmap[i]), where:
     * -   s is the aggregated signature
     * -   G is the base point on the chosen curve
     * -   H1 is the Hash function, different than one used for the commitment hash
     * -   <L'> is the set of all signers's public keys (the consensus group used in SPoS)
     * -   Xi is the public key for signer i
     * -   R is the aggregated commitment
     * -   m is the message that was signed with s
     * -   Bitmap[i] the i bit inside a bitmap, set to 1 if signer i in <L'> has signed or 0 otherwise
     *
     * @param signers              an ArrayList containing all possible signers's public keys
     * @param aggregatedCommitment the aggregated commitment
     * @param aggregatedSignature  the aggregated signature to be verified
     * @param bitmapSigners        the bitmap of signers
     * @param message              the message on which the signature was calculated
     * @return true if aggregated signature is valid, false otherwise
     */
    @Override
    public boolean VerifyAggregatedSignature(ArrayList<PublicKey> signers,
                                             byte[] aggregatedCommitment,
                                             byte[] aggregatedSignature,
                                             byte bitmapSigners,
                                             byte[] message) {
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECPoint aggregatedCommitmentPoint = ecParameters.getCurve().decodePoint(aggregatedCommitment);
        int idx = 0;
        byte[] concatenatedResult = new byte[0];
        ECPoint point1 = null;
        ECPoint point2 = null;

        //concatenate signers to calculate <L'>
        for (PublicKey publicKey : signers) {
            if (0 != ((1 << idx) & bitmapSigners)) {
                concatenatedResult = Util.concatenateArrays(concatenatedResult, publicKey.getQ().getEncoded(true));
            }
        }

        // compute sum(H1(<L'> || Xi || R || m)*Xi*Bitmap[i])
        idx = 0;
        for (PublicKey publicKey : signers) {
            if (0 != ((1 << idx) & bitmapSigners)) {
                // <L'> || Xi
                concatenatedResult = Util.concatenateArrays(concatenatedResult, publicKey.getQ().getEncoded(true));
                // <L'> || Xi || R
                concatenatedResult = Util.concatenateArrays(concatenatedResult, aggregatedCommitment);
                // <L'> || Xi || R || m
                concatenatedResult = Util.concatenateArrays(concatenatedResult, message);
                // compute the hash (H1 = sha3) on the result
                concatenatedResult = Util.SHA3.digest(concatenatedResult);
                // do the sum
                if (null == point1) {
                    // H1 * Xi * Bitmap[i]
                    point1 = publicKey.getQ().multiply(new BigInteger(concatenatedResult));
                } else {
                    point1 = point1.add(publicKey.getQ().multiply(new BigInteger(concatenatedResult)));
                }
            }
        }

        // add R to the sum
        point1 = point1.add(aggregatedCommitmentPoint);
        // calculate s*G
        point2 = ecParameters.getG().multiply(new BigInteger(aggregatedSignature));

        return point1.equals(point2);
    }
}
