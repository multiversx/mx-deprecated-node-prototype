package network.elrond.crypto;


public interface KeyPair extends Cloneable{
    byte[] getPrivateKey();
    byte[] getPublicKey();
    KeyPair clone() throws CloneNotSupportedException;
    //KeyPair getReadOnly();
}
