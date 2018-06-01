package network.elrond.data;

import network.elrond.Application;
import network.elrond.account.AccountsContext;
import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.p2p.P2PObjectService;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.mapdb.Fun;

import java.math.BigInteger;

public class BootstrapServiceImpl implements BootstrapService {

    private BlockchainService blockchainService = AppServiceProvider.getBlockchainService();
    private P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();

    //returns max block height from location
    public BigInteger getMaxBlockSize(LocationType locationType, Blockchain structure) throws Exception {
        if (locationType == LocationType.BOTH) {
            throw new Exception("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL) {
            return structure.getCurrentBlockIndex();
        }

        if (locationType == LocationType.NETWORK) {

            BigInteger maxHeight = p2PObjectService.getJsonDecoded(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure.getConnection(), BigInteger.class);
            if (maxHeight == null) {
                return (Util.BIG_INT_MIN_ONE);
            }

            return (maxHeight);
        }

        throw new Exception("Unimplemented location type: " + locationType.toString() + "!");
    }

    //sets max block height in location
    public void setMaxBlockSize(LocationType locationType, BigInteger height, Blockchain structure) throws Exception {
        if ((locationType.getIndex() & 2) != 0) {
            //locally
            structure.setCurrentBlockIndex(height);
        }

        if ((locationType.getIndex() & 1) != 0) {
            //network
            BigInteger maxHeight = getMaxBlockSize(LocationType.NETWORK, structure);
            BigInteger max = height.max(maxHeight);
            p2PObjectService.putJsonEncoded(max, SettingsType.MAX_BLOCK_HEIGHT.toString(), structure.getConnection());

        }
    }

    //gets the hash for the block height from location
    public String getBlockHashFromHeight(LocationType locationType, BigInteger blockHeight, Blockchain structure) throws Exception {
        if (locationType == LocationType.BOTH) {
            throw new Exception("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL) {
            return ((String) blockchainService.get(getHeightBlockHashString(blockHeight), structure, BlockchainUnitType.BLOCK_INDEX));
        }

        if (locationType == LocationType.NETWORK) {
            return (p2PObjectService.getJsonDecoded(getHeightBlockHashString(blockHeight), structure.getConnection(), String.class));
        }

        throw new Exception("Unimplemented location type: " + locationType.toString() + "!");
    }

    //sets the hash for a block height in location
    public void setBlockHashWithHeight(LocationType locationType, BigInteger blockHeight, String hash, Blockchain structure) throws Exception {
        if ((locationType.getIndex() & 2) != 0) {
            //locally
            blockchainService.put(getHeightBlockHashString(blockHeight), hash, structure, BlockchainUnitType.BLOCK_INDEX);
        }

        if ((locationType.getIndex() & 1) != 0) {
            //network
            p2PObjectService.putJsonEncoded(hash, getHeightBlockHashString(blockHeight), structure.getConnection());
        }
    }

    public ExecutionReport startFromScratch(Application application) {
        SerializationService serializationService = AppServiceProvider.getSerializationService();

        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        AppContext context = application.getContext();

        AccountsContext accountsContext = new AccountsContext();


        result.combine(new ExecutionReport().ok("Start from scratch..."));
        Fun.Tuple2<Block, Transaction> genesisData = AppServiceProvider.getAccountStateService().generateGenesisBlock(context.getStrAddressMint(), context.getValueMint(),
                accountsContext, context.getPrivateKey());
        String strHashGB = new String(Base64.encode(serializationService.getHash(genesisData.a)));
        String strHashTx = new String(Base64.encode(serializationService.getHash(genesisData.b)));

        //put locally and broadcasting it
        try {
            result.combine(putBlockInBlockchain(genesisData.a, strHashGB, state.getBlockchain()));
            result.combine(putTransactionInBlockchain(genesisData.b, strHashTx, state));
            setMaxBlockSize(LocationType.BOTH, BigInteger.ZERO, state.getBlockchain());

            //block execution is handled inside rebuildFromDisk
//            ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(genesisData.a, state.getAccounts(), state.getBlockchain());
//            result.combine(exExecuteBlock);
//
//            if (result.isOk()){
//                result.combine(new ExecutionReport().ok("Start from scratch...OK!"));
//            }

        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
            return (result);
        }

        //should broadcast generated block
        result.combine(rebuildFromDisk(application, BigInteger.ZERO));

        return (result);
    }

    @Override
    public ExecutionReport synchronize(Application application, BigInteger localBlockIndex, BigInteger remoteBlockIndex) {

        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        result.combine(
                new ExecutionReport().ok("Bootstrapping... [local height: " + localBlockIndex.toString(10) + " > network height: " +
                        remoteBlockIndex.toString(10) + "..."));


        //re-run stored blocks to update internal state
        ExecutionService executionService = AppServiceProvider.getExecutionService();

        for (BigInteger blockIndex = BigInteger.ZERO.max(localBlockIndex); blockIndex.compareTo(remoteBlockIndex) <= 0; blockIndex = blockIndex.add(BigInteger.ONE)) {
            try {

                String blockHash = getBlockHashFromHeight(LocationType.NETWORK, blockIndex, state.getBlockchain());
                if (blockHash == null) {
                    result.ko("Can not synchronize! Could not find block with nonce = " + blockIndex.toString(10) + " on LOCAL!");
                    return (result);
                }

                Block block = AppServiceProvider.getBlockchainService().get(blockHash, state.getBlockchain(), BlockchainUnitType.BLOCK);
                if (block == null) {
                    result.ko("Can not find block hash " + blockHash + " on LOCAL!");
                    break;
                }

                ExecutionReport executionReport = executionService.processBlock(block, state.getAccounts(), state.getBlockchain());
                result.combine(executionReport);

                if (!result.isOk()) {
                    return (result);
                }

                state.getBlockchain().setCurrentBlockIndex(blockIndex);


            } catch (Exception ex) {
                result.ko(ex);
                return (result);
            }
        }

        return (result);
    }

    public ExecutionReport rebuildFromDisk(Application application, BigInteger maxBlkHeightLocal) {
        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();


        result.combine(new ExecutionReport().ok("Start bootstrapping by loading from disk..."));
        //state.setBootstrapping(true);
        //start pushing blocks and transactions
        //block with nonce = 0 is genesis and should be broadcast
        for (BigInteger counter = BigInteger.valueOf(0); counter.compareTo(maxBlkHeightLocal) <= 0; counter = counter.add(BigInteger.ONE)) {
            try {
                result.combine(new ExecutionReport().ok("Put block with height: " + counter.toString(10) + "..."));
                //put block
                String strHashBlk = getBlockHashFromHeight(LocationType.LOCAL, counter, state.getBlockchain());
                Block blk = blockchainService.get(strHashBlk, state.getBlockchain(), BlockchainUnitType.BLOCK);
                p2PObjectService.putJsonEncoded(blk, strHashBlk, state.getConnection());

                //re-run block to update internal state
                ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

                result.combine(exExecuteBlock);

                if (!result.isOk()) {
                    return (result);
                }

                //put pair block_height - block hash
                setBlockHashWithHeight(LocationType.NETWORK, counter, strHashBlk, state.getBlockchain());

                //put transactions
                for (int j = 0; j < blk.getListTXHashes().size(); j++) {
                    String strHashTx = new String(Base64.encode(blk.getListTXHashes().get(j)));

                    Transaction tx = blockchainService.get(strHashTx, state.getBlockchain(), BlockchainUnitType.TRANSACTION);

                    p2PObjectService.putJsonEncoded(tx, strHashTx, state.getConnection());
                }

                //put settings max_block_height
                setMaxBlockSize(LocationType.NETWORK, counter, state.getBlockchain());
            } catch (Exception ex) {
                result.ko(ex);
                return (result);
            }
        }

        return (result);
    }


    @Override
    public ExecutionReport putBlockInBlockchain(Block blk, String blockHash, Blockchain blockchain) {
        ExecutionReport result = new ExecutionReport();


        try {


            AppServiceProvider.getBlockchainService().put(blockHash, blk, blockchain, BlockchainUnitType.BLOCK);
            setBlockHashWithHeight(LocationType.BOTH, blk.getNonce(), blockHash, blockchain);
            // Put index <=> hash mapping
            AppServiceProvider.getBlockchainService().put(blk.getNonce(), blockHash, blockchain, BlockchainUnitType.BLOCK_INDEX);

            // Update max index
            setMaxBlockSize(LocationType.BOTH, blk.getNonce(), blockchain);

            // Update current block
            blockchain.setCurrentBlock(blk);

            result.combine(new ExecutionReport().ok("Put block in blockchain : " + blockHash + " # " + blk));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }

    public ExecutionReport putTransactionInBlockchain(Transaction transaction, String transactionHash, AppState state) {
        ExecutionReport result = new ExecutionReport();

        //BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();

        try {
            AppServiceProvider.getBlockchainService().put(transactionHash, transaction, state.getBlockchain(), BlockchainUnitType.TRANSACTION);
            result.combine(new ExecutionReport().ok("Put transaction in blockchain with hash: " + transactionHash));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }

    //generate block height name to search the hash
    public String getHeightBlockHashString(BigInteger blockHeight) {
        return (SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }
}
