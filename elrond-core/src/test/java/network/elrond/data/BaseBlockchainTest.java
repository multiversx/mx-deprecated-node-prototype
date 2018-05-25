package network.elrond.data;

import network.elrond.blockchain.BlockchainContext;
import network.elrond.blockchain.BlockchainUnitType;

import java.io.IOException;
import java.util.Random;

public abstract class BaseBlockchainTest {

    public static final String BLOCKCHAIN_BLOCK_DATA_TEST_PATH = "tests/blockchain.block.data-test";
    public static final String BLOCKCHAIN_TRANSACTION_DATA_TEST_PATH = "tests/blockchain.transaction.data-test";
    public static final String BLOCKCHAIN_SETTINGS_DATA_TEST_PATH = "tests/blockchain.settings.data-test";
    public static final String BLOCKCHAIN_BLOCKIDX_DATA_TEST_PATH = "tests/blockchain.blockidx.data-test";

    public BlockchainContext getDefaultTestBlockchainContext() throws IOException {
        Random r = new Random(System.currentTimeMillis());
        String currentDir = r.nextInt()+"";

        BlockchainContext context = new BlockchainContext();
        context.setDatabasePath(BlockchainUnitType.BLOCK, BLOCKCHAIN_BLOCK_DATA_TEST_PATH + currentDir);
        context.setDatabasePath(BlockchainUnitType.TRANSACTION, BLOCKCHAIN_TRANSACTION_DATA_TEST_PATH+ currentDir);
        context.setDatabasePath(BlockchainUnitType.SETTINGS, BLOCKCHAIN_SETTINGS_DATA_TEST_PATH+ currentDir);
        context.setDatabasePath(BlockchainUnitType.BLOCK_INDEX, BLOCKCHAIN_BLOCKIDX_DATA_TEST_PATH+ currentDir);
        return context;
    }
}