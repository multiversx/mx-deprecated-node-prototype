package network.elrond.crypto;

public class ECKeyPair implements IKeyPair {

    private ECKeyPair(){

    }

    public ECKeyPair(){

    }

    @Override
    public String toSting(){
        return "";
    }

    public byte[] getPrivateKey() {
        return new byte[0];
    }

    public byte[] getPublicKey() {
        return new byte[0];
    }

    public IKeyPair clone() throws CloneNotSupportedException {
        return null;
    }
}
