package network.elrond.data;

import network.elrond.Application;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.p2p.P2PConnection;

import java.io.IOException;
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

    ExecutionReport startFromScratch(Application application);

    ExecutionReport bootstrap(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw);

    ExecutionReport rebuildFromDisk(Application application, BigInteger maxBlkHeightLocal);

    ExecutionReport rebuildFromDiskDeltaNoExec(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw);

    ExecutionReport putBlockInBlockchain(Block blk, String blockHash, AppState state);

}
