package network.elrond.crypto;

import java.util.ArrayList;

public interface MultiSignatureService {
    byte[] computeCommitmentSecret();

    byte[] computeCommitment(byte[] commitmentSecret);

    byte[] computeCommitmentHash(byte[] commitment);

    boolean validateCommitment(byte[] commitment,
                               byte[] commitmentHash);

    byte[] aggregateCommitments(ArrayList<byte[]> commitments,
                                long bitmapCommitments);

    // compute or get the challenge from leader
    byte[] computeChallenge(ArrayList<byte[]> signers,
                            byte[] publicKey,
                            byte[] aggregatedCommitment,
                            byte[] message,
                            long bitmapCommitments);

    byte[] computeSignatureShare(byte[] challenge,
                                 byte[] privateKey,
                                 byte[] commitmentSecret);

    boolean verifySignatureShare(ArrayList<byte[]> publicKeys,
                                 byte[] publicKey,
                                 byte[] signature,
                                 byte[] aggCommitment,
                                 byte[] commitment,
                                 byte[] message,
                                 long bitmap);

    byte[] aggregateSignatures(ArrayList<byte[]> signatureShares,
                               long bitmapSigners);

    boolean verifyAggregatedSignature(ArrayList<byte[]> signers,
                                      byte[] aggregatedSignature,
                                      byte[] aggregatedCommitment,
                                      byte[] message,
                                      long bitmapSigners);
}
