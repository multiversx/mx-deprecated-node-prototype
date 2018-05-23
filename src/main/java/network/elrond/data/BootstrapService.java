package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.p2p.P2PConnection;

import java.io.IOException;
import java.math.BigInteger;

public interface BootstrapService {
    BigInteger getMaxBlockSizeLocal(Blockchain structure) throws IOException, ClassNotFoundException;
    BigInteger getMaxBlockSizeNetwork(P2PConnection connection) throws IOException, ClassNotFoundException,
            NullPointerException;
    String getBlockHashFromBlockHeight(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException;
    void setMaxBlockSizeLocal(Blockchain structure, BigInteger height) throws IOException, ClassNotFoundException;

}
