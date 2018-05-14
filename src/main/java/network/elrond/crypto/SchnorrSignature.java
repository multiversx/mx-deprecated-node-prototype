package network.elrond.crypto;

import network.elrond.core.Util;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SchnorrSignature {
    private BigInteger signature;
    private BigInteger challenge;
    private boolean isCalculated;

    public SchnorrSignature() {
        isCalculated = false;
    }

    /**
     * Getter for signature
     *
     * @return the signature value
     */
    public BigInteger getSignatureValue() {
        return (isCalculated) ? signature : null;
    }

    /**
     * Getter for challenge associated with signature
     *
     * @return the challenge value
     */
    public BigInteger getChallenge() {
        return (isCalculated) ? challenge : null;
    }

    /**
     * Setter for signature
     *
     * @param signature BigInteger representing signature
     * @param challenge BigInteger representing the associated challenge
     * @return true if all set was done, false otherwise
     */
    public boolean setSignature(BigInteger signature, BigInteger challenge) {
        if (signature == null || signature.equals(BigInteger.ZERO)) {
            return false;
        }

        if (challenge == null || challenge.equals(BigInteger.ZERO)) {
            return false;
        }

        this.signature = signature;
        this.challenge = challenge;
        isCalculated = true;

        return true;
    }

    /**
     * Generates the signature according to Schnorr signing algorithm:
     * 1. Generate random k in range [1, curveOrder - 1]
     * 2. Compute commitment (point) Q = k*G, where G is the base point on the selected curve
     * 3. Compute the challenge r = H(Q, publicKey, message)
     * 4. if r = 0 mod(order), start again from 1
     * 5. else compute s = k - r * privateKey mod(order)
     * 6. if s = 0 start from 1
     * 7. else return signature (r, s)
     *
     * @param message    the message to sign
     * @param privateKey the private key used to sign
     * @param publicKey  the public key used for challenge calculation
     * @return true if signed successfully, false otherwise
     */
    public boolean signMessage(byte[] message, PrivateKey privateKey, PublicKey publicKey) {
        // Choose random private part k
        SecureRandom secureRandom;
        byte[] k;
        BigInteger kInteger;
        byte[] s = null;
        ECPoint basePointG;
        ECPoint commitPointQ;
        byte[] challengeR;
        boolean rValid = false;

        // check message not null
        if (message == null || message.length == 0) {
            return false;
        }

        // check private key is valid
        if (!privateKey.isValid()) {
            return false;
        }

        isCalculated = false;
        while (!rValid || !isCalculated) {
            rValid = false;
            isCalculated = false;
            secureRandom = new SecureRandom();
            k = secureRandom.generateSeed(32);
            kInteger = new BigInteger(1, k);

            //make sure k is in allowed range
            while (0 <= kInteger.compareTo(PrivateKey.getCurveOrder())) {
                k = Util.SHA3.digest(k);
                kInteger = new BigInteger(k);
            }

            // Calculate public part (commit point) Q = k*G
            basePointG = PrivateKey.getEcParameters().getG();
            commitPointQ = basePointG.multiply(kInteger);

            // Calculate the challenge
            // First concatenate Q public key and message
            challengeR = Util.concatenateArrays(commitPointQ.getEncoded(true), publicKey.getEncoded());
            challengeR = Util.concatenateArrays(challengeR, message);
            // Calculate the digest of the byte array to get the challenge
            challengeR = Util.SHA3.digest(challengeR);
            BigInteger challengeRInteger = new BigInteger(challengeR);
            if (challengeRInteger.equals(BigInteger.ZERO)) {
                rValid = false;
            } else {
                rValid = true;

                // Compute signature as s = k - r * privateKey
                BigInteger sInteger = kInteger.subtract(challengeRInteger.multiply(privateKey.getValue()));

                if (sInteger.equals(BigInteger.ZERO)) {
                    isCalculated = false;
                } else {
                    signature = sInteger;
                    challenge = challengeRInteger;
                    isCalculated = true;
                }
            }
        }

        return isCalculated;
    }

    /**
     * Generates the signature according to Schnorr signing algorithm.
     *
     * @param message    message to sign in string format
     * @param privateKey the private key
     * @param publicKey  the public key
     * @return true if signed successfully, false otherwise
     */
    public boolean signMessage(String message, PrivateKey privateKey, PublicKey publicKey) {
        return signMessage(message.getBytes(), privateKey, publicKey);
    }

    /**
     * Verifies the signature (r, s) on a message m, according to Schnorr verification algorithm:
     * 1. check if r, s is in [1, order-1]
     * 2. Compute Q = s*G + r*publicKey
     * 3. if Q = O, return false
     * 4. else calculate r2 = H(Q, publicKey, message)
     * 5. return r2 == r
     *
     * @param message   the message as byte stream, on which the signature was computed
     * @param publicKey the public key to verify signature against
     * @return true if the public key verifies the signature and message, false otherwise
     */
    public boolean verifySignature(byte[] message, PublicKey publicKey) {
        ECPoint basePointG;
        ECPoint commitPointQ;
        byte[] r2;
        BigInteger r2Integer;

        // check signature
        if (null == signature || false == this.isCalculated) {
            return false;
        }

        // check challenge
        if (null == challenge) {
            return false;
        }

        // check message
        if (null == message || 0 == message.length) {
            return false;
        }

        // check public key
        if (null == publicKey || (new BigInteger(publicKey.getEncoded())).equals(BigInteger.ZERO)) {
            return false;
        }

        // Compute Q = s*g + r*publicKey
        basePointG = PrivateKey.getEcParameters().getG();
        commitPointQ = basePointG.multiply(signature).add(publicKey.getQ().multiply(challenge));

        if (commitPointQ.isInfinity()) {
            return false;
        }

        // if not at infinity calculate r2 = H(Q, publicKey, message)
        r2 = Util.concatenateArrays(commitPointQ.getEncoded(true), publicKey.getEncoded());
        r2 = Util.concatenateArrays(r2, message);
        r2 = Util.SHA3.digest(r2);
        r2Integer = new BigInteger(r2);

        return challenge.equals(r2Integer);
    }

    /**
     * Verifies the signature (r, s) on a message m, according to Schnorr verification algorithm.
     *
     * @param message   the message as string, on which the signature was computed
     * @param publicKey the public key to verify signature against
     * @return true if the public key verifies the signature and message, false otherwise
     */
    public boolean verifySignature(String message, PublicKey publicKey) {
        return verifySignature(message.getBytes(), publicKey);
    }
}
