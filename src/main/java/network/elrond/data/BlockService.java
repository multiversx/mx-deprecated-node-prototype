package network.elrond.data;

import network.elrond.blockchain.Blockchain;

public interface BlockService {

    byte[] getHash(Block blk, boolean withHash);

    void executeBlock(Blockchain blkc, Block blk) throws Exception;

}
