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
public class BNMultiSignatureServiceImpl implements MultiSignatureService {
    private static SecureRandom secureRandom;

    static {
        byte[] seed;
        secureRandom = new SecureRandom();
        seed = secureRandom.generateSeed(32);
        secureRandom.setSeed(seed);
    }

    /**
     * Default constructor
     */
    public BNMultiSignatureServiceImpl() {
    }

    /**
     * Calculate the commitment secret
     *
     * @return commitment secret as a byte array
     */
    @Override
    public byte[] computeCommitmentSecret() {
        // choose a random r (commitment secret) in interval[1, n-1], where n is the order of the curve
        byte[] r = new byte[32];
        secureRandom.nextBytes(r);
        BigInteger commitmentSecret = new BigInteger(r);

        // make sure r is not 0
        // r below order of curve
        while (commitmentSecret.compareTo(BigInteger.ONE) < 0 ||
                commitmentSecret.compareTo(PrivateKey.getEcParameters().getN()) >= 0) {
            r = Util.SHA3.digest(r);
            commitmentSecret = new BigInteger(r);
        }
        return r;
    }

    /**
     * Calculate the commitment Point
     *
     * @param commitmentSecret the commitment secret as a byte array
     * @return commitment as a byte array
     */
    @Override
    public byte[] computeCommitment(byte[] commitmentSecret) {
        BigInteger secretInt = new BigInteger(commitmentSecret);
        ECPoint basePointG;
        ECPoint commitmentPointR;

        // compute commitment R = r*G
        basePointG = PrivateKey.getEcParameters().getG();
        commitmentPointR = basePointG.multiply(secretInt);

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
     * Calculate the aggregated commitment
     *
     * @param commitments       an array list of commitments from each signer
     * @param bitmapCommitments the bitmap of considered commitments from the whole list
     * @return the aggregated commitment
     */
    @Override
    public byte[] aggregateCommitments(ArrayList<byte[]> commitments, long bitmapCommitments) {
        int idx = 0;
        ECPoint aggregatedCommitment = null;
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECPoint decodedCommitment;
        byte[] result = new byte[0];

        for (byte[] commitment : commitments) {
            if (0 != ((1 << idx) & bitmapCommitments)) {
                // aggregate the commits
                decodedCommitment = ecParameters.getCurve().decodePoint(commitment.clone());
                if (null == aggregatedCommitment) {
                    aggregatedCommitment = decodedCommitment;
                } else {
                    aggregatedCommitment = aggregatedCommitment.add(decodedCommitment);
                }
            }
            idx++;
        }

        if (null != aggregatedCommitment) {
            result = aggregatedCommitment.getEncoded(true);
        }

        return result;
    }

    /**
     * Concatenates the specified pubic keys
     *
     * @param publicKeys        the list of signers's (consensus group's) public keys
     * @param bitmapCommitments bitmap showing which elements from publicKeys to concatenate
     * @return a byte array holding the concatenation of public keys
     */
    private byte[] concatenatePublicKeys(ArrayList<PublicKey> publicKeys, long bitmapCommitments) {
        int idx = 0;
        byte[] result = new byte[0];

        // computing <L'> as concatenation of participating signers public keys
        for (PublicKey key : publicKeys) {
            if (0 != ((1 << idx) & bitmapCommitments)) {
                // concatenate the public keys
                result = Util.concatenateArrays(result, key.getQ().getEncoded(true));
            }
            idx++;
        }

        return result;
    }

    /**
     * Calculates the challenge according to Belare Naveen multi-signature algorithm:
     * H1(<L'>||Xi||R||m), where H1 is a Hashing function, e.g Sha3, Xi is the public key,
     * R is the aggregated commitment, and m is the message.
     *
     * @param signers              the list of signers's (consensus group's) public keys
     * @param publicKey            own public key
     * @param aggregatedCommitment the aggregated commitment from all signers as a byte array
     * @param message              the message to be signed
     * @param bitmapCommitments    commitment mask (byte), bit is 1 if corresponding signer participates in signing
     *                             or 0 otherwise
     * @return the challenge as a byte array
     */
    public byte[] computeChallenge(ArrayList<PublicKey> signers,
                                   PublicKey publicKey,
                                   byte[] aggregatedCommitment,
                                   byte[] message,
                                   long bitmapCommitments) {
        byte[] challenge;
        BigInteger challengeInt;

        // computing <L'> as concatenation of participating signers public keys
        challenge = concatenatePublicKeys(signers, bitmapCommitments);
        // do rest of concatenation <L'> || public key
        challenge = Util.concatenateArrays(challenge, publicKey.getQ().getEncoded(true));
        // <L'> || public key || R
        challenge = Util.concatenateArrays(challenge, aggregatedCommitment);
        // <L'> || public key || R || m
        challenge = Util.concatenateArrays(challenge, message);
        // compute hash
        challenge = Util.SHA3.digest(challenge);
        challengeInt = new BigInteger(1, challenge);

        //reduce the challenge modulo curve order
        challengeInt = challengeInt.mod(PrivateKey.getCurveOrder());

        return challengeInt.toByteArray();
    }

    /**
     * Calculates the signature share associated to this private key according to formula:
     * s = ri + challenge * xi, where ri is the private part of the commitment, xi is own
     * private key, and challenge is the result of computeChallenge
     *
     * @param challenge        the calculated challenge associated with own public key
     * @param privateKey       the own private key
     * @param commitmentSecret the commitment secret
     * @return the signature share
     */
    @Override
    public byte[] computeSignatureShare(byte[] challenge, PrivateKey privateKey, byte[] commitmentSecret) {
        BigInteger curveOrder = PrivateKey.getCurveOrder();
        BigInteger sigShare;
        BigInteger challengeInt = new BigInteger(challenge);
        BigInteger privateKeyInt = new BigInteger(privateKey.getValue());
        BigInteger commitmentSecretInt = new BigInteger(commitmentSecret);
        sigShare = commitmentSecretInt.add(challengeInt.multiply(privateKeyInt).mod(curveOrder)).mod(curveOrder);

        return sigShare.toByteArray();
    }

    /**
     * Verifies the signature share (R, s) on a message m, according to Schnorr verification algorithm:
     * 1. check if s is in [1, order-1]
     * 2. Compute c =  H(<L'> || R || publicKey || message)
     * 3. Compute R2 = s*G - c*publicKey
     * 4. if R2 = O, return false
     * 5. return R2 == R
     *
     * @param publicKeys    array list of signer's public keys
     * @param publicKey     public key for the signature share
     * @param signature     signature share to verify
     * @param aggCommitment aggregated commitment
     * @param commitment    commitment for signature share
     * @param message       message for which the signature was computed
     * @param bitmap        bitmap of participating signers out of all signers list
     * @return true if signature is verified, false otherwise
     */
    @Override
    public boolean verifySignatureShare(ArrayList<PublicKey> publicKeys,
                                        PublicKey publicKey,
                                        byte[] signature,
                                        byte[] aggCommitment,
                                        byte[] commitment,
                                        byte[] message,
                                        long bitmap) {
        // Compute R2 = s*G + c*publicKey
        ECPoint basePointG = PrivateKey.getEcParameters().getG();
        BigInteger commitmentRInt = new BigInteger(commitment);
        byte[] challenge;
        BigInteger challengeInt;
        ECPoint commitmentR2;

        // calculate challenge
        challenge = computeChallenge(publicKeys, publicKey, aggCommitment, message, bitmap);
        // get BigInteger challenge
        challengeInt = (new BigInteger(1, challenge));
        // Compute R2 = s*G - c*publicKey
        commitmentR2 = basePointG.multiply(new BigInteger(signature)).subtract(publicKey.getQ().multiply(challengeInt));

        return (new BigInteger(commitmentR2.getEncoded(true))).equals(commitmentRInt);
    }

    /**
     * Aggregates the signature shares according to the participating signers
     *
     * @param signatureShares the list of signature shares
     * @param bitmapSigners   the participating signers as a bitmap (byte)
     * @return the aggregated signature
     */
    @Override
    public byte[] aggregateSignatures(ArrayList<byte[]> signatureShares, long bitmapSigners) {
        byte idx = 0;
        BigInteger curveOrder = PrivateKey.getCurveOrder();
        BigInteger aggregatedSignature = BigInteger.ZERO;

        for (byte[] signature : signatureShares) {
            if (0 != ((1 << idx) & bitmapSigners)) {
                aggregatedSignature = aggregatedSignature.add(new BigInteger(signature)).mod(curveOrder);
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
     * @param aggregatedSignature  the aggregated signature to be verified
     * @param aggregatedCommitment the aggregated commitment
     * @param message              the message on which the signature was calculated
     * @param bitmapSigners        the bitmap of signers
     * @return true if aggregated signature is valid, false otherwise
     */
    @Override
    public boolean verifyAggregatedSignature(ArrayList<PublicKey> signers,
                                             byte[] aggregatedSignature, byte[] aggregatedCommitment,
                                             byte[] message, long bitmapSigners) {
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECPoint aggregatedCommitmentPoint = ecParameters.getCurve().decodePoint(aggregatedCommitment.clone());
        int idx = 0;
        ECPoint sum = null;
        ECPoint sG;
        BigInteger tempChallenge;
        ECPoint tmp;

        // compute sum(H1(<L'> || Xi || R || m)*Xi*Bitmap[i])
        for (PublicKey publicKey : signers) {
            if (0 != ((1 << idx) & bitmapSigners)) {
                //compute challenge H1(<L'>||Xi||R||m)
                tempChallenge = new BigInteger(computeChallenge(signers, publicKey, aggregatedCommitment, message, bitmapSigners));
                tmp = publicKey.getQ().multiply(tempChallenge);
                // do the sum
                if (null == sum) {
                    // H1 * Xi * Bitmap[i]
                    sum = tmp;
                } else {
                    sum = sum.add(tmp);
                }
            }
            idx++;
        }

        // calculate s*G
        sG = ecParameters.getG().multiply(new BigInteger(aggregatedSignature));
        // calculate sG-sum(H1(...)Xi)
        sum = sG.subtract(sum);

        // comparison R = sG - sum(H1(<L'>||Xi||R||m)Xi)
        return aggregatedCommitmentPoint.equals(sum);
    }
}
