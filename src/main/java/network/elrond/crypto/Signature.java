package network.elrond.crypto;

public class Signature {
    private byte[] signature;
    private byte[] commitment;

    public Signature(byte[] signature, byte[] commitment) {
        this.signature = signature.clone();
        this.commitment = commitment.clone();
    }

    public byte[] getSignature() {
        return signature.clone();
    }

    public byte[] getCommitment() {
        return commitment.clone();
    }

    public void setSignature(byte[] signature) {
        this.signature = signature.clone();
    }

    public void setCommitment(byte[] commitment) {
        this.commitment = commitment.clone();
    }
}
