package network.elrond.blockchain;

import network.elrond.core.Util;
import network.elrond.p2p.model.P2PConnection;
import network.elrond.sharding.Shard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BlockchainContext implements Serializable {

    private P2PConnection connection;
    private Shard shard;
    private Map<BlockchainUnitType, String> databasePaths = new HashMap<>();

    public Shard getShard() {
        return shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
    }

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
