package network.elrond.account;

import java.math.BigInteger;

public class AccountState {

//    private byte[] rlpEncoded;

    private BigInteger nonce;
    private BigInteger balance;
    private boolean dirty;

    public AccountState() {
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        if(nonce == null || nonce.compareTo(BigInteger.ZERO) < 0){
            throw new IllegalArgumentException();
        }

        if(balance == null || balance.compareTo(BigInteger.ZERO) < 0){
            throw new IllegalArgumentException();
        }

        this.nonce = nonce;
        this.balance = balance;
    }

    public AccountState(AccountState source) {
        if(source == null){
            throw new IllegalArgumentException();
        }

        setNonce(source.getNonce());
        setBalance(source.getBalance());
    }

//    public AccountState(byte[] rlpData) {
//        this.rlpEncoded = rlpData;
//
//        RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
//        this.nonce = new BigInteger(1, ((items.get(0).getRLPData()) == null ? new byte[]{0} :
//                items.get(0).getRLPData()));
//        this.balance = new BigInteger(1, ((items.get(1).getRLPData()) == null ? new byte[]{0} :
//                items.get(1).getRLPData()));
//    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        //rlpEncoded = null;
        if(nonce == null || nonce.compareTo(BigInteger.ZERO) < 0){
            throw new IllegalArgumentException();
        }

        this.nonce = nonce;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        //rlpEncoded = null;
        if(balance == null || balance.compareTo(BigInteger.ZERO) < 0){
            throw new IllegalArgumentException();
        }

        this.balance = balance;
    }

    public BigInteger addToBalance(BigInteger value) {
        //if (value.signum() != 0) rlpEncoded = null;
        if(value == null){
            throw new IllegalArgumentException();
        }

        if(balance.add(value).compareTo(BigInteger.ZERO) < 0){
            throw new IllegalArgumentException("Balance would be negative!!!");
        }

        this.balance = balance.add(value);
        return this.balance;
    }

//    public BigInteger subFromBalance(BigInteger value) {
//        if (value.signum() != 0) rlpEncoded = null;
//        this.balance = balance.subtract(value);
//        return this.balance;
//    }

//    public byte[] getEncoded() {
//        if (rlpEncoded == null) {
//            byte[] nonce = RLP.encodeBigInteger(this.nonce);
//            byte[] balance = RLP.encodeBigInteger(this.balance);
//            this.rlpEncoded = RLP.encodeList(nonce, balance);
//        }
//        return rlpEncoded;
//    }

    public String toString() {
        String ret = "Nonce: " + this.getNonce().toString() + "\n" +
                "Balance: " + this.getBalance().toString() + "\n";

        return ret;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
