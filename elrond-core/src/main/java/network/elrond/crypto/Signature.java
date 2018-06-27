package network.elrond.crypto;

import java.io.Serializable;
import java.math.BigInteger;

public class Signature implements Serializable {
    private byte[] signature;
    private byte[] commitment;
    private byte[] challenge;

    public Signature() {
        signature = new byte[0];
        commitment = new byte[0];
        challenge = new byte[0];
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
        byte[] result = signature;

        if (0 != signature.length) {
            result = signature.clone();
        }

        return result;
    }

    public byte[] getCommitment() {
        byte[] result = commitment;

        if (0 != commitment.length) {
            result = commitment.clone();
        }
        return result;
    }

    public byte[] getChallenge() {
        byte[] result = challenge;

        if (0 != challenge.length) {
            result = challenge.clone();
        }

        return result;
    }

    public boolean setSignature(byte[] signature) {

        if (0 == signature.length || (new BigInteger(signature)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.signature = signature.clone();

        return true;
    }

    public boolean setCommitment(byte[] commitment) {

        if (0 == commitment.length || (new BigInteger(commitment)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.commitment = commitment.clone();

        return true;
    }

    public boolean setChallenge(byte[] challenge) {
        if (0 == challenge.length || (new BigInteger(challenge)).equals(BigInteger.ZERO)) {
            return false;
        }

        this.challenge = challenge.clone();

        return true;
    }
}
