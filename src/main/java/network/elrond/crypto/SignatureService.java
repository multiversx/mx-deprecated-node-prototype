package network.elrond.crypto;

public interface SignatureService {

    public Signature signMessage(byte[] message, PrivateKey privateKey, PublicKey publicKey);

    public Signature signMessage(String message, PrivateKey privateKey, PublicKey publicKey);

    public boolean verifySignature(byte[] signature, byte[] challenge, byte[] message, PublicKey publicKey);

    public boolean verifySignature(byte[] signature, byte[] challenge, String message, PublicKey publicKey);
}
