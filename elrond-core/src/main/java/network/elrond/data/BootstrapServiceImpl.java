package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.p2p.P2PConnection;
import network.elrond.p2p.P2PObjectService;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrapServiceImpl implements BootstrapService {

    private BlockchainService apsServ = AppServiceProvider.getAppPersistanceService();
    private SerializationService serServ = AppServiceProvider.getSerializationService();
    private P2PObjectService p2PObjectService = AppServiceProvider.getP2PObjectService();

    //returns max block height from local data (disk)
    public BigInteger getMaxBlockSizeLocal(Blockchain structure) throws IOException, ClassNotFoundException {
        String maxHeight = apsServ.get(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure, BlockchainUnitType.SETTINGS);

        if (maxHeight == null) {
            return(Util.BIG_INT_MIN_ONE);
        }

        return (new BigInteger(maxHeight));
    }

    //sets max block height on local (disk)
    public void setMaxBlockSizeLocal(Blockchain structure, BigInteger height) throws IOException, ClassNotFoundException {
        BigInteger maxHeight = getMaxBlockSizeLocal(structure);

        if ((maxHeight == null) || (height.compareTo(maxHeight)) > 0) {
            String json = serServ.encodeJSON(height);
            apsServ.put(SettingsType.MAX_BLOCK_HEIGHT.toString(), json, structure, BlockchainUnitType.SETTINGS);
        }
    }

    //returns max block height from network (DHT)
    public BigInteger getMaxBlockSizeNetwork(P2PConnection connection) throws IOException, ClassNotFoundException {

        BigInteger maxHeight = p2PObjectService.getJsonDecoded(SettingsType.MAX_BLOCK_HEIGHT.toString(),connection, BigInteger.class);

        if (maxHeight == null){
            return(Util.BIG_INT_MIN_ONE);
        }

        return (maxHeight);
    }

    //sets max block height on network (DHT)
    public void setMaxBlockSizeNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException{

        p2PObjectService.putJsonEncoded(blockHeight, SettingsType.MAX_BLOCK_HEIGHT.toString(), connection);
    }

    //gets the hash for the block height from local data (disk)
    public String getBlockHashFromHeightLocal(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException {
        return ((String) apsServ.get(getHeightBlockHashString(blockHeight), structure, BlockchainUnitType.BLOCK_INDEX));
    }

    //sets the hash for a block height on local (disk)
    public void setBlockHashFromHeightLocal(Blockchain structure, BigInteger blockHeight, String strHash) throws IOException {
        apsServ.put(getHeightBlockHashString(blockHeight), strHash, structure, BlockchainUnitType.BLOCK_INDEX);
    }

    //gets the hash for the block height from network (DHT)
    public String getBlockHashFromHeightNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException, ClassNotFoundException {

        return(p2PObjectService.getJsonDecoded(getHeightBlockHashString(blockHeight), connection, String.class));
    }

    //sets the hash for a block height on network (DHT)
    public void setBlockHashFromHeightNetwork(BigInteger blockHeight, String strHash, P2PConnection connection) throws IOException {

        p2PObjectService.putJsonEncoded(strHash, getHeightBlockHashString(blockHeight), connection);
    }

    //generate block height name to search the hash
    public String getHeightBlockHashString(BigInteger blockHeight){
        return(SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }
}
