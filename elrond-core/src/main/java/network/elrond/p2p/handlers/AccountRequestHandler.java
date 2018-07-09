package network.elrond.p2p.handlers;

import network.elrond.account.AccountAddress;
import network.elrond.account.AccountState;
import network.elrond.application.AppState;
import network.elrond.p2p.P2PRequestMessage;
import network.elrond.p2p.RequestHandler;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AccountRequestHandler implements RequestHandler<AccountState, P2PRequestMessage> {
    private static final Logger logger = LogManager.getLogger(AccountRequestHandler.class);

    @Override
    public AccountState onRequest(AppState state, P2PRequestMessage data) {

        logger.traceEntry("params: {} {}", state, data);
        AccountAddress address = (AccountAddress) data.getKey();
        try {
            AccountState accountState = AppServiceProvider.getAccountStateService().getAccountState(address, state.getAccounts());
            return logger.traceExit(accountState);
        } catch (IOException | ClassNotFoundException e) {
            logger.throwing(e);
            throw new RuntimeException(e);
        }
    }
}
