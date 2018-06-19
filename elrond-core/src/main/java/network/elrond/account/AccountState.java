package network.elrond.account;

import network.elrond.core.Util;

import java.math.BigInteger;

public class AccountState {


    private BigInteger nonce;
    private BigInteger balance;
    private boolean dirty;

    public AccountState() {
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        Util.check(!(nonce == null || nonce.compareTo(BigInteger.ZERO) < 0), "nonce>=0");
        Util.check(!(balance == null || balance.compareTo(BigInteger.ZERO) < 0), "balance>=0");

        this.nonce = nonce;
        this.balance = balance;
    }

    public AccountState(AccountState source) {
        Util.check(source!=null, "source!=null");

        setNonce(source.getNonce());
        setBalance(source.getBalance());
    }


    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        Util.check(!(nonce == null || nonce.compareTo(BigInteger.ZERO) < 0), "nonce>=0");
        Util.check( !(this.nonce != null && this.nonce.compareTo(nonce)>0), "new nonce should be bigger");
        this.nonce = nonce;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        if (balance == null || balance.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException();
        }

        this.balance = balance;
    }

    public BigInteger addToBalance(BigInteger value) {
        Util.check(value!=null, "value!=null");
        Util.check(balance.add(value).compareTo(BigInteger.ZERO) > 0, "Balance would be negative!!!");

        this.balance = balance.add(value);
        return this.balance;
    }

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
