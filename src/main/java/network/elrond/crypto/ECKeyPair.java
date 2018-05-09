package network.elrond.crypto;

public class ECKeyPair implements KeyPair {

    private ECKeyPair() {

    }

    @Override
    public String toString(){
        return "";
    }

    public byte[] getPrivateKey() {
        return new byte[0];
    }

    public byte[] getPublicKey() {
        return new byte[0];
    }

    public KeyPair clone() throws CloneNotSupportedException {
        return null;
    }
}
