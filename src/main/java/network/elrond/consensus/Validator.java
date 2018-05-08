package network.elrond.consensus;

public class Validator {

    private String pubKey;
    private String ip;
    /*......*/

    public Validator(String pubKey, String ip)
    {
        this.pubKey = pubKey;
        this.ip = ip;
    }
}
