package network.elrond.crypto;


public interface KeyPair extends Cloneable{
    PrivateKey getPrivateKey();
    PublicKey getPublicKey();
    KeyPair clone() throws CloneNotSupportedException;
}
