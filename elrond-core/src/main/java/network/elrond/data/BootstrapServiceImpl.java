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
import network.elrond.p2p.P2PConnection;
import network.elrond.p2p.P2PObjectService;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrapServiceImpl implements BootstrapService {
    private Logger logger = LoggerFactory.getLogger(BootstrapServiceImpl.class);

    //returns max block height from location
    public BigInteger getMaxBlockSize(LocationType locationType, Blockchain structure) throws Exception{
        if (locationType == LocationType.BOTH){
            throw new Exception("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL){
            String maxHeight = AppServiceProvider.getAppPersistanceService().get(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure, BlockchainUnitType.SETTINGS);

            if (maxHeight == null) {
                logger.info("getMaxBlockSize LOCAL: -1");
                return(Util.BIG_INT_MIN_ONE);
            }

            logger.info("getMaxBlockSize LOCAL: " + maxHeight);
            return (new BigInteger(maxHeight));
        }

        if (locationType == LocationType.NETWORK){
            BigInteger maxHeight = AppServiceProvider.getP2PObjectService().getJsonDecoded(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure.getConnection(), BigInteger.class);

            if (maxHeight == null){
                logger.info("getMaxBlockSize NETWORK: -1");
                return(Util.BIG_INT_MIN_ONE);
            }

            logger.info("getMaxBlockSize NETWORK: " + maxHeight);
            return (maxHeight);
        }

        throw new Exception("Unimplemented location type: " + locationType.toString() + "!");
    }

    //sets max block height in location
    public void setMaxBlockSize(LocationType locationType, BigInteger height, Blockchain structure) throws Exception{
        if ((locationType.getIndex() & 2) != 0){
            //locally
            String json = AppServiceProvider.getSerializationService().encodeJSON(height);
            AppServiceProvider.getAppPersistanceService().put(SettingsType.MAX_BLOCK_HEIGHT.toString(), json, structure, BlockchainUnitType.SETTINGS);
            logger.info("setMaxBlockSize LOCAL (" +height.toString(10) + ")");
        }

        if ((locationType.getIndex() & 1) != 0){
            //network
            BigInteger maxHeight = getMaxBlockSize(LocationType.NETWORK, structure);

            if ((maxHeight == null) || (height.compareTo(maxHeight)) > 0) {
                logger.info("setMaxBlockSize NETWORK (" +height.toString(10) + ")");
                AppServiceProvider.getP2PObjectService().putJsonEncoded(height, SettingsType.MAX_BLOCK_HEIGHT.toString(), structure.getConnection());
            }
        }
    }

    //gets the hash for the block height from location
    public String getBlockHashFromHeight(LocationType locationType, BigInteger blockHeight, Blockchain structure) throws Exception{
        if (locationType == LocationType.BOTH){
            throw new Exception("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL){
            String data = (String) AppServiceProvider.getAppPersistanceService().get(getHeightBlockHashString(blockHeight), structure, BlockchainUnitType.BLOCK_INDEX);
            logger.info("getBlockHashFromHeight LOCAL: " + getDataAsString(data));
            return (data);
        }

        if (locationType == LocationType.NETWORK){
            String data = AppServiceProvider.getP2PObjectService().getJsonDecoded(getHeightBlockHashString(blockHeight), structure.getConnection(), String.class);
            logger.info("getBlockHashFromHeight NETWORK: " + getDataAsString(data));
            return (data);
        }

        throw new Exception("Unimplemented location type: " + locationType.toString() + "!");
    }

    //sets the hash for a block height in location
    public void setBlockHashWithHeight(LocationType locationType, BigInteger blockHeight, String hash, Blockchain structure) throws Exception{
        if ((locationType.getIndex() & 2) != 0){
            //locally
            AppServiceProvider.getAppPersistanceService().put(getHeightBlockHashString(blockHeight), hash, structure, BlockchainUnitType.BLOCK_INDEX);
            logger.info("setBlockHashWithHeight LOCAL: ( blockHeight: " + getDataAsString(blockHeight) + ", hash: " + getDataAsString(hash) + ")");
        }

        if ((locationType.getIndex() & 1) != 0){
            //network
            AppServiceProvider.getP2PObjectService().putJsonEncoded(hash, getHeightBlockHashString(blockHeight), structure.getConnection());
            logger.info("setBlockHashWithHeight NETWORK: ( blockHeight: " + getDataAsString(blockHeight) + ", hash: " + getDataAsString(hash) + ")");
        }
    }

    public ExecutionReport startFromScratch(Application application) {
        SerializationService serializationService = AppServiceProvider.getSerializationService();

        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        AppContext context = application.getContext();

        AccountsContext accountsContext = new AccountsContext();


        logger.info("START start from scratch!");
        //result.combine(new ExecutionReport().ok("Start from scratch..."));
        Fun.Tuple2<Block, Transaction> genesisData = AppServiceProvider.getAccountStateService().generateGenesisBlock(context.getStrAddressMint(), context.getValueMint(),
                accountsContext);
        String strHashGB = new String(Base64.encode(serializationService.getHash(genesisData.a)));
        String strHashTx = new String(Base64.encode(serializationService.getHash(genesisData.b)));

        logger.info("Got genesis tx: " + getSerialized(genesisData.b) + " hash: " + strHashTx);
        logger.info("Got genesis blk: " + getSerialized(genesisData.a) + " hash: " + strHashGB);

        //put locally and broadcasting it
        try {
            result.combine(putBlockInBlockchain(genesisData.a, strHashGB, state));
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
            return(result);
        }

        //should broadcast generated block
        result.combine(rebuildFromDisk(application, BigInteger.ZERO));

        logger.info("END of start from scratch called!");
        return (result);
    }

    public ExecutionReport bootstrap(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw) {
        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        logger.info("START start bootstrap!");

        logger.info("Bootstrapping from local height: " + getDataAsString(maxBlkHeightLocal) + " > network height: " + getDataAsString(maxBlkHeightNetw));
//        result.combine(new ExecutionReport().ok("Bootstrapping... [local height: " + maxBlkHeightLocal.toString(10) + " > network height: " +
//                maxBlkHeightNetw.toString(10) + "..."));
        //state.setBootstrapping(true);

        String strHashBlock;
        Block blk;

        //re-run stored blocks to update internal state
        for (BigInteger counter = BigInteger.ZERO; counter.compareTo(maxBlkHeightLocal) <= 0; counter = counter.add(BigInteger.ONE)){
            logger.info("Re-run stored blocks, counter: " + getDataAsString(counter));
            try{
                strHashBlock = getBlockHashFromHeight(LocationType.LOCAL, counter, state.getBlockchain());

                if (strHashBlock == null) {
                    result.ko("Can not bootstrap! Could not find block with nonce = " + counter.toString(10) + " on LOCAL!");
                    logger.info("END start bootstrap!");
                    return(result);
                }

                blk = AppServiceProvider.getAppPersistanceService().get(strHashBlock, state.getBlockchain(), BlockchainUnitType.BLOCK);

                if (blk == null) {
                    result.ko("Can not find block hash " + strHashBlock + " on LOCAL!");
                    break;
                }

                logger.info("Got block " + getSerialized(blk) + " hash " + getDataAsString(strHashBlock) + ", trying to execute it!");

                ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

                result.combine(exExecuteBlock);

                if (!result.isOk()){
                    logger.info("END start bootstrap!");
                    return (result);
                }
            } catch (Exception ex){
                result.ko(ex);
                logger.info("END start bootstrap!");
                return (result);
            }
        }

        for (BigInteger counter = maxBlkHeightLocal.add(BigInteger.ONE); counter.compareTo(maxBlkHeightNetw) <= 0; counter = counter.add(BigInteger.ONE)) {
            //get the hash of the block from network
            logger.info("Fetch new blocks, counter: " + getDataAsString(counter));

            try {
                strHashBlock = getBlockHashFromHeight(LocationType.NETWORK, counter, state.getBlockchain());
            } catch (Exception ex) {
                result.ko(ex);
                logger.info("END start bootstrap!");
                return (result);
            }

            if (strHashBlock == null) {
                result.ko("Can not bootstrap! Could not find block with nonce = " + counter.toString(10) + " on DHT!");
                logger.info("END start bootstrap!");
                return(result);
            }

            try {
                blk = AppServiceProvider.getP2PObjectService().getJsonDecoded(strHashBlock, state.getConnection(), Block.class);
            } catch (Exception ex) {
                result.ko(ex);
                logger.info("END start bootstrap!");
                return (result);
            }

            if (blk == null) {
                result.ko("Can not find block hash " + strHashBlock + " on DHT!");
                logger.info("END start bootstrap!");
                break;
            }

            logger.info("Got block " + getSerialized(blk) + " hash " + getDataAsString(strHashBlock) + ", trying to execute it!");
            ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

            result.combine(exExecuteBlock);

            if (!result.isOk()){
                logger.info("END start bootstrap!");
                return (result);
            }

            //block successfully processed, add it to blockchain structure
            result.combine(putBlockInBlockchain(blk, strHashBlock, state));

            if (!result.isOk()){
                logger.info("END start bootstrap!");
                return (result);
            }
        }

        logger.info("END start bootstrap!");
        return(result);
    }

    public ExecutionReport rebuildFromDisk(Application application, BigInteger maxBlkHeightLocal) {
        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        logger.info("START rebuild from disk!");

        //result.combine(new ExecutionReport().ok("Start bootstrapping by loading from disk..."));
        //state.setBootstrapping(true);
        //start pushing blocks and transactions
        //block with nonce = 0 is genesis and should be broadcast
        for (BigInteger counter = BigInteger.valueOf(0); counter.compareTo(maxBlkHeightLocal) <= 0; counter = counter.add(BigInteger.ONE)) {
            logger.info("Put block with height: " + getDataAsString(counter) + "...");
            try {
                //result.combine(new ExecutionReport().ok("Put block with height: " + counter.toString(10) + "..."));
                //put block
                String strHashBlk = getBlockHashFromHeight(LocationType.LOCAL, counter, state.getBlockchain());
                Block blk = blockchainService.get(strHashBlk, state.getBlockchain(), BlockchainUnitType.BLOCK);
                AppServiceProvider.getP2PObjectService().putJsonEncoded(blk, strHashBlk, state.getConnection());

                logger.info("Put block " + getSerialized(blk) + " hash: " + strHashBlk);

                //re-run block to update internal state
                ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

                result.combine(exExecuteBlock);

                if (!result.isOk()){
                    logger.info("END rebuild from disk!");
                    return (result);
                }

                //put pair block_height - block hash
                setBlockHashWithHeight(LocationType.NETWORK, counter, strHashBlk, state.getBlockchain());

                //put transactions
                for (int j = 0; j < blk.getListTXHashes().size(); j++) {
                    String strHashTx = new String(Base64.encode(blk.getListTXHashes().get(j)));

                    Transaction tx = blockchainService.get(strHashTx, state.getBlockchain(), BlockchainUnitType.TRANSACTION);

                    AppServiceProvider.getP2PObjectService().putJsonEncoded(tx, strHashTx, state.getConnection());

                    logger.info("Put tx " + getSerialized(tx) + " hash: " + strHashTx);
                }

                //put settings max_block_height
                setMaxBlockSize(LocationType.NETWORK, counter, state.getBlockchain());
            } catch (Exception ex) {
                result.ko(ex);
                logger.info("END rebuild from disk!");
                return (result);
            }
        }

        logger.info("END rebuild from disk!");
        return(result);
    }

    public ExecutionReport rebuildFromDiskDeltaNoExec(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw) {
        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();

        logger.info("START rebuild from disk delta without exec!");

        //result.combine(new ExecutionReport().ok("Start bootstrapping by loading from disk..."));
        //state.setBootstrapping(true);
        //start pushing blocks and transactions
        //block with nonce = 0 is genesis and should be broadcast
        for (BigInteger counter = maxBlkHeightNetw.add(BigInteger.ONE); counter.compareTo(maxBlkHeightLocal) <= 0; counter = counter.add(BigInteger.ONE)) {
            logger.info("Put block with height: " + getDataAsString(counter) + "...");
            try {
                //result.combine(new ExecutionReport().ok("Put block with height: " + counter.toString(10) + "..."));
                //put block
                String strHashBlk = getBlockHashFromHeight(LocationType.LOCAL, counter, state.getBlockchain());
                Block blk = blockchainService.get(strHashBlk, state.getBlockchain(), BlockchainUnitType.BLOCK);
                AppServiceProvider.getP2PObjectService().putJsonEncoded(blk, strHashBlk, state.getConnection());

                logger.info("Put block " + getSerialized(blk) + " hash: " + strHashBlk);

                //put pair block_height - block hash
                setBlockHashWithHeight(LocationType.NETWORK, counter, strHashBlk, state.getBlockchain());

                //put transactions
                for (int j = 0; j < blk.getListTXHashes().size(); j++) {
                    String strHashTx = new String(Base64.encode(blk.getListTXHashes().get(j)));

                    Transaction tx = blockchainService.get(strHashTx, state.getBlockchain(), BlockchainUnitType.TRANSACTION);

                    AppServiceProvider.getP2PObjectService().putJsonEncoded(tx, strHashTx, state.getConnection());

                    logger.info("Put tx " + getSerialized(tx) + " hash: " + strHashTx);
                }

                //put settings max_block_height
                setMaxBlockSize(LocationType.NETWORK, counter, state.getBlockchain());
            } catch (Exception ex) {
                result.ko(ex);
                logger.info("END rebuild from disk delta without exec!");
                return (result);
            }
        }

        logger.info("END rebuild from disk delta without exec!");
        return(result);
    }

    public ExecutionReport putBlockInBlockchain(Block blk, String blockHash, AppState state) {
        ExecutionReport result = new ExecutionReport();

        logger.info("START put block on blockchain!");

        BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();

        try {
            appPersistanceService.put(blockHash, blk, state.getBlockchain(), BlockchainUnitType.BLOCK);
            setBlockHashWithHeight(LocationType.LOCAL, blk.getNonce(), blockHash, state.getBlockchain());
            appPersistanceService.put(blk.getNonce(), blockHash, state.getBlockchain(), BlockchainUnitType.BLOCK_INDEX);
            setMaxBlockSize(LocationType.LOCAL, blk.getNonce(), state.getBlockchain());
            state.setCurrentBlock(blk);
            //result.combine(new ExecutionReport().ok("Put block in blockchain with hash: " + blockHash));
            logger.info("Put block " + getSerialized(blk) + " hash: " + blockHash);
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        logger.info("END put block on blockchain!");
        return (result);
    }

    public ExecutionReport putTransactionInBlockchain(Transaction transaction, String transactionHash, AppState state) {
        ExecutionReport result = new ExecutionReport();

        BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();

        logger.info("START put tx on blockchain!");

        try {
            appPersistanceService.put(transactionHash, transaction, state.getBlockchain(), BlockchainUnitType.TRANSACTION);
            //result.combine(new ExecutionReport().ok("Put transaction in blockchain with hash: " + transactionHash));
            logger.info("Put tx " + getSerialized(transaction) + " hash: " + transactionHash);
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }

    //generate block height name to search the hash
    public String getHeightBlockHashString(BigInteger blockHeight){
        return(SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }

    private String getDataAsString(Object obj){
        if (obj == null) {
            return("NULL");
        } else {
            return(obj.toString());
        }
    }

    private String getSerialized(Object data){
        return (AppServiceProvider.getSerializationService().encodeJSON(data));

    }
}
