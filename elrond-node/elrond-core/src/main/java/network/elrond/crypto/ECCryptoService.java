package network.elrond.crypto;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public interface ECCryptoService {
    String getAlgorithm();

    String getProvider();

    X9ECParameters getEcParameters();

    ECCurve getCurve();

    BigInteger getN();

    ECPoint getG();

    BigInteger getH();

    byte[] getSeed();
}
