package network.elrond.data;

public interface BlockService {



    byte[] getHash(Block blk, boolean withHash);

//    String encodeJSON(Block blk, boolean withSig);
//    Block decodeJSON(String strJSONData);
}
