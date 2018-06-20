package network.elrond.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import network.elrond.core.Util;

import java.math.BigInteger;

public class AccountState {
    private static final Logger logger = LogManager.getLogger(AccountState.class);

    private BigInteger nonce;
    private BigInteger balance;
    private boolean dirty;

    public AccountState() {
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        logger.traceEntry("params: {} {}", nonce, balance);
        Util.check(!(nonce == null || nonce.compareTo(BigInteger.ZERO) < 0), "nonce>=0");
        Util.check(!(balance == null || balance.compareTo(BigInteger.ZERO) < 0), "balance>=0");

        this.nonce = nonce;
        this.balance = balance;
        logger.traceExit();
    }

    public AccountState(AccountState source) {
        logger.traceEntry("params: {}", source);
        Util.check(source!=null, "source!=null");

        setNonce(source.getNonce());
        setBalance(source.getBalance());
        logger.traceExit();
    }


    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        logger.traceEntry("params: {}", nonce);
        Util.check(!(nonce == null || nonce.compareTo(BigInteger.ZERO) < 0), "nonce>=0");
        Util.check( !(this.nonce != null && this.nonce.compareTo(nonce)>0), "new nonce should be bigger");
        this.nonce = nonce;
        logger.traceExit();
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        logger.traceEntry("params: {}", balance);
        Util.check(!(balance == null || balance.compareTo(BigInteger.ZERO) < 0), "balance>=0");

        this.balance = balance;
        logger.traceExit();
    }

    public BigInteger addToBalance(BigInteger value) {
        logger.traceEntry("params: {}", value);
        Util.check(value!=null, "value!=null");
        Util.check(value.compareTo(BigInteger.ZERO)>0, "value must be positive");
        Util.check(balance.add(value).compareTo(BigInteger.ZERO) > 0, "Balance would be negative!!!");

        this.balance = balance.add(value);
        return logger.traceExit(this.balance);
    }

    public String toString() {
        return String.format("AccountState{nonce=%d, balance=%d}", this.getNonce(), this.getBalance());
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
