package network.elrond.crypto;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class SignatureServiceSchnorrImpl implements SignatureService {
    private static SecureRandom secureRandom;

    static {
        byte[] seed;
        secureRandom = new SecureRandom();
        seed = secureRandom.generateSeed(32);
        secureRandom.setSeed(seed);
    }

    /**
     * Generates the signature according to Schnorr signing algorithm:
     * 1. Generate random r in range [1, curveOrder - 1]
     * 2. Compute commitment (point) R = r*G, where G is the base point on the selected curve
     * 3. Compute the challenge c = H(R, publicKey, message)
     * 4. if c = 0 mod(order), start again from 1
     * 5. else compute s = r - c * privateKey mod(order)
     * 6. if s = 0 start from 1
     * 7. else return signature (c, s)
     *
     * @param message    the message to sign as a byte array
     * @param privateKey the private key used to sign
     * @param publicKey  the public key used for challenge calculation
     * @return signature object containing signature, commitment and challenge
     */
    @Override
    public Signature signMessage(byte[] message, byte[] privateKey, byte[] publicKey) {
        // Choose random private part r
        byte[] r = new byte[32];
        BigInteger rInteger;
        ECPoint basePointG;
        ECPoint commitPointR;
        byte[] challengeC;
        BigInteger challengeCInteger;
        boolean cValid = false;
        boolean calculated = false;
        Signature signature = new Signature();
        BigInteger sInteger;
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        Util.check(message != null, "message != null");
        Util.check(privateKey != null, "privateKey != null");
        Util.check(publicKey != null, "publicKey != null");
        Util.check(message.length != 0, "message.length != 0");
        Util.check(privateKey.length != 0, "privateKey.length != 0");
        Util.check(publicKey.length != 0, "publicKey.length != 0");

        while (!cValid || !calculated) {
            secureRandom.nextBytes(r);
            rInteger = new BigInteger(1, r);

            //make sure r is not zero
            while (rInteger.equals(BigInteger.ZERO)) {
                secureRandom.nextBytes(r);
                rInteger = new BigInteger(1, r);
            }

            //without possible extra byte for unsigned
            rInteger = new BigInteger(r);

            // Calculate public part (commit point) Q = k*G
            basePointG = ecCryptoService.getG();
            commitPointR = basePointG.multiply(rInteger);

            // Calculate the challenge
            // First concatenate R, public key and message
            challengeC = Util.concatenateArrays(commitPointR.getEncoded(true), publicKey);
            challengeC = Util.concatenateArrays(challengeC, message);
            // Calculate the digest of the byte array to getAccountState the challenge
            challengeC = Util.SHA3.digest(challengeC);
            challengeCInteger = new BigInteger(challengeC);

            if (challengeCInteger.equals(BigInteger.ZERO)) {
                cValid = false;
            } else {
                cValid = true;
                // Compute signature as s = r - c * privateKey
                sInteger = rInteger.subtract(challengeCInteger.multiply(new BigInteger(privateKey)));

                if (sInteger.equals(BigInteger.ZERO)) {
                    calculated = false;
                } else {
                    signature.setSignature(sInteger.toByteArray());
                    signature.setChallenge(challengeC);
                    signature.setCommitment(commitPointR.getEncoded(true));
                    calculated = true;
                }
            }
        }

        return signature;
    }

    /**
     * Generates the signature according to Schnorr signing algorithm.
     *
     * @param message    message to sign in string format
     * @param privateKey the private key
     * @param publicKey  the public key
     * @return signature object containing signature, commitment and challenge
     */
    @Override
    public Signature signMessage(String message, byte[] privateKey, byte[] publicKey) {
        return signMessage(message.getBytes(), privateKey, publicKey);
    }

    /**
     * Verifies the signature (c, s) on a message m, according to Schnorr verification algorithm:
     * 1. check if c, s is in [1, order-1]
     * 2. Compute R = s*G + c*publicKey
     * 3. if R = O, return false
     * 4. else calculate c2 = H(R, publicKey, message)
     * 5. return c2 == c
     *
     * @param message   the message as byte array, on which the signature was computed
     * @param publicKey the public key to verify signature against
     * @return true if the public key verifies the signature and message, false otherwise
     */
    @Override
    public boolean verifySignature(byte[] signature, byte[] challenge, byte[] message, byte[] publicKey) {
        ECPoint basePointG;
        ECPoint commitPointR;
        byte[] c2;
        BigInteger challengeInt;
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        Util.check(signature != null, "signature != null");
        Util.check(challenge != null, "challenge != null");
        Util.check(message != null, "message != null");
        Util.check(publicKey != null, "publicKey != null");
        Util.check(signature.length != 0, "signature.length != 0");
        Util.check(challenge.length != 0, "challenge.length != 0");
        Util.check(message.length != 0, "message.length != 0");
        Util.check(publicKey.length != 0, "publicKey.length != 0");

        // Compute R = s*G + c*publicKey
        //TODO: Do not recreate PublicKey!!!
        ECPoint publicKeyPoint = (new PublicKey(publicKey)).getQ();
        basePointG = ecCryptoService.getG();
        challengeInt = new BigInteger(challenge);
        commitPointR = basePointG.multiply(new BigInteger(signature))
                .add(publicKeyPoint.multiply(challengeInt));

        if (commitPointR.isInfinity()) {
            return false;
        }

        // if not at infinity calculate c2 = H(R, publicKey, message)
        c2 = Util.concatenateArrays(commitPointR.getEncoded(true), publicKey);
        c2 = Util.concatenateArrays(c2, message);
        //c2 = Util.SHA3.digest(c2);

        MessageDigest instance = null;
        try {
            instance = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        c2 = instance.digest(c2);



        return Arrays.equals(challenge, c2);
    }

    /**
     * Verifies the signature (c, s) on a message m, according to Schnorr verification algorithm.
     *
     * @param message   the message as string, on which the signature was computed
     * @param publicKey the public key to verify signature against
     * @return true if the public key verifies the signature and message, false otherwise
     */
    @Override
    public boolean verifySignature(byte[] signature, byte[] challenge, String message, byte[] publicKey) {
        return verifySignature(signature, challenge, message.getBytes(), publicKey);
    }
}
