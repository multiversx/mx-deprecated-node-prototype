package network.elrond.crypto;

import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.Security;

public class ECCryptoServiceSecp256k1Impl implements ECCryptoService {
    private static final X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");
    private static final String ALGORITHM = "EC";
    private static final String PROVIDER = "SC";

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    public X9ECParameters getEcParameters() {
        return EC_PARAMETERS;
    }

    @Override
    public ECCurve getCurve() {
        return EC_PARAMETERS.getCurve();
    }

    @Override
    public BigInteger getN() {
        return EC_PARAMETERS.getN();
    }

    @Override
    public ECPoint getG() {
        return EC_PARAMETERS.getG();
    }

    @Override
    public BigInteger getH() {
        return EC_PARAMETERS.getH();
    }

    @Override
    public byte[] getSeed() {
        return EC_PARAMETERS.getSeed();
    }
}
