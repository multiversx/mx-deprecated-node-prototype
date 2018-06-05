package network.elrond.crypto;

public class PKSKPair {

    private String publicKey;
    private String privateKey;

    public PKSKPair(String publicKey, String privateKey)
    {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString(){
        return ("Public key: " + publicKey + ", Private key: " + privateKey);
    }

}
