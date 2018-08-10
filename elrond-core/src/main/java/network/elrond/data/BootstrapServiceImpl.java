package network.elrond.data;

import network.elrond.account.Accounts;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.chronology.NTPClient;
import network.elrond.core.AppStateUtil;
import network.elrond.core.Util;
import network.elrond.p2p.P2PRequestChannelName;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class BootstrapServiceImpl implements BootstrapService {
    private static final Logger logger = LogManager.getLogger(BootstrapServiceImpl.class);

    @Override
    public BigInteger getCurrentBlockIndex(LocationType locationType, Blockchain blockchain) {
        logger.traceEntry("params: {} {}", locationType, blockchain);

        if (locationType == LocationType.BOTH) {
            logger.error("Decide from where to get the data!");
            return Util.BIG_INT_MIN_ONE;
        }

        if (locationType == LocationType.LOCAL) {
            BigInteger currentBlockIndex = blockchain.getCurrentBlockIndex();
            return logger.traceExit(currentBlockIndex);
        }

        if (locationType == LocationType.NETWORK) {
            return logger.traceExit(blockchain.getNetworkHeight());
        }

        logger.error("Unimplemented location type: {}!", locationType.toString());
        return Util.BIG_INT_MIN_ONE;
    }

    public void fetchNetworkBlockIndex(Blockchain blockchain) throws java.io.IOException, ClassNotFoundException {
        logger.traceEntry("params: {}", blockchain);

        BigInteger value = AppServiceProvider.getP2PRequestService().get(blockchain.getConnection().getRequestChannel("BLOCK_HEIGHT"),
                blockchain.getShard(), P2PRequestChannelName.BLOCK_HEIGHT, "");

        if (value == null) {
            value = Util.BIG_INT_MIN_ONE;
        }

        if (value.compareTo(blockchain.getNetworkHeight()) > 0) {
            blockchain.setNetworkHeight(value);
            logger.info("Set current network block height to value: {}", value);
        }
    }

    @Override
    public void setCurrentBlockIndex(LocationType locationType, BigInteger currentBlockIndex, Blockchain blockchain) throws Exception {
        logger.traceEntry("params: {} {} {}", locationType, currentBlockIndex, blockchain);
        if ((locationType.getIndex() & 2) != 0) {
            logger.trace("put locally");
            blockchain.setCurrentBlockIndex(currentBlockIndex);
        }

        if ((locationType.getIndex() & 1) != 0) {
            logger.debug("Saving network max height of {} in cache...", currentBlockIndex);
            blockchain.setNetworkHeight(currentBlockIndex);

            BlockHeightMessage message = new BlockHeightMessage(currentBlockIndex, blockchain.getShard().getIndex());
            AppServiceProvider.getP2PConnectionService().broadcastMessage(message, blockchain.getConnection());
        }

        logger.traceExit();
    }

    @Override
    public String getBlockHashFromIndex(BigInteger blockIndex, Blockchain blockchain) throws Exception {
        logger.traceEntry("params: {} {}", blockIndex, blockchain);
        String identifier = getBlockIndexIdentifier(blockIndex);
        String blockHash = AppServiceProvider.getBlockchainService().get(identifier, blockchain, BlockchainUnitType.BLOCK_INDEX);

        if (blockHash != null) {
            AppServiceProvider.getBlockchainService().putLocal(identifier, blockHash, blockchain, BlockchainUnitType.BLOCK_INDEX);
        }

        return logger.traceExit(blockHash);
    }


    @Override
    public void setBlockHashWithIndex(BigInteger blockIndex, String blockHash, Blockchain blockchain) throws Exception {
        logger.traceEntry("params: {} {} {}", blockIndex, blockHash, blockchain);
        String identifier = getBlockIndexIdentifier(blockIndex);
        AppServiceProvider.getBlockchainService().put(identifier, blockHash, blockchain, BlockchainUnitType.BLOCK_INDEX);
        logger.traceExit();
    }

    private String getBlockIndexIdentifier(BigInteger blockHeight) {
        logger.traceEntry("params: {}", blockHeight);
        return logger.traceExit(SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }

    @Override
    public ExecutionReport commitBlock(Block block, String blockHash, Blockchain blockchain) {
        logger.traceEntry("params: {} {} {}", block, blockHash, blockchain);
        ExecutionReport result = new ExecutionReport();

        try {
            fetchNetworkBlockIndex(blockchain);

            if (block.getNonce().compareTo(blockchain.getNetworkHeight()) <= 0) {
                result.combine(new ExecutionReport().ko("put block in blockchain failed! block nonce to be propossed is: " + block.getNonce() +
                        " and network block height is: " + blockchain.getNetworkHeight()));
                return logger.traceExit(result);
            }

            logger.trace("stored block index {}", block.getNonce());

            AppServiceProvider.getBlockchainService().putLocal(blockHash, block, blockchain, BlockchainUnitType.BLOCK);
            setBlockHashWithIndex(block.getNonce(), blockHash, blockchain);
            logger.trace("stored block {}", blockHash);

            // Update max index
            setCurrentBlockIndex(LocationType.BOTH, block.getNonce(), blockchain);
            logger.trace("done updating maxblock to {}", block.getNonce());

            // Update current block
            blockchain.setCurrentBlock(block);
            logger.trace("done updating current block");

            //Maintain processed transactions
            blockchain.getPool().addBlock(block);

            result.combine(new ExecutionReport().ok("Put block in blockchain : " + blockHash + " # " + block));

        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return logger.traceExit(result);
    }

    public ExecutionReport commitTransaction(Transaction transaction, String transactionHash, Blockchain blockchain) {
        logger.traceEntry("params: {} {} {}", transaction, transactionHash, blockchain);
        ExecutionReport result = new ExecutionReport();

        try {
            AppServiceProvider.getBlockchainService().putLocal(transactionHash, transaction, blockchain, BlockchainUnitType.TRANSACTION);
            result.combine(new ExecutionReport().ok("Put transaction in blockchain with hash: " + transactionHash));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return logger.traceExit(result);
    }


    @Override
    public ExecutionReport startFromGenesis(AppState state, AppContext context) {
        logger.traceEntry("params: {} {}", state, context);
        ExecutionReport result = new ExecutionReport().ok("Start from scratch...");


        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();

        // Generate genesis block
        String addressMint = context.getStrAddressMint();
        BigInteger valueMint = context.getValueMint();
        Fun.Tuple2<Block, Transaction> genesisData = AppServiceProvider.getAccountStateService().generateGenesisBlock(addressMint, valueMint, state, context);

        Block genesisBlock = genesisData.a;
        String genesisBlockHash = AppServiceProvider.getSerializationService().getHashString(genesisBlock);

        Transaction genesisTransaction = genesisData.b;
        String genesisTransactionHash = AppServiceProvider.getSerializationService().getHashString(genesisTransaction);

        logger.trace("Generated genesis transaction and block.");

        try {
            ExecutionReport reportBlock = commitBlock(genesisBlock, genesisBlockHash, blockchain);
            result.combine(reportBlock);

            ExecutionReport reportTransaction = commitTransaction(genesisTransaction, genesisTransactionHash, blockchain);
            result.combine(reportTransaction);

            ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(genesisBlock, accounts, blockchain, state.getStatisticsManager());
            result.combine(executionReport);

            if (result.isOk()) {
                logger.trace("Execution of genesis block was successful!");
                setCurrentBlockIndex(LocationType.BOTH, genesisBlock.getNonce(), blockchain);

                logger.info("\n" + state.print().render());
                //logger.info("\n" + AsciiTableUtil.listToTables(Arrays.asList(genesisTransaction)));
                AppStateUtil.printBlockAndAccounts(genesisBlock, accounts);
            }


        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
            return (result);
        }

        return logger.traceExit(result);
    }


    @Override
    public ExecutionReport restoreFromDisk(BigInteger currentBlockIndex, AppState state, AppContext context) {

        logger.traceEntry("params: {} {}", currentBlockIndex, context);

        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();
        NTPClient ntpClient = state.getNtpClient();


        ExecutionReport result = new ExecutionReport().ok("Start bootstrapping by loading from disk...");
        BigInteger idx = BigInteger.valueOf(-1);

        try {
            // get the last index from disk
            while (getBlockHashFromIndex(idx.add(BigInteger.ONE), blockchain) != null) {
                idx = idx.add(BigInteger.ONE);
            }
        } catch (Exception ex) {
            result.ko(ex);
            return (result);
        }

        if (idx.equals(BigInteger.valueOf(-1))) {
            logger.trace("no index stored on disk so need to create genesis");
            return startFromGenesis(state, context);
        }

        BigInteger genesisBlockIndex = BigInteger.valueOf(0);
        for (BigInteger index = genesisBlockIndex; index.compareTo(idx) <= 0; index = index.add(BigInteger.ONE)) {
            try {
                result.combine(new ExecutionReport().ok("Put block with height: " + index.toString(10) + "..."));

                String blockHash = getBlockHashFromIndex(index, blockchain);
                Block block = AppServiceProvider.getBlockchainService().getLocal(blockHash, blockchain, BlockchainUnitType.BLOCK);

                logger.trace("re-running block to update internal state...");
                ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(block, accounts, blockchain, state.getStatisticsManager());
                result.combine(executionReport);

                if (!result.isOk()) {
                    logger.trace("Rebuild FAILED at index {}!", index);
                    return logger.traceExit(result);
                }

                commitBlock(block, blockHash, blockchain);
                commitBlockTransactions(block, blockchain);
                // Update current block
                blockchain.setCurrentBlock(block);
                logger.trace("done updating current block");

            } catch (Exception ex) {
                result.ko(ex);
                logger.trace("Rebuild FAILED at index {}!", index);
                return logger.traceExit(result);
            }
        }

        logger.trace("Rebuild was SUCCESSFUL!");
        return logger.traceExit(result);
    }

    private void commitBlockTransactions(Block block, Blockchain blockchain) throws IOException, ClassNotFoundException {
        logger.traceEntry("params: {} {}", block, blockchain);

        List<String> hashes = BlockUtil.getTransactionsHashesAsString(block);
        for (String transactionHash : hashes) {
            Transaction transaction = AppServiceProvider.getBlockchainService().get(transactionHash, blockchain, BlockchainUnitType.TRANSACTION);
            if (transaction != null) {
                commitTransaction(transaction, transactionHash, blockchain);
            }
        }

        logger.trace("Done, {} transactions processed!", BlockUtil.getTransactionsCount(block));
        logger.traceExit();
    }


    @Override
    public ExecutionReport synchronize(BigInteger localBlockIndex, BigInteger remoteBlockIndex, AppState state) {
        logger.traceEntry("params: {} {} {} {}", localBlockIndex, remoteBlockIndex, state);

        Accounts accounts = state.getAccounts();
        Blockchain blockchain = state.getBlockchain();

        ExecutionReport result = new ExecutionReport().ok("Bootstrapping... [local height: " + localBlockIndex + " > network height: " + remoteBlockIndex + "...");

        logger.trace("re-running stored blocks to update internal state...");
        ExecutionService executionService = AppServiceProvider.getExecutionService();

        for (BigInteger blockIndex = localBlockIndex.add(BigInteger.ONE); blockIndex.compareTo(remoteBlockIndex) <= 0; blockIndex = blockIndex.add(BigInteger.ONE)) {
            try {

                String blockHash = getBlockHashFromIndex(blockIndex, blockchain);
                if (blockHash == null) {
                    result.ko("Can not synchronize! Could not find block with nonce = " + blockIndex.toString(10) + " on LOCAL!");
                    logger.trace("Synchronized FAILED at index {}!", blockIndex);
                    return logger.traceExit(result);
                }

                Block block = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK);
                if (block == null) {
                    result.ko("Can not find block hash " + blockHash + " on LOCAL!");
                    logger.trace("Synchronized FAILED at index {}!", blockIndex);
                    return logger.traceExit(result);
                }


                ExecutionReport executionReport = executionService.processBlock(block, accounts, blockchain, state.getStatisticsManager());
                result.combine(executionReport);

                if (!result.isOk()) {
                    logger.trace("Synchronized FAILED at index {}!", blockIndex);
                    return logger.traceExit(result);
                }

                AppServiceProvider.getBlockchainService().putLocal(blockHash, block, blockchain, BlockchainUnitType.BLOCK);

                //AppBlockManager.instance().removeAlreadyProcessedTransactionsFromPool(state, block);

                blockchain.getPool().addBlock(block);
                result.ok("Added block in blockchain : " + blockHash + " # " + block);

                logger.info("New block synchronized with hash {}", blockHash);
                logger.info("\r\n" + state.print().render());
                //logger.info("\n" + AsciiTableUtil.listToTables(transactions));
                AppStateUtil.printBlockAndAccounts(block, accounts);

                // Update current block
                blockchain.setCurrentBlockIndex(blockIndex);
                blockchain.setCurrentBlock(block);

//                if (state.getStatisticsManager() != null){
//                    state.getStatisticsManager().addStatistic(
//                            new Statistic(block.getListTXHashes().size(), block.getTimestamp()));
//                }

                logger.trace("done updating current block");

            } catch (Exception ex) {
                result.ko(ex);
                logger.trace("Synchronized FAILED at index {}!", blockIndex);
                return logger.traceExit(result);
            }
        }

        logger.trace("Synchronized was SUCCESSFUL!");
        return logger.traceExit(result);
    }


    @Override
    public void setBlockHeightFromNetwork(BigInteger blockHeight, Blockchain blockchain) {
        blockchain.setNetworkHeight(blockHeight);
    }

    @Override
    public SyncState getSyncState(Blockchain blockchain) {
        SyncState syncState = new SyncState();

        BigInteger localHeight = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.LOCAL, blockchain);
        BigInteger networkHeight = AppServiceProvider.getBootstrapService().getCurrentBlockIndex(LocationType.NETWORK, blockchain);

        syncState.setValid(true);

        syncState.setLocalBlockIndex(localHeight);
        syncState.setRemoteBlockIndex(networkHeight);

        boolean isBlocAvailable = syncState.getRemoteBlockIndex().compareTo(BigInteger.ZERO) >= 0;
        boolean isNewBlockRemote = syncState.getRemoteBlockIndex().compareTo(syncState.getLocalBlockIndex()) > 0;
        syncState.setSyncRequired(isBlocAvailable && isNewBlockRemote);

        return (syncState);
    }
}
