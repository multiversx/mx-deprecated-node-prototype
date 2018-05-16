package network.elrond.data;

public interface BlockService {
    String encodeJSON(Block blk, boolean withSig);
    byte[] getHash(Block blk, boolean withHash);
    Block decodeJSON(String strJSONData);
}
