package network.elrond.crypto;

import network.elrond.crypto.curves.SECNamedCurves;
import network.elrond.crypto.asn1.x9.X9ECParameters;

import network.elrond.crypto.ecmath.ECCurve;
import network.elrond.crypto.ecmath.ECPoint;

import java.math.BigInteger;

public class ECCryptoServiceSecp256k1Impl implements ECCryptoService {
    private static final X9ECParameters EC_PARAMETERS = SECNamedCurves.getByName("secp256k1");
    private static final String ALGORITHM = "EC";

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
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
