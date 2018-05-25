package network.elrond.crypto;

public interface SignatureService {

    Signature signMessage(byte[] message, byte[] privateKey, byte[] publicKey);

    Signature signMessage(String message, byte[] privateKey, byte[] publicKey);

    boolean verifySignature(byte[] signature, byte[] challenge, byte[] message, byte[] publicKey);

    boolean verifySignature(byte[] signature, byte[] challenge, String message, byte[] publicKey);
}
