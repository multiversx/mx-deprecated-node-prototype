package network.elrond.account;

import static network.elrond.core.ByteUtil.EMPTY_BYTE_ARRAY;
import network.elrond.core.RLP;

import java.math.BigInteger;

public class AccountState {

    private byte[] rlpEncoded;

    private BigInteger nonce;
    private BigInteger balance;
    private byte[] stateRoot = EMPTY_BYTE_ARRAY;
    private boolean dirty;

    public AccountState() {
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance) {
        this.nonce = nonce;
        this.balance = balance;
    }

    public AccountState(AccountState source) {
        this.nonce = source.getNonce();
        this.balance = source.getBalance();
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        rlpEncoded = null;
        this.nonce = nonce;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        rlpEncoded = null;
        this.balance = balance;
    }

    public byte[] getStateRoot() { return stateRoot; }

    public void setStateRoot(byte[] stateRoot) {
        rlpEncoded = null;
        this.stateRoot = stateRoot;
    }

    public BigInteger addToBalance(BigInteger value) {
        if (value.signum() != 0) rlpEncoded = null;
        this.balance = balance.add(value);
        return this.balance;
    }

    public BigInteger subFromBalance(BigInteger value) {
        if (value.signum() != 0) rlpEncoded = null;
        this.balance = balance.subtract(value);
        return this.balance;
    }

    public byte[] getEncoded() {
        if(rlpEncoded == null) {
            byte[] nonce		= RLP.encodeBigInteger(this.nonce);
            byte[] balance		= RLP.encodeBigInteger(this.balance);
            byte[] stateRoot	= RLP.encodeElement(this.stateRoot);
            this.rlpEncoded = RLP.encodeList(nonce, balance, stateRoot);
        }
        return rlpEncoded;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
