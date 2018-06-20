package network.elrond.crypto;

import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import java.math.BigInteger;

public class SignatureServiceECDSAImpl implements SignatureService {
    private static final Logger logger = LogManager.getLogger(SignatureServiceECDSAImpl.class);

    @Override
    public Signature signMessage(byte[] message, byte[] privateKey, byte[] publicKey) {
        logger.traceEntry();
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
        BigInteger[] sig;
        Signature  signature = new Signature();

        Util.check(message != null, "message!=null");
        Util.check(privateKey != null, "privateKey!=null");
        Util.check(publicKey != null, "publicKey!=null");
        Util.check(message.length != 0, "message.length != 0");
        Util.check(privateKey.length != 0, "privateKey.length != 0");
        Util.check(publicKey.length != 0, "publicKey.length != 0");

        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey= new ECPrivateKeyParameters(new BigInteger(privateKey),
                new ECDomainParameters(ecCryptoService.getCurve(),
                        ecCryptoService.getG(),
                        ecCryptoService.getN(),
                        ecCryptoService.getH(),
                        ecCryptoService.getSeed()));

        signer.init(true, privKey);
        sig = signer.generateSignature(message);
        signature.setChallenge(sig[0].toByteArray());
        signature.setSignature(sig[1].toByteArray());

        return logger.traceExit(signature);
    }

    @Override
    public Signature signMessage(String message, byte[] privateKey, byte[] publicKey) {
        Util.check(message != null, "message!=null");

        return signMessage(message.getBytes(), privateKey, publicKey);
    }

    @Override
    public boolean verifySignature(byte[] signature, byte[] challenge, byte[] message, byte[] publicKey) {
        logger.traceEntry();
        ECDSASigner signer = new ECDSASigner();
        ECCryptoService ecCryptoService = AppServiceProvider.getECCryptoService();
        ECPublicKeyParameters parameters;

        Util.check(signature != null, "signature!=null");
        Util.check(challenge != null, "challenge!=null");
        Util.check(message != null, "message!=null");
        Util.check(publicKey != null, "publicKey!=null");
        Util.check(signature.length != 0, "signature!=null");
        Util.check(challenge.length != 0, "challenge!=null");
        Util.check(message.length != 0, "message!=null");
        Util.check(publicKey.length != 0, "publicKey!=null");

        parameters = new ECPublicKeyParameters(ecCryptoService.getCurve().decodePoint(publicKey),
                new ECDomainParameters(ecCryptoService.getCurve(),
                        ecCryptoService.getG(),
                        ecCryptoService.getN(),
                        ecCryptoService.getH(),
                        ecCryptoService.getSeed()));

        signer.init(false, parameters);
        try{
            return logger.traceExit(signer.verifySignature(message, new BigInteger(challenge), new BigInteger(signature)));
        } catch(NullPointerException ex) {
            logger.throwing(ex);
            return logger.traceExit(false);
        }
    }

    @Override
    public boolean verifySignature(byte[] signature, byte[] challenge, String message, byte[] publicKey) {

        Util.check(message != null, "message!=null");

        return verifySignature(signature, challenge, message.getBytes(), publicKey);
    }
}
