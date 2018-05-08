package network.elrond.crypto;


public interface IKeyPair extends Cloneable{

    byte[] getPrivateKey();
    byte[] getPublicKey();
    IKeyPair clone() throws CloneNotSupportedException;
    //IKeyPair getReadOnly();
}
