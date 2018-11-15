package network.elrond.crypto;

public class PKSKPair {

    private final String publicKey;
    private final String privateKey;

    public PKSKPair(String publicKey, String privateKey)
    {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public String toString(){
        return ("Public key: " + publicKey + ", Private key: " + privateKey);
    }
}
