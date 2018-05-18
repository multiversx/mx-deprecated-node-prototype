package network.elrond.data;

import network.elrond.application.AppState;

public interface BlockService {
    String encodeJSON(Block blk, boolean withSig);
    byte[] getHash(Block blk, boolean withSig);
    String getHashAsString(Block blk, boolean withSig);
    Block decodeJSON(String strJSONData);
    void solveBlocks(AppState appState);
}
