package network.elrond.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrivateKey {
    private BigInteger privateKey;

    /**
     * Default constructor
     * Creates a new private key
     */
    public PrivateKey() {
        X9ECParameters ecParameters = SECNamedCurves.getByName("secp256k1");
        AsymmetricCipherKeyPair keyPair;
        ECDomainParameters domainParameters;
        ECKeyGenerationParameters keyParameters;
        ECKeyPairGenerator keyPairGenerator;

        domainParameters = new ECDomainParameters(ecParameters.getCurve(),
                ecParameters.getG(),
                ecParameters.getN(),
                ecParameters.getH(),
                ecParameters.getSeed());

        keyParameters = new ECKeyGenerationParameters(
                domainParameters,
                new SecureRandom());

        keyPairGenerator = new ECKeyPairGenerator();
        keyPairGenerator.init(keyParameters);
        keyPair = keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        this.privateKey = privateKey.getD();
    }

    /**
     * Creates a private key from a byte array
     *
     * @param src The byte array
     */
    public PrivateKey(byte[] src) {
        privateKey = new BigInteger(src);
    }

    /**
     * Constructor
     * Generates a private key starting from String
     *
     * @param seed String seed to generate the private key
     */
    public PrivateKey(String seed){

    }

    /**
     * Getter for the private key
     *
     * @return the private key
     */
    public BigInteger getPrivateKey() {
        return privateKey;
    }

    /**
     * Setter for the private key
     *
     * @param privateKey
     */
    public void setPrivateKey(BigInteger privateKey) {
        this.privateKey = privateKey;
    }
}
