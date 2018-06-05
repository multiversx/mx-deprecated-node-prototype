package network.elrond.data;

import network.elrond.account.Accounts;
import network.elrond.account.AccountsContext;
import network.elrond.application.AppContext;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.crypto.PrivateKey;
import network.elrond.service.AppServiceProvider;
import org.spongycastle.util.encoders.Base64;
import org.mapdb.Fun;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrapServiceImpl implements BootstrapService {


    @Override
    public BigInteger getCurrentBlockIndex(LocationType locationType, Blockchain blockchain) throws IOException, ClassNotFoundException {

        if (locationType == LocationType.BOTH) {
            throw new RuntimeException("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL) {
            BigInteger currentBlockIndex = blockchain.getCurrentBlockIndex();
            BigInteger networkBlockIndex = getNetworkBlockIndex(blockchain);
            return currentBlockIndex.min(networkBlockIndex);
        }

        if (locationType == LocationType.NETWORK) {
            return getNetworkBlockIndex(blockchain);
        }

        throw new RuntimeException("Unimplemented location type: " + locationType.toString() + "!");
    }

    private BigInteger getNetworkBlockIndex(Blockchain blockchain) throws java.io.IOException, ClassNotFoundException {

        BigInteger maxHeight = AppServiceProvider.getP2PObjectService().getJsonDecoded(
                SettingsType.MAX_BLOCK_HEIGHT.toString(),
                blockchain.getConnection(), BigInteger.class);

        if (maxHeight == null) {
            return (Util.BIG_INT_MIN_ONE);
        }

        return (maxHeight);
    }

    @Override
    public void setCurrentBlockIndex(LocationType locationType, BigInteger currentBlockIndex, Blockchain blockchain) throws Exception {
        if ((locationType.getIndex() & 2) != 0) {
            //locally
            blockchain.setCurrentBlockIndex(currentBlockIndex);
        }

        if ((locationType.getIndex() & 1) != 0) {
            //network
            BigInteger currentBlockIndexOnNetwork = getCurrentBlockIndex(LocationType.NETWORK, blockchain);
            BigInteger max = currentBlockIndex.max(currentBlockIndexOnNetwork);
            AppServiceProvider.getP2PObjectService().putJsonEncoded(max, SettingsType.MAX_BLOCK_HEIGHT.toString(), blockchain.getConnection());

        }
    }

    @Override
    public String getBlockHashFromIndex(BigInteger blockIndex, Blockchain blockchain) throws Exception {
        String identifier = getBlockIndexIdentifier(blockIndex);
        return AppServiceProvider.getBlockchainService().get(identifier, blockchain, BlockchainUnitType.BLOCK_INDEX);
    }


    @Override
    public void setBlockHashWithIndex(BigInteger blockIndex, String blockHash, Blockchain blockchain) throws Exception {
        String identifier = getBlockIndexIdentifier(blockIndex);
        AppServiceProvider.getBlockchainService().put(identifier, blockHash, blockchain, BlockchainUnitType.BLOCK_INDEX);
    }

    private String getBlockIndexIdentifier(BigInteger blockHeight) {
        return (SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }

    @Override
    public ExecutionReport commitBlock(Block block, String blockHash, Blockchain blockchain) {

        ExecutionReport result = new ExecutionReport();

        try {

            AppServiceProvider.getBlockchainService().put(blockHash, block, blockchain, BlockchainUnitType.BLOCK);
            setBlockHashWithIndex(block.getNonce(), blockHash, blockchain);
            // Put index <=> hash mapping
            AppServiceProvider.getBlockchainService().put(block.getNonce(), blockHash, blockchain, BlockchainUnitType.BLOCK_INDEX);

            // Update max index
            setCurrentBlockIndex(LocationType.BOTH, block.getNonce(), blockchain);

            // Update current block
            blockchain.setCurrentBlock(block);

            result.combine(new ExecutionReport().ok("Put block in blockchain : " + blockHash + " # " + block));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }

    public ExecutionReport commitTransaction(Transaction transaction, String transactionHash, Blockchain blockchain) {

        ExecutionReport result = new ExecutionReport();

        try {
            AppServiceProvider.getBlockchainService().put(transactionHash, transaction, blockchain, BlockchainUnitType.TRANSACTION);
            result.combine(new ExecutionReport().ok("Put transaction in blockchain with hash: " + transactionHash));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }


    @Override
    public ExecutionReport startFromGenesis(Accounts accounts, Blockchain blockchain, AppContext context) {

        ExecutionReport result = new ExecutionReport().ok("Start from scratch...");

        // Generate genesis block
        String addressMint = context.getStrAddressMint();
        BigInteger valueMint = context.getValueMint();
        PrivateKey privateKey = context.getPrivateKey();

        AccountsContext accountsContext = new AccountsContext();
        Fun.Tuple2<Block, Transaction> genesisData = AppServiceProvider.getAccountStateService()
                .generateGenesisBlock(addressMint, valueMint, accountsContext, privateKey);

        Block genesisBlock = genesisData.a;
        String genesisBlockHash = AppServiceProvider.getSerializationService().getHashString(genesisBlock);

        Transaction genesisTransaction = genesisData.b;
        String genesisTransactionHash = AppServiceProvider.getSerializationService().getHashString(genesisTransaction);


        try {

            ExecutionReport reportBlock = commitBlock(genesisBlock, genesisBlockHash, blockchain);
            result.combine(reportBlock);

            ExecutionReport reportTransaction = commitTransaction(genesisTransaction, genesisTransactionHash, blockchain);
            result.combine(reportTransaction);

            ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(genesisBlock, accounts, blockchain);
            result.combine(executionReport);

            if (result.isOk()) {
                setCurrentBlockIndex(LocationType.BOTH, genesisBlock.getNonce(), blockchain);
            }


        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
            return (result);
        }

        return (result);
    }


    @Override
    public ExecutionReport restoreFromDisk(BigInteger currentBlockIndex, Accounts accounts, Blockchain blockchain) {

        ExecutionReport result = new ExecutionReport().ok("Start bootstrapping by loading from disk...");


        BigInteger genesisBlockIndex = BigInteger.valueOf(0);
        for (BigInteger index = genesisBlockIndex; index.compareTo(currentBlockIndex) <= 0; index = index.add(BigInteger.ONE)) {
            try {
                result.combine(new ExecutionReport().ok("Put block with height: " + index.toString(10) + "..."));


                String blockHash = getBlockHashFromIndex(index, blockchain);
                Block block = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK);
                //re-run block to update internal state
                ExecutionReport executionReport = AppServiceProvider.getExecutionService().processBlock(block, accounts, blockchain);
                result.combine(executionReport);

                if (!result.isOk()) {
                    return result;
                }

                commitBlock(block, blockHash, blockchain);
                commitBlockTransactions(block, blockchain);


            } catch (Exception ex) {
                result.ko(ex);
                return (result);
            }
        }

        return (result);
    }

    private void commitBlockTransactions(Block block, Blockchain blockchain) throws IOException, ClassNotFoundException {
        for (byte[] hash : block.getListTXHashes()) {
            String transactionHash = new String(Base64.encode(hash));
            Transaction transaction = AppServiceProvider.getBlockchainService().get(transactionHash, blockchain, BlockchainUnitType.TRANSACTION);
            commitTransaction(transaction, transactionHash, blockchain);
        }
    }


    @Override
    public ExecutionReport synchronize(BigInteger localBlockIndex, BigInteger remoteBlockIndex, Blockchain blockchain, Accounts accounts) {

        ExecutionReport result = new ExecutionReport().ok("Bootstrapping... [local height: " + localBlockIndex + " > network height: " + remoteBlockIndex + "...");


        //re-run stored blocks to update internal state
        ExecutionService executionService = AppServiceProvider.getExecutionService();

        for (BigInteger blockIndex = localBlockIndex.add(BigInteger.ONE); blockIndex.compareTo(remoteBlockIndex) <= 0; blockIndex = blockIndex.add(BigInteger.ONE)) {
            try {

                String blockHash = getBlockHashFromIndex(blockIndex, blockchain);
                if (blockHash == null) {
                    result.ko("Can not synchronize! Could not find block with nonce = " + blockIndex.toString(10) + " on LOCAL!");
                    return (result);
                }

                Block block = AppServiceProvider.getBlockchainService().get(blockHash, blockchain, BlockchainUnitType.BLOCK);
                if (block == null) {
                    result.ko("Can not find block hash " + blockHash + " on LOCAL!");
                    break;
                }


                ExecutionReport executionReport = executionService.processBlock(block, accounts, blockchain);
                result.combine(executionReport);

                if (!result.isOk()) {
                    return (result);
                }

                blockchain.setCurrentBlockIndex(blockIndex);


            } catch (Exception ex) {
                result.ko(ex);
                return (result);
            }
        }

        return (result);
    }


}
