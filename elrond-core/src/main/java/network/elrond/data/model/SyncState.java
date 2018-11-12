package network.elrond.data.model;

import java.math.BigInteger;

public class SyncState {
    boolean syncRequired;
    BigInteger localBlockIndex;
    BigInteger remoteBlockIndex;
    boolean valid;

    public SyncState(){
        syncRequired = false;
        valid = false;
        localBlockIndex = BigInteger.valueOf(-1);
        remoteBlockIndex = BigInteger.valueOf(-1);
    }

    public SyncState(boolean syncRequired, boolean valid, BigInteger localBlockIndex, BigInteger remoteBlockIndex){
        this.syncRequired = syncRequired;
        this.valid = valid;
        this.localBlockIndex = localBlockIndex;
        this.remoteBlockIndex = remoteBlockIndex;
    }

    public boolean isSyncRequired(){
        return (syncRequired);
    }

    public void setSyncRequired(boolean syncRequired){
        this.syncRequired = syncRequired;
    }

    public boolean isValid(){
        return (valid);
    }

    public void setValid(boolean valid){
        this.valid = valid;
    }

    public BigInteger getLocalBlockIndex(){
        return (localBlockIndex);
    }

    public void setLocalBlockIndex(BigInteger localBlockIndex){
        this.localBlockIndex = localBlockIndex;
    }

    public BigInteger getRemoteBlockIndex(){
        return (remoteBlockIndex);
    }

    public void setRemoteBlockIndex(BigInteger remoteBlockIndex){
        this.remoteBlockIndex = remoteBlockIndex;
    }
}
