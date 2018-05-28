package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.p2p.P2PConnection;

import java.io.IOException;
import java.math.BigInteger;

public interface BootstrapService {

    BigInteger getMaxBlockSizeLocal(Blockchain structure) throws IOException, ClassNotFoundException;

    BigInteger getMaxBlockSizeNetwork(P2PConnection connection) throws IOException, ClassNotFoundException;

    String getBlockHashFromBlockHeight(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException;

    void setMaxBlockSizeLocal(Blockchain structure, BigInteger height) throws IOException, ClassNotFoundException;

    void setMaxBlockSizeNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException;

    void setBlockHeightHashNetwork(BigInteger blockHeight, String strHash, P2PConnection connection) throws IOException;

    String getBlockHeightHashNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException, ClassNotFoundException;

}
