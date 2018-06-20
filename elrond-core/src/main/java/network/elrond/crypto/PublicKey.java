package network.elrond.crypto;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPublicKeySpec;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public class PublicKey {
    private static final Logger logger = LogManager.getLogger(PublicKey.class);
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
        logger.traceEntry("params: {}", publicPointQEncoding);
        if(publicPointQEncoding == null){
            IllegalArgumentException ex = new IllegalArgumentException("publicPointQEncoding cannot be null");
            logger.throwing(ex);
            throw ex;
        }

        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
        ECParameterSpec ecParameterSpec = new ECParameterSpec(
                ecCryptoService.getCurve(),
                ecCryptoService.getG(),
                ecCryptoService.getN(),
                ecCryptoService.getH(),
                ecCryptoService.getSeed());
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecCryptoService.getCurve().decodePoint(publicPointQEncoding), ecParameterSpec);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(ecCryptoService.getAlgorithm(), ecCryptoService.getProvider());
            q = ((ECPublicKey) keyFactory.generatePublic(publicKeySpec)).getQ();
            initialized = true;
            logger.trace("done initializing public key = {}", q);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            logger.catching(e);
        }

        logger.traceExit();
    }

    public PublicKey(PublicKey publicKey) {
        logger.traceEntry("params: {}", publicKey);
        if(publicKey == null){
            IllegalArgumentException ex = new IllegalArgumentException();
            logger.throwing(ex);
            throw ex;
        }

        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
        ECParameterSpec ecParameterSpec = new ECParameterSpec(
                ecCryptoService.getCurve(),
                ecCryptoService.getG(),
                ecCryptoService.getN(),
                ecCryptoService.getH(),
                ecCryptoService.getSeed());
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecCryptoService.getCurve().decodePoint(publicKey.getValue()), ecParameterSpec);
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(ecCryptoService.getAlgorithm(), ecCryptoService.getProvider());
            q = ((ECPublicKey) keyFactory.generatePublic(publicKeySpec)).getQ();
            initialized = true;
            logger.trace("done initializing public key = {}", q);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            logger.catching(e);
        }

        logger.traceExit();
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given private key
     *
     * @param privateKey the corresponding private key
     */
    public PublicKey(PrivateKey privateKey) {
        logger.traceEntry("params: {}", privateKey);
        if(privateKey == null){
            IllegalArgumentException ex = new IllegalArgumentException("PrivateKey cannot be null");
            logger.throwing(ex);
            throw ex;
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
        logger.trace("done initializing public key = {}", q);
        logger.traceExit();
    }

    /**
     * Setter for public key
     *
     * @param point the elliptic curve point
     */
//    public void setPublicKey(ECPoint point) {
//        q = point;
//    }

    /**
     * Sets the public key from a byte stream
     *
     * @param key the encoded byte stream representation of the public key (obtained through getEncoded call)
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
//    public void setPublicKey(byte[] key) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
//        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
//        ECParameterSpec ecParameterSpec = new ECParameterSpec(
//                ecCryptoService.getCurve(),
//                ecCryptoService.getG(),
//                ecCryptoService.getN(),
//                ecCryptoService.getH(),
//                ecCryptoService.getSeed());
//        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecCryptoService.getCurve().decodePoint(key), ecParameterSpec);
//        KeyFactory keyFactory = KeyFactory.getInstance(ecCryptoService.getAlgorithm(), ecCryptoService.getProvider());
//        q = ((ECPublicKey) keyFactory.generatePublic(publicKeySpec)).getQ();
//        initialized = true;
//    }

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
        logger.traceEntry();
        if (!initialized || q.isInfinity() || new BigInteger(q.getEncoded(true)).equals(BigInteger.ZERO)) {
            logger.trace("not correctly initialized!");
            return logger.traceExit(false);
        }

        // check if it satisfies curve equation
        if (!q.isValid()) {
            logger.trace("does not satisfy curve equation!");
            return logger.traceExit(false);
        }

        return logger.traceExit(true);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public ECPoint getQ() {
        return (initialized) ? q : null;
    }

    @Override
    public String toString(){
        return String.format("PublicKey{%s}", Util.byteArrayToHexString(this.getValue()));
    }


}
