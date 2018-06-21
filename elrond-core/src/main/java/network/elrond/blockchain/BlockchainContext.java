package network.elrond.blockchain;

import network.elrond.core.Util;
import network.elrond.p2p.P2PConnection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BlockchainContext implements Serializable {

    private P2PConnection connection;

    private Map<BlockchainUnitType, String> databasePaths = new HashMap<>();

    public P2PConnection getConnection() {
        return connection;
    }

    public void setConnection(P2PConnection connection) {
        Util.check(connection!=null, "connection!=null");
        this.connection = connection;
    }

    public String getDatabasePath(BlockchainUnitType type) {
        return databasePaths.get(type);
    }

    public void setDatabasePath(BlockchainUnitType type, String path) {
        Util.check(!(path==null || path.isEmpty()), "path!=null");
        databasePaths.put(type, path);
    }
}
