package network.elrond.data;

import network.elrond.core.Util;
//import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

//import java.util.Date;

public class Block {

    private String hash;
    private String previousHash;
    private String data;
//    private long timeStamp;

    public Block(String data, String previousHash ) {
        this.data = data;
        this.previousHash = previousHash;
//        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    private String calculateHash() {
        return Util.applySha256(
                previousHash +
//                        Long.toString(timeStamp) +
                        data
        );
    }

    public String getHash() { return (hash); }
    public String getData() { return (data); }
}
