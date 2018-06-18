package network.elrond.crypto;

import network.elrond.crypto.generators.ECKeyPairGenerator;
import network.elrond.service.AppServiceProvider;
import network.elrond.crypto.params.ECDomainParameters;
import network.elrond.crypto.params.ECKeyGenerationParameters;
import network.elrond.crypto.params.ECPrivateKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrivateKey {
    private byte[] privateKey;

    /**
     * Default constructor
     * Creates a new private key
     */
    public PrivateKey() {
        AsymmetricCipherKeyPair keyPair;
        ECDomainParameters domainParameters;
        ECKeyGenerationParameters keyParameters;
        ECKeyPairGenerator keyPairGenerator;
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        domainParameters = new ECDomainParameters(ecCryptoService.getCurve(),
                ecCryptoService.getG(),
                ecCryptoService.getN(),
                ecCryptoService.getH(),
                ecCryptoService.getSeed());

        keyParameters = new ECKeyGenerationParameters(
                domainParameters,
                new SecureRandom());

        keyPairGenerator = new ECKeyPairGenerator();
        keyPairGenerator.init(keyParameters);
        keyPair = keyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        this.privateKey = privateKey.getD().toByteArray();
    }

    /**
     * Creates a private key from a byte array
     *
     * @param src The byte array
     */
    public PrivateKey(byte[] src) {
        if (src == null) {
            throw new IllegalArgumentException("Src cannot be null");
        }
        privateKey = src.clone();
    }

    /**
     * Constructor
     * Generates a private key starting from String
     *
     * @param seed String seed to generate the private key
     */
    public PrivateKey(String seed) {
        if (seed == null || seed.isEmpty()) {
            throw new IllegalArgumentException("Seed cannot be null");
        }
        byte[] seedArray = seed.getBytes();
        BigInteger seedInt;
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        seedArray = SHA3Helper.sha3(seedArray);
        seedInt = new BigInteger(1, seedArray);

        // to be a valid private key it needs to verify:
        // 0 < pk < n, where n is the order of the largest prime order subgroup
        while (1 != seedInt.compareTo(BigInteger.ZERO) ||
                0 <= seedInt.compareTo(ecCryptoService.getN())) {
            seedArray = SHA3Helper.sha3(seedArray);
            seedInt = new BigInteger(1, seedArray);
        }

        // store without the extra byte in case of unsigned
        privateKey = seedInt.toByteArray();
    }

    /**
     * Checks if the private key is valid
     *
     * @return true if private key is valid, false otherwise
     */
    public boolean isValid() {
        BigInteger privateKeyInt = new BigInteger(privateKey);
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        if (1 != privateKeyInt.compareTo(BigInteger.ZERO) ||
                0 <= privateKeyInt.compareTo(ecCryptoService.getN())) {
            return false;
        }
        return true;
    }

    /**
     * Getter for the private key
     *
     * @return the private key
     */
    public byte[] getValue() {
        return privateKey.clone();
    }

//    /**
//     * Setter for the private key
//     *
//     * @param privateKey
//     */
//    public void setPrivateKey(byte[] privateKey) {
//        if(privateKey == null){
//            throw new IllegalArgumentException("PrivateKey cannot be null");
//        }
//
//        this.privateKey = privateKey.clone();
//    }
}
