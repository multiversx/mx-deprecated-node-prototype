package network.elrond.crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrivateKey {
    private BigInteger privateKey;
    private final byte MIN_HASHING_ITERATIONS = 5;
    private static final SHA3.DigestSHA3 SHA3_INSTANCE = new SHA3.Digest256();

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
        X9ECParameters ecParameters = SECNamedCurves.getByName("secp256k1");
        BigInteger primeOrder = ecParameters.getN();
        byte[] seedArray = seed.getBytes();

        for(int i=0; i< MIN_HASHING_ITERATIONS; i++){
            seedArray = SHA3_INSTANCE.digest(seedArray);
        }

        privateKey = new BigInteger(seedArray);

        // to be a valid private key it needs to verify:
        // 0 < pk < n, where n is the order of the largest prime order subgroup
        while((-1 != privateKey.compareTo(primeOrder)) && (1 != privateKey.compareTo(BigInteger.valueOf(0)))) {
            seedArray = SHA3_INSTANCE.digest(seedArray);
            privateKey = new BigInteger(seedArray);
        }
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
