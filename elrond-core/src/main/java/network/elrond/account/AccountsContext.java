package network.elrond.account;

import network.elrond.sharding.Shard;

import java.io.Serializable;

public class AccountsContext implements Serializable {

    private String databasePath;

    private Shard shard;

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    public Shard getShard() {
        return shard;
    }

    public void setShard(Shard shard) {
        this.shard = shard;
    }
}
