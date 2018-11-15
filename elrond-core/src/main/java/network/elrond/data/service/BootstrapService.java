package network.elrond.data.service;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.data.model.Block;
import network.elrond.data.model.ExecutionReport;
import network.elrond.data.model.LocationType;
import network.elrond.data.model.SyncState;
import network.elrond.data.model.Transaction;

import java.math.BigInteger;

public interface BootstrapService {

    /** Returns max block height from location */
    BigInteger getCurrentBlockIndex(LocationType locationType, Blockchain blockchain);

    /** Sets max block height in location */
    void setCurrentBlockIndex(LocationType locationType, BigInteger height, Blockchain blockchain) throws Exception;

    /** Gets the hash for the block height from location */
    String getBlockHashFromIndex(BigInteger blockIndex, Blockchain blockchain) throws Exception;

    /** Sets the hash for a block height in location */
    void setBlockHashWithIndex(BigInteger blockIndex, String blockHash, Blockchain blockchain) throws Exception;

    ExecutionReport startFromGenesis(AppState state, AppContext context);

    ExecutionReport synchronize(BigInteger localBlockIndex, BigInteger remoteBlockIndex, AppState state);

    ExecutionReport restoreFromDisk(BigInteger currentBlockIndex, AppState state, AppContext context);

    ExecutionReport commitBlock(Block blk, String blockHash, Blockchain blockchain);

    ExecutionReport commitTransaction(Transaction transaction, String transactionHash, Blockchain blockchain);

    SyncState getSyncState(Blockchain blockchain);

    void setBlockHeightFromNetwork(BigInteger blockHeight, Blockchain blockchain);

    void fetchNetworkBlockIndex(Blockchain blockchain) throws java.io.IOException, ClassNotFoundException;
}
