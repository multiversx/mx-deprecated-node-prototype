package network.elrond.account;

import network.elrond.AsciiTable;
import network.elrond.core.Util;
import network.elrond.data.AsciiPrintable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.math.BigInteger;

public class AccountState implements Serializable, AsciiPrintable {
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
        Util.check(source != null, "source!=null");

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
        Util.check(!(this.nonce != null && this.nonce.compareTo(nonce) > 0), "new nonce should be bigger");
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
        Util.check(value != null, "value!=null");
        Util.check(value.compareTo(BigInteger.ZERO) > 0, "value must be positive");
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

    @Override
    public AsciiTable print() {

        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(20);

        table.getColumns().add(new AsciiTable.Column("Nonce " + nonce));
        table.getColumns().add(new AsciiTable.Column(""));

        AsciiTable.Row row0 = new AsciiTable.Row();
        row0.getValues().add("Nonce");
        row0.getValues().add(nonce.toString());
        table.getData().add(row0);

        AsciiTable.Row row1 = new AsciiTable.Row();
        row1.getValues().add("Balance");
        row1.getValues().add(balance + "");
        table.getData().add(row1);


        table.calculateColumnWidth();
        return table;
    }
}
