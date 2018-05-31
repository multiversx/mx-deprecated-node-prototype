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

    private BlockchainService apsServ = AppServiceProvider.getAppPersistanceService();
    private SerializationService serServ = AppServiceProvider.getSerializationService();
    private P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();

    //returns max block height from location
    public BigInteger getMaxBlockSize(LocationType locationType, Blockchain structure) throws Exception{
        if (locationType == LocationType.BOTH){
            throw new Exception("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL){
            String maxHeight = apsServ.get(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure, BlockchainUnitType.SETTINGS);

            if (maxHeight == null) {
                return(Util.BIG_INT_MIN_ONE);
            }

            return (new BigInteger(maxHeight));
        }

        if (locationType == LocationType.NETWORK){
            BigInteger maxHeight = p2PObjectService.getJsonDecoded(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure.getConnection(), BigInteger.class);

            if (maxHeight == null){
                return(Util.BIG_INT_MIN_ONE);
            }

            return (maxHeight);
        }

        throw new Exception("Unimplemented location type: " + locationType.toString() + "!");
    }

    //sets max block height in location
    public void setMaxBlockSize(LocationType locationType, BigInteger height, Blockchain structure) throws Exception{
        if ((locationType.getIndex() & 2) != 0){
            //locally
            String json = serServ.encodeJSON(height);
            apsServ.put(SettingsType.MAX_BLOCK_HEIGHT.toString(), json, structure, BlockchainUnitType.SETTINGS);
        }

        if ((locationType.getIndex() & 1) != 0){
            //network
            BigInteger maxHeight = getMaxBlockSize(LocationType.NETWORK, structure);

            if ((maxHeight == null) || (height.compareTo(maxHeight)) > 0) {
                p2PObjectService.putJsonEncoded(height, SettingsType.MAX_BLOCK_HEIGHT.toString(), structure.getConnection());
            }
        }
    }

    //gets the hash for the block height from location
    public String getBlockHashFromHeight(LocationType locationType, BigInteger blockHeight, Blockchain structure) throws Exception{
        if (locationType == LocationType.BOTH){
            throw new Exception("Decide from where to get the data!");
        }

        if (locationType == LocationType.LOCAL){
            return ((String) apsServ.get(getHeightBlockHashString(blockHeight), structure, BlockchainUnitType.BLOCK_INDEX));
        }

        if (locationType == LocationType.NETWORK){
            return(p2PObjectService.getJsonDecoded(getHeightBlockHashString(blockHeight), structure.getConnection(), String.class));
        }

        throw new Exception("Unimplemented location type: " + locationType.toString() + "!");
    }

    //sets the hash for a block height in location
    public void setBlockHashWithHeight(LocationType locationType, BigInteger blockHeight, String hash, Blockchain structure) throws Exception{
        if ((locationType.getIndex() & 2) != 0){
            //locally
            apsServ.put(getHeightBlockHashString(blockHeight), hash, structure, BlockchainUnitType.BLOCK_INDEX);
        }

        if ((locationType.getIndex() & 1) != 0){
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

        return (result);
    }

    public ExecutionReport bootstrap(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw) {
        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        result.combine(new ExecutionReport().ok("Bootstrapping... [local height: " + maxBlkHeightLocal.toString(10) + " > network height: " +
                maxBlkHeightNetw.toString(10) + "..."));
        //state.setBootstrapping(true);

        String strHashBlock;
        Block blk;

        //re-run stored blocks to update internal state
        for (BigInteger counter = BigInteger.ZERO; counter.compareTo(maxBlkHeightLocal) <= 0; counter = counter.add(BigInteger.ONE)){
            try{
                strHashBlock = getBlockHashFromHeight(LocationType.LOCAL, counter, state.getBlockchain());

                if (strHashBlock == null) {
                    result.ko("Can not bootstrap! Could not find block with nonce = " + counter.toString(10) + " on LOCAL!");
                    return(result);
                }

                blk = apsServ.get(strHashBlock, state.getBlockchain(), BlockchainUnitType.BLOCK);

                if (blk == null) {
                    result.ko("Can not find block hash " + strHashBlock + " on LOCAL!");
                    break;
                }

                ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

                result.combine(exExecuteBlock);

                if (!result.isOk()){
                    return (result);
                }
            } catch (Exception ex){
                result.ko(ex);
                return (result);
            }
        }

        for (BigInteger counter = maxBlkHeightLocal.add(BigInteger.ONE); counter.compareTo(maxBlkHeightNetw) <= 0; counter = counter.add(BigInteger.ONE)) {
            //get the hash of the block from network
            try {
                strHashBlock = getBlockHashFromHeight(LocationType.NETWORK, counter, state.getBlockchain());
            } catch (Exception ex) {
                result.ko(ex);
                return (result);
            }

            if (strHashBlock == null) {
                result.ko("Can not bootstrap! Could not find block with nonce = " + counter.toString(10) + " on DHT!");
                return(result);
            }

            try {
                blk = p2PObjectService.getJsonDecoded(strHashBlock, state.getConnection(), Block.class);
            } catch (Exception ex) {
                result.ko(ex);
                return (result);
            }

            if (blk == null) {
                result.ko("Can not find block hash " + strHashBlock + " on DHT!");
                break;
            }

            ExecutionReport exExecuteBlock = AppServiceProvider.getExecutionService().processBlock(blk, state.getAccounts(), state.getBlockchain());

            result.combine(exExecuteBlock);

            if (!result.isOk()){
                return (result);
            }

            //block successfully processed, add it to blockchain structure
            result.combine(putBlockInBlockchain(blk, strHashBlock, state));

            if (!result.isOk()){
                return (result);
            }
        }

        return(result);
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

                if (!result.isOk()){
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

        return(result);
    }

    public ExecutionReport rebuildFromDiskDeltaNoExec(Application application, BigInteger maxBlkHeightLocal, BigInteger maxBlkHeightNetw) {
        ExecutionReport result = new ExecutionReport();
        AppState state = application.getState();

        BlockchainService blockchainService = AppServiceProvider.getBlockchainService();


        result.combine(new ExecutionReport().ok("Start bootstrapping by loading from disk..."));
        //state.setBootstrapping(true);
        //start pushing blocks and transactions
        //block with nonce = 0 is genesis and should be broadcast
        for (BigInteger counter = maxBlkHeightNetw.add(BigInteger.ONE); counter.compareTo(maxBlkHeightLocal) <= 0; counter = counter.add(BigInteger.ONE)) {
            try {
                result.combine(new ExecutionReport().ok("Put block with height: " + counter.toString(10) + "..."));
                //put block
                String strHashBlk = getBlockHashFromHeight(LocationType.LOCAL, counter, state.getBlockchain());
                Block blk = blockchainService.get(strHashBlk, state.getBlockchain(), BlockchainUnitType.BLOCK);
                p2PObjectService.putJsonEncoded(blk, strHashBlk, state.getConnection());

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

        return(result);
    }

    public ExecutionReport putBlockInBlockchain(Block blk, String blockHash, AppState state) {
        ExecutionReport result = new ExecutionReport();

        BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();

        try {
            appPersistanceService.put(blockHash, blk, state.getBlockchain(), BlockchainUnitType.BLOCK);
            setBlockHashWithHeight(LocationType.LOCAL, blk.getNonce(), blockHash, state.getBlockchain());
            appPersistanceService.put(blk.getNonce(), blockHash, state.getBlockchain(), BlockchainUnitType.BLOCK_INDEX);
            setMaxBlockSize(LocationType.LOCAL, blk.getNonce(), state.getBlockchain());
            state.setCurrentBlock(blk);
            result.combine(new ExecutionReport().ok("Put block in blockchain with hash: " + blockHash));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }

    public ExecutionReport putTransactionInBlockchain(Transaction transaction, String transactionHash, AppState state) {
        ExecutionReport result = new ExecutionReport();

        BlockchainService appPersistanceService = AppServiceProvider.getAppPersistanceService();

        try {
            appPersistanceService.put(transactionHash, transaction, state.getBlockchain(), BlockchainUnitType.TRANSACTION);
            result.combine(new ExecutionReport().ok("Put transaction in blockchain with hash: " + transactionHash));
        } catch (Exception ex) {
            result.combine(new ExecutionReport().ko(ex));
        }

        return (result);
    }

    //generate block height name to search the hash
    public String getHeightBlockHashString(BigInteger blockHeight){
        return(SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }
}
