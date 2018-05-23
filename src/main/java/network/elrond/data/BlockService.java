package network.elrond.data;

import network.elrond.blockchain.Blockchain;

public interface BlockService {

    String encodeJSON(Block blk, boolean withSig);

    byte[] getHash(Block blk, boolean withHash);

    Block decodeJSON(String strJSONData);

    void executeBlock(Blockchain blkc, Block blk) throws Exception;
}
