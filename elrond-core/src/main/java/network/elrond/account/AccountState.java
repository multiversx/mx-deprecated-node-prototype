package network.elrond.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        if (nonce == null || nonce.compareTo(BigInteger.ZERO) < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("nonce should not be null nor negative");
            logger.throwing(ex);
            throw ex;
        }

        if (balance == null || balance.compareTo(BigInteger.ZERO) < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("balance should not be null nor negative");
            logger.throwing(ex);
            throw ex;
        }

        this.nonce = nonce;
        this.balance = balance;
        logger.traceExit();
    }

    public AccountState(AccountState source) {
        logger.traceEntry("params: {}", source);
        if (source == null) {
            IllegalArgumentException ex = new IllegalArgumentException("source object can not be null");
            logger.throwing(ex);
            throw ex;
        }

        setNonce(source.getNonce());
        setBalance(source.getBalance());
        logger.traceExit();
    }


    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        logger.traceEntry("params: {}", nonce);
        if (nonce == null || nonce.compareTo(BigInteger.ZERO) < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("nonce should not be null nor negative");
            logger.throwing(ex);
            throw ex;
        }

        this.nonce = nonce;
        logger.traceExit();
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        logger.traceEntry("params: {}", balance);
        if (balance == null || balance.compareTo(BigInteger.ZERO) < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("balance should not be null nor negative");
            logger.throwing(ex);
            throw ex;
        }

        this.balance = balance;
        logger.traceExit();
    }

    public BigInteger addToBalance(BigInteger value) {
        logger.traceEntry("params: {}", value);
        if ((value == null) || (value.compareTo(BigInteger.ZERO) < 0)) {
            IllegalArgumentException ex = new IllegalArgumentException("value should not be null nor negative");
            logger.throwing(ex);
            throw ex;
        }

        if (balance.add(value).compareTo(BigInteger.ZERO) < 0) {
            IllegalArgumentException ex = new IllegalArgumentException("balance would be negative!!!");
            logger.throwing(ex);
            throw ex;
        }

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
