package network.elrond.crypto;

import java.math.BigInteger;

//TODO: parameters & return types need to be decided/confirmed.
public interface MultiSignature {

    public BigInteger getRandom();

    public BigInteger computeCommitment(BigInteger pubKey);

    public BigInteger computeCommitmentHash(BigInteger commitment);

    public boolean validateCommitment(BigInteger commitment, BigInteger commitmentHash);

    // compute or get the challenge from leader
    public BigInteger computeChallenge();

    public BigInteger computeSignatureShare();

    public boolean VerifySignatureShare();

    public BigInteger AggregateSignatures();

    public boolean VerifyAggregatedSignature();

    public byte[] serialize(BigInteger value);

    public BigInteger deserialize(byte[] array);

}
