package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.p2p.P2PConnection;

import java.io.IOException;
import java.math.BigInteger;

public interface BootstrapService {
    //returns max block height from local data (disk)
    BigInteger getMaxBlockSizeLocal(Blockchain structure) throws IOException, ClassNotFoundException;

    //sets max block height on local (disk)
    void setMaxBlockSizeLocal(Blockchain structure, BigInteger height) throws IOException, ClassNotFoundException;

    //returns max block height from network (DHT)
    BigInteger getMaxBlockSizeNetwork(P2PConnection connection) throws IOException, ClassNotFoundException;

    //sets max block height on network (DHT)
    void setMaxBlockSizeNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException;

    //gets the hash for the block height from local data (disk)
    String getBlockHashFromHeightLocal(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException;

    //sets the hash for a block height on local (disk)
    void setBlockHashFromHeightLocal(Blockchain structure, BigInteger blockHeight, String strHash) throws IOException;

    //gets the hash for the block height from network (DHT)
    String getBlockHashFromHeightNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException, ClassNotFoundException;

    //sets the hash for a block height on network (DHT)
    void setBlockHashFromHeightNetwork(BigInteger blockHeight, String strHash, P2PConnection connection) throws IOException;
}
