package network.elrond.account;

import java.math.BigInteger;

public class AccountState {


    private BigInteger nonce;
    private BigInteger balance;
    private boolean dirty;

    public AccountState() {
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        if (nonce == null || nonce.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException();
        }

        if (balance == null || balance.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException();
        }

        this.nonce = nonce;
        this.balance = balance;
    }

    public AccountState(AccountState source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }

        setNonce(source.getNonce());
        setBalance(source.getBalance());
    }


    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        if (nonce == null || nonce.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException();
        }

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

        if (value == null) {
            throw new IllegalArgumentException();
        }

        if (balance.add(value).compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Balance would be negative!!!");
        }

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
