package network.elrond.data;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;

import java.math.BigInteger;

public interface BootstrapService {

    //returns max block height from location
    BigInteger getMaxBlockSize(LocationType locationType, Blockchain structure) throws Exception;

    //sets max block height in location
    void setMaxBlockSize(LocationType locationType, BigInteger height, Blockchain structure) throws Exception;

    //gets the hash for the block height from location
    String getBlockHashFromHeight(LocationType locationType, BigInteger blockHeight, Blockchain structure) throws Exception;

    //sets the hash for a block height in location
    void setBlockHashWithHeight(LocationType locationType, BigInteger blockHeight, String hash, Blockchain structure) throws Exception;

    ExecutionReport startFromScratch(AppState state, AppContext context);

    ExecutionReport synchronize(AppState state, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw);

    ExecutionReport rebuildFromDisk(AppState state, BigInteger maxBlkHeightLocal);

    //ExecutionReport rebuildFromDiskDeltaNoExec(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw);

    ExecutionReport putBlockInBlockchain(Block blk, String blockHash, Blockchain blockchain);

    ExecutionReport putTransactionInBlockchain(Transaction transaction, String transactionHash, Blockchain blockchain);

}
