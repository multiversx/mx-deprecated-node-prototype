package network.elrond.crypto;

import java.util.ArrayList;

public interface MultiSignature {
    public byte[] computeCommitment();

    public byte[] computeCommitmentHash(byte[] commitment);

    public boolean validateCommitment(byte[] commitment, byte[] commitmentHash);

    // compute or get the challenge from leader
    public byte[] computeChallenge(ArrayList<PublicKey> signers,
                                   ArrayList<byte[]> commitment,
                                   PublicKey publicKey,
                                   byte[] message,
                                   byte bitmapCommitments);

    public byte[] computeSignatureShare(byte[] challenge, PrivateKey privateKey);

    public byte[] AggregateSignatures(ArrayList<byte[]> signatureShares, byte bitmapSigners);

    public boolean VerifyAggregatedSignature(ArrayList<PublicKey> signers, byte[] aggregatedCommitment, byte[] aggregatedSignature, byte bitmapSigners, byte[] message);
}
