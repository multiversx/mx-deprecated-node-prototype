package network.elrond.crypto;

import java.math.BigInteger;

public class Signature {
    private byte[] signature;
    private byte[] commitment;
    private byte[] challenge;

    public Signature() {
        signature = new byte[0];
        commitment = new byte[0];
        signature = new byte[0];
    }

    public Signature(byte[] signature) {
        this.signature = signature.clone();
        challenge = new byte[0];
        commitment = new byte[0];
    }

    public Signature(byte[] signature, byte[] commitment) {
        this.signature = signature.clone();
        this.commitment = commitment.clone();
        challenge = new byte[0];
    }

    public byte[] getSignature() {
        return signature.clone();
    }

    public byte[] getCommitment() {
        return commitment.clone();
    }

    public byte[] getChallenge() {
        return challenge.clone();
    }

    public boolean setSignature(byte[] signature) {

        if (null == signature || (new BigInteger(signature)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.signature = signature.clone();

        return true;
    }

    public boolean setCommitment(byte[] commitment) {

        if (null == commitment || (new BigInteger(commitment)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.commitment = commitment.clone();

        return true;
    }

    public boolean setChallenge(byte[] challenge) {
        if (null == challenge || (new BigInteger(challenge)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.challenge = challenge.clone();

        return true;
    }
}
