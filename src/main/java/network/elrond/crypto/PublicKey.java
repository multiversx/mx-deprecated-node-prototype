package network.elrond.crypto;

import network.elrond.core.Util;
import org.bouncycastle.asn1.sec.SECNamedCurves;
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

import static network.elrond.core.Util.byteArrayToHexString;

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
    private PublicKey() {
        isInitialized = false;
    }

    /**
     * Constructor
     * Generates the corresponding public key for the given private key
     *
     * @param privateKey the corresponding private key
     */
    public PublicKey(PrivateKey privateKey) {
        X9ECParameters ecParameters = SECNamedCurves.getByName("secp256r1");
        ECDomainParameters domainParameters = new ECDomainParameters(
                ecParameters.getCurve(),
                ecParameters.getG(),
                ecParameters.getN(),
                ecParameters.getH(),
                ecParameters.getSeed());

        // compute the public key based on the private key
        q = domainParameters.getG().multiply(privateKey.getPrivateKey());
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
     * @param key
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public void setPublicKey(byte[] key) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        X9ECParameters ecParameters = SECNamedCurves.getByName("secp256k1");
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
        return "ECDH";
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

    public static void main(String[] args) {
        PrivateKey privk = new PrivateKey();
        PublicKey pubk = new PublicKey(privk);

        int length = pubk.getEncoded().length;
        System.out.println("Calculated public key encoded: " + byteArrayToHexString(pubk.getEncoded()) + "\n length: " + length);

        System.out.println("Generated private key: " + byteArrayToHexString(privk.getPrivateKey().toByteArray()));

        PublicKey pubk2 = new PublicKey();
        try {
            pubk2.setPublicKey(pubk.getEncoded());
            System.out.println("Generated public key encoded: " + byteArrayToHexString(pubk2.getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
