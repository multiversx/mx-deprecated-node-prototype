package network.elrond.crypto;

import java.util.ArrayList;

public interface MultiSignatureService {
    public byte[] computeCommitmentSecret();

    public byte[] computeCommitment(byte[] commitmentSecret);

    public byte[] computeCommitmentHash(byte[] commitment);

    public boolean validateCommitment(byte[] commitment,
                                      byte[] commitmentHash);

    public byte[] aggregateCommitments(ArrayList<byte[]> commitments,
                                       long bitmapCommitments);

    // compute or get the challenge from leader
    public byte[] computeChallenge(ArrayList<PublicKey> signers,
                                   PublicKey publicKey,
                                   byte[] aggregatedCommitment,
                                   byte[] message,
                                   long bitmapCommitments);

    public byte[] computeSignatureShare(byte[] challenge,
                                        PrivateKey privateKey,
                                        byte[] commitmentSecret);

    public boolean verifySignatureShare(ArrayList<PublicKey> publicKeys,
                                        PublicKey publicKey,
                                        byte[] signature,
                                        byte[] aggCommitment,
                                        byte[] commitment,
                                        byte[] message,
                                        long bitmap);

    public byte[] aggregateSignatures(ArrayList<byte[]> signatureShares,
                                      long bitmapSigners);

    public boolean verifyAggregatedSignature(ArrayList<PublicKey> signers,
                                             byte[] aggregatedSignature,
                                             byte[] aggregatedCommitment,
                                             byte[] message,
                                             long bitmapSigners);
}
