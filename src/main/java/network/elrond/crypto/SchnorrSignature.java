package network.elrond.crypto;

import network.elrond.core.Util;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class SchnorrSignature {
    private byte[] signature;
    private byte[] challenge;
    private boolean calculated;
    private static SecureRandom secureRandom;

    static {
        byte[] seed;
        secureRandom = new SecureRandom();
        seed = secureRandom.generateSeed(32);
        secureRandom.setSeed(seed);
    }

    /**
     * Default Constructor
     */
    public SchnorrSignature() {
        calculated = false;
    }

    /**
     * Getter for signature
     *
     * @return the signature value
     */
    public byte[] getSignatureValue() {
        return (calculated) ? signature.clone() : null;
    }

    /**
     * Getter for challenge associated with signature
     *
     * @return the challenge value
     */
    public byte[] getChallenge() {
        return (calculated) ? challenge.clone() : null;
    }

    /**
     * Setter for signature
     *
     * @param signature signature as a byte array
     * @param challenge challenge as a byte array
     * @return true if all set was done, false otherwise
     */
    public boolean setSignature(byte[] signature, byte[] challenge) {
        if (signature == null || (new BigInteger(signature)).equals(BigInteger.ZERO)) {
            return false;
        }

        if (challenge == null || (new BigInteger(challenge)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.signature = signature.clone();
        this.challenge = challenge.clone();
        calculated = true;

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
     * @param message    the message to sign as a byte array
     * @param privateKey the private key used to sign
     * @param publicKey  the public key used for challenge calculation
     * @return true if signed successfully, false otherwise
     */
    public boolean signMessage(byte[] message, PrivateKey privateKey, PublicKey publicKey) {
        // Choose random private part k
        byte[] k = new byte[32];
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

        calculated = false;
        while (!rValid || !calculated) {
            calculated = false;
            secureRandom.nextBytes(k);
            kInteger = new BigInteger(1, k);

            //make sure k is not zero
            while (kInteger.equals(BigInteger.ZERO)) {
                secureRandom.nextBytes(k);
                kInteger = new BigInteger(1, k);
            }

            //without possible extra byte for unsigned
            kInteger = new BigInteger(k);

            // Calculate public part (commit point) Q = k*G
            basePointG = PrivateKey.getEcParameters().getG();
            commitPointQ = basePointG.multiply(kInteger);

            // Calculate the challenge
            // First concatenate Q, public key and message
            challengeR = Util.concatenateArrays(commitPointQ.getEncoded(true), publicKey.getEncoded());
            challengeR = Util.concatenateArrays(challengeR, message);
            // Calculate the digest of the byte array to get the challenge
            challengeR = Util.SHA3.digest(challengeR);
            BigInteger challengeRInteger = new BigInteger(challengeR);
            if (challengeRInteger.equals(BigInteger.ZERO)) {
                rValid = false;
                System.out.println("challenge zero: " + Util.byteArrayToHexString(challengeRInteger.toByteArray()));
            } else {
                rValid = true;

                // Compute signature as s = k - r * privateKey
                BigInteger sInteger = kInteger.subtract(challengeRInteger.multiply(new BigInteger(privateKey.getValue())));

                if (sInteger.equals(BigInteger.ZERO)) {
                    calculated = false;
                    System.out.println("signature zero: " + Util.byteArrayToHexString(sInteger.toByteArray()));
                } else {
                    signature = sInteger.toByteArray();
                    challenge = challengeR;
                    calculated = true;
                }
            }
        }

        return calculated;
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
     * @param message   the message as byte array, on which the signature was computed
     * @param publicKey the public key to verify signature against
     * @return true if the public key verifies the signature and message, false otherwise
     */
    public boolean verifySignature(byte[] message, PublicKey publicKey) {
        ECPoint basePointG;
        ECPoint commitPointQ;
        byte[] r2;
        BigInteger challengeInt;

        // check signature
        if (null == signature || !this.calculated) {
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
        challengeInt = new BigInteger(challenge);
        commitPointQ = basePointG.multiply(new BigInteger(signature))
                .add(publicKey.getQ().multiply(challengeInt));

        if (commitPointQ.isInfinity()) {
            return false;
        }

        // if not at infinity calculate r2 = H(Q, publicKey, message)
        r2 = Util.concatenateArrays(commitPointQ.getEncoded(true), publicKey.getEncoded());
        r2 = Util.concatenateArrays(r2, message);
        r2 = Util.SHA3.digest(r2);

        return Arrays.equals(challenge, r2);
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
