package network.elrond.crypto;

public class ECKeyPair implements KeyPair {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    /**
     * Default constructor
     * Creates a new pair of (private, public) keys
     */
    public ECKeyPair() {
        privateKey = new PrivateKey();
        publicKey = new PublicKey(privateKey);
    }

    /**
     * Constructor
     * Creates the pair of (private, public) keys from the private key
     *
     * @param privateKey the private key
     */
    public ECKeyPair(PrivateKey privateKey) {
        this.privateKey = privateKey;
        publicKey = new PublicKey(privateKey);
    }

    /**
     * Getter for the private key
     *
     * @return the private key
     */
    @Override
	public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Getter for the public key
     *
     * @return the public key
     */
    @Override
	public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
	public KeyPair clone() throws CloneNotSupportedException {
        return (KeyPair) super.clone();
    }
}
