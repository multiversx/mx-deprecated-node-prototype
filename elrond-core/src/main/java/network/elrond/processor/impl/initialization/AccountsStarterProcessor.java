package network.elrond.processor.impl.initialization;

import network.elrond.Application;
import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.account.AccountsPersistenceUnit;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.processor.AppTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AccountsStarterProcessor implements AppTask {

    public static final String ACCOUNTS_DATA = "blockchain.accounts.data";
    private static final Logger logger = LogManager.getLogger(AccountsStarterProcessor.class);

    @Override
    public void process(Application application) throws IOException {
        logger.traceEntry("params: {}", application);
        AppContext context = application.getContext();

        AppState state = application.getState();

        String workingDirectory = System.getProperty("user.dir");
        String storageBasePath = context.getStorageBasePath();
        Path databasePath = Paths.get(workingDirectory, storageBasePath, ACCOUNTS_DATA);

        AccountsContext accountContext = new AccountsContext();
        accountContext.setDatabasePath(databasePath.toString());
        accountContext.setShard(state.getShard());

        Accounts accounts = new Accounts(accountContext, new AccountsPersistenceUnit<>(accountContext.getDatabasePath(), 100000));
        state.setAccounts(accounts);
        logger.traceExit();
    }

}
