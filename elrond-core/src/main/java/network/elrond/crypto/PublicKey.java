package network.elrond.crypto;

import network.elrond.service.AppServiceProvider;
import network.elrond.crypto.params.ECDomainParameters;
import network.elrond.crypto.ecmath.ECPoint;

import java.math.BigInteger;

public class PublicKey {
    private ECPoint q;
    private boolean initialized;

    /**
     * Default constructor
     */
    private PublicKey() {
        initialized = false;
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given public point encoding
     *
     * @param publicPointQEncoding the public point Q encoding, as a byte array
     */
    public PublicKey(byte[] publicPointQEncoding) {
        createPublicKey(publicPointQEncoding);
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given public point encoding
     *
     * @param publicKey the public point Q encoding, as a byte array
     */
    public PublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("publicKey cannot be null");
        }

        createPublicKey(publicKey.getValue());
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given private key
     *
     * @param privateKey the corresponding private key
     */
    public PublicKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("PrivateKey cannot be null");
        }

        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
        ECDomainParameters domainParameters = new ECDomainParameters(
                ecCryptoService.getCurve(),
                ecCryptoService.getG(),
                ecCryptoService.getN(),
                ecCryptoService.getH(),
                ecCryptoService.getSeed());

        // compute the public key based on the private key
        q = domainParameters.getG().multiply(new BigInteger(privateKey.getValue()));
        initialized = true;
    }


    private void createPublicKey(byte[] publicPointQEncoding){
        if (publicPointQEncoding == null) {
            throw new IllegalArgumentException("publicPointQEncoding cannot be null");
        }

        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
        try {
            q = ecCryptoService.getCurve().decodePoint(publicPointQEncoding);
            initialized = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the encoded form of public key as a byte array
     *
     * @return a byte array representing the signature
     */
    public byte[] getValue() {
        return q.getEncoded(true);
    }


    /**
     * Checks if public key is valid
     *
     * @return true if public key is valid, false otherwise
     */
    public boolean isValid() {
        if (!initialized || q.isInfinity() || new BigInteger(q.getEncoded(true)).equals(BigInteger.ZERO)) {
            return false;
        }

        // check if it satisfies curve equation
        if (!q.isValid()) {
            return false;
        }

        return true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public ECPoint getQ() {
        return (initialized) ? q : null;
    }

}
