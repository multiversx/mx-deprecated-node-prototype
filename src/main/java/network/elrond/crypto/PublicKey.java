package network.elrond.crypto;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

public class PublicKey implements ECPublicKey {
    private ECPoint q;
    private boolean initialized;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Default constructor
     */
    public PublicKey() {
        initialized = false;
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given public point encoding
     *
     * @param key the public point Q encoding, as a byte array
     */
    public PublicKey(byte[] key) {
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECParameterSpec ecParameterSpec = new ECParameterSpec(
                ecParameters.getCurve(),
                ecParameters.getG(),
                ecParameters.getN(),
                ecParameters.getH(),
                ecParameters.getSeed());
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecParameters.getCurve().decodePoint(key), ecParameterSpec);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("EC", "BC");
            q = ((ECPublicKey) keyFactory.generatePublic(publicKeySpec)).getQ();
            initialized = true;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given private key
     *
     * @param privateKey the corresponding private key
     */
    public PublicKey(PrivateKey privateKey) {
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(
                ecParameters.getCurve(),
                ecParameters.getG(),
                ecParameters.getN(),
                ecParameters.getH(),
                ecParameters.getSeed());

        // compute the public key based on the private key
        q = domainParameters.getG().multiply(new BigInteger(privateKey.getValue()));
        initialized = true;
    }

    /**
     * Setter for public key
     *
     * @param point the elliptic curve point
     */
    public void setPublicKey(ECPoint point) {
        q = point;
    }

    /**
     * Sets the public key from a byte stream
     *
     * @param key the encoded byte stream representation of the public key (obtained through getEncoded call)
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public void setPublicKey(byte[] key) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECParameterSpec ecParameterSpec = new ECParameterSpec(
                ecParameters.getCurve(),
                ecParameters.getG(),
                ecParameters.getN(),
                ecParameters.getH(),
                ecParameters.getSeed());
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecParameters.getCurve().decodePoint(key), ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        q = ((ECPublicKey) keyFactory.generatePublic(publicKeySpec)).getQ();
        initialized = true;
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

    @Override
    public ECPoint getQ() {
        return (initialized) ? q : null;
    }

    @Override
    public String getAlgorithm() {
        return "EC";
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return (initialized) ? q.getEncoded(true) : null;
    }

    @Override
    public ECParameterSpec getParameters() {
        X9ECParameters ecParameters = PrivateKey.getEcParameters();
        ECParameterSpec ecParameterSpec = new ECParameterSpec(
                ecParameters.getCurve(),
                ecParameters.getG(),
                ecParameters.getN(),
                ecParameters.getH());

        return ecParameterSpec;
    }
}
