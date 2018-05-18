package network.elrond.data;

import network.elrond.core.Util;

import java.math.BigInteger;

public class AccountState {
    private BigInteger nonce;
    private BigInteger balance;
    private BigInteger validatorLockedStake;
    private int validatorReputation;
    private int validatorShardNo;

    public AccountState(){
        this(BigInteger.ZERO, BigInteger.ZERO);
    }

    public AccountState(BigInteger nonce, BigInteger balance){
        this.nonce = nonce;
        this.balance = balance;
        validatorLockedStake = BigInteger.ZERO;
        validatorReputation = 0;
        validatorShardNo = 0;
    }

    public AccountState(AccountState src){
        this.nonce =src.getNonce();
        this.balance = src.getBalance();
        this.validatorLockedStake = src.getValidatorLockedStake();
        this.validatorReputation = src.validatorReputation;
        this.validatorShardNo = src.validatorShardNo;
    }

    public BigInteger getNonce(){ return nonce;}

    public void setNonce(BigInteger nonce){ this.nonce = nonce; }

    public BigInteger getBalance(){ return balance; }

    public void setBalance(BigInteger balance){ this.balance = balance; }

    public BigInteger getValidatorLockedStake() { return validatorLockedStake; }

    public void setValidatorLockedStake(BigInteger validatorLockedStake) { this.validatorLockedStake = validatorLockedStake;}

    public int getValidatorReputation() {return validatorReputation;}

    public void setValidatorReputation(int validatorReputation) {this.validatorReputation = validatorReputation;}

    public int getValidatorShardNo(){return validatorShardNo;}

    public void setValidatorShardNo(int shard) {this.validatorShardNo = validatorShardNo;}
}
