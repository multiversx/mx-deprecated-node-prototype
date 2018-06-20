package network.elrond.crypto;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PrivateKey {
    private static final Logger logger = LogManager.getLogger(PrivateKey.class);

    private byte[] privateKey;

    /**
     * Default constructor
     * Creates a new private key
     */
    public PrivateKey() {
        logger.traceEntry();
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
        logger.trace("done initializing private key = {}", this.getValue());
        logger.traceExit();
    }

    /**
     * Creates a private key from a byte array
     *
     * @param src The byte array
     */
    public PrivateKey(byte[] src) {
        logger.traceEntry("params: {}", src);
        if(src == null){
            IllegalArgumentException ex = new IllegalArgumentException("Src cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        privateKey = src.clone();
        logger.trace("done initializing private key = {}", this.getValue());
        logger.traceExit();
    }

    /**
     * Constructor
     * Generates a private key starting from String
     *
     * @param seed String seed to generate the private key
     */
    public PrivateKey(String seed) {
        logger.traceEntry("params: {}", seed);
        if(seed == null || seed.isEmpty()){
            IllegalArgumentException ex = new IllegalArgumentException("Seed cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        byte[] seedArray = seed.getBytes();
        BigInteger seedInt;
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        seedArray = Util.SHA3.get().digest(seedArray);
        seedInt = new BigInteger(1, seedArray);

        // to be a valid private key it needs to verify:
        // 0 < pk < n, where n is the order of the largest prime order subgroup
        while (1 != seedInt.compareTo(BigInteger.ZERO) ||
                0 <= seedInt.compareTo(ecCryptoService.getN())) {
            seedArray = Util.SHA3.get().digest(seedArray);
            seedInt = new BigInteger(1, seedArray);
        }

        // store without the extra byte in case of unsigned
        privateKey = seedInt.toByteArray();
        logger.trace("done initializing private key = {}", this.getValue());
        logger.traceExit();
    }

    /**
     * Checks if the private key is valid
     *
     * @return true if private key is valid, false otherwise
     */
    public boolean isValid() {
        logger.traceEntry();
        BigInteger privateKeyInt = new BigInteger(privateKey);
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();

        if (1 != privateKeyInt.compareTo(BigInteger.ZERO) ||
                0 <= privateKeyInt.compareTo(ecCryptoService.getN())) {
            return logger.traceExit(false);
        }
        return logger.traceExit(true);
    }

    /**
     * Getter for the private key
     *
     * @return the private key
     */
    public byte[] getValue() {
        return privateKey.clone();
    }

    @Override
    public String toString(){
        return String.format("PrivateKey{%s}", Util.byteArrayToHexString(this.getValue()));
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
