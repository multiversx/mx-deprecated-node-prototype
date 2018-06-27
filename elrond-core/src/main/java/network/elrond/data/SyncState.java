package network.elrond.data;

import java.math.BigInteger;

public class SyncState {
    boolean syncRequired;
    BigInteger localBlockIndex;
    BigInteger remoteBlockIndex;

    public SyncState(){
        syncRequired = false;
        localBlockIndex = BigInteger.valueOf(-1);
        remoteBlockIndex = BigInteger.valueOf(-1);
    }

    public SyncState(boolean syncRequired, BigInteger localBlockIndex, BigInteger remoteBlockIndex){
        this.syncRequired = syncRequired;
        this.localBlockIndex = localBlockIndex;
        this.remoteBlockIndex = remoteBlockIndex;
    }

    public boolean isSyncRequired(){
        return (syncRequired);
    }

    public void setSyncRequired(boolean syncRequired){
        this.syncRequired = syncRequired;
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
