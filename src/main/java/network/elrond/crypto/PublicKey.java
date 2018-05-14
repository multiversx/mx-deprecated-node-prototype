package network.elrond.crypto;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

public class PublicKey implements ECPublicKey {
    private ECPoint q;
    private boolean isInitialized;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Default constructor
     */
    public PublicKey() {
        isInitialized = false;
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
        q = domainParameters.getG().multiply(privateKey.getValue());
        isInitialized = true;
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
        isInitialized = true;
    }

    @Override
    public ECPoint getQ() {
        return q;
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
        return q.getEncoded(true);
    }

    @Override
    public ECParameterSpec getParameters() {
        return null;
    }
}
