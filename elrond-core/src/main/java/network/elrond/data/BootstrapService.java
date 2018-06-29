package network.elrond.data;

import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;

import java.math.BigInteger;

public interface BootstrapService {

    //returns max block height from location
    BigInteger getCurrentBlockIndex(LocationType locationType, Blockchain blockchain) throws Exception;

    //sets max block height in location
    void setCurrentBlockIndex(LocationType locationType, BigInteger height, Blockchain blockchain) throws Exception;

    //gets the hash for the block height from location
    String getBlockHashFromIndex(BigInteger blockIndex, Blockchain blockchain) throws Exception;

    //sets the hash for a block height in location
    void setBlockHashWithIndex(BigInteger blockIndex, String blockHash, Blockchain blockchain) throws Exception;

    ExecutionReport startFromGenesis(AppState state, AppContext context);

    ExecutionReport synchronize(BigInteger localBlockIndex, BigInteger remoteBlockIndex, AppState state);

    ExecutionReport restoreFromDisk(BigInteger currentBlockIndex, AppState state, AppContext context);

//    ExecutionReport commitBlock(Block blk, String blockHash, Blockchain blockchain);
    ExecutionReport commitBlock(Block blk, String blockHash, Blockchain blockchain, Accounts accounts); // PMS: 2018.06.29

    ExecutionReport commitTransaction(Transaction transaction, String transactionHash, Blockchain blockchain);

    SyncState getSyncState(Blockchain blockchain) throws Exception;
}
