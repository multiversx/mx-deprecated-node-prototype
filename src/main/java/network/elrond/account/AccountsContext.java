package network.elrond.account;

import java.io.Serializable;

public class AccountsContext implements Serializable {


    private String databasePath;

    public String getDatabasePath() {
        return databasePath;
    }

    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }
}
