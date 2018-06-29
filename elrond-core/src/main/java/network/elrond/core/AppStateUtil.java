package network.elrond.core;

import network.elrond.data.Block;
import network.elrond.account.Accounts;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

public class AppStateUtil {

    private static final Logger logger = LogManager.getLogger(AppStateUtil.class);

    public static void print(Block block, Accounts accounts) {

        logger.info("\n" + block.print().render());
        logger.info("\n" + AsciiTableUtil.listToTables(accounts.getAddresses()
                .stream()
                .map(accountAddress -> {
                    try {
                        return AppServiceProvider.getAccountStateService().getAccountState(accountAddress, accounts);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList())));
    }
}
