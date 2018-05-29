package network.elrond.processor.impl;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.processor.AppTask;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AccountsStarterProcessor implements AppTask {

    public static final String ACCOUNTS_DATA = "blockchain.accounts.data";

    @Override
    public void process(Application application) throws IOException {

        AppContext context = application.getContext();

        AppState state = application.getState();

        String workingDirectory = System.getProperty("user.dir");
        String storageBasePath = context.getStorageBasePath();
        Path databasePath = Paths.get(workingDirectory, storageBasePath, ACCOUNTS_DATA);

        AccountsContext accountContext = new AccountsContext();
        accountContext.setDatabasePath(databasePath.toString());

        Accounts accounts = new Accounts(accountContext);
        state.setAccounts(accounts);
    }

}
