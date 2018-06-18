package network.elrond.crypto;

import network.elrond.crypto.ecmath.ECCurve;
import network.elrond.crypto.ecmath.ECPoint;

import java.math.BigInteger;

public interface ECCryptoService {
    String getAlgorithm();

    ECCurve getCurve();

    BigInteger getN();

    ECPoint getG();

    BigInteger getH();

    byte[] getSeed();
}
