package network.elrond.crypto;


import java.math.BigInteger;

/**
 * Class implementing Belare and Neven Multi-signature
 */
public class BNMultiSignature implements MultiSignature {

    @Override
    public BigInteger getRandom() {
        return null;
    }

    @Override
    public BigInteger computeCommitment(BigInteger pubKey) {
        return null;
    }

    @Override
    public BigInteger computeCommitmentHash(BigInteger commitment) {
        return null;
    }

    @Override
    public boolean validateCommitment(BigInteger commitment, BigInteger commitmentHash) {
        return false;
    }

    @Override
    public BigInteger computeChallenge() {
        return null;
    }

    @Override
    public BigInteger computeSignatureShare() {
        return null;
    }

    @Override
    public boolean VerifySignatureShare() {
        return false;
    }

    @Override
    public BigInteger AggregateSignatures() {
        return null;
    }

    @Override
    public boolean VerifyAggregatedSignature() {
        return false;
    }

    @Override
    public byte[] serialize(BigInteger value) {
        return new byte[0];
    }

    @Override
    public BigInteger deserialize(byte[] array) {
        return null;
    }
}
