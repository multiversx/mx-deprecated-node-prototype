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


    @Override
    public BigInteger getMaxBlockSizeLocal(Blockchain structure) throws IOException, ClassNotFoundException {
        String maxHeight = apsServ.get(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure, BlockchainUnitType.SETTINGS);

        if (maxHeight == null) {
            String json = serServ.encodeJSON(BigInteger.ZERO);
            apsServ.put(SettingsType.MAX_BLOCK_HEIGHT.toString(), json, structure, BlockchainUnitType.SETTINGS);
            return (BigInteger.ZERO);
        }

        return (new BigInteger(maxHeight));
    }

    @Override
    public void setMaxBlockSizeLocal(Blockchain structure, BigInteger height) throws IOException, ClassNotFoundException {
        BigInteger maxHeight = getMaxBlockSizeLocal(structure);

        if (height.compareTo(maxHeight) > 0) {
            String json = serServ.encodeJSON(height);
            apsServ.put(SettingsType.MAX_BLOCK_HEIGHT.toString(), json, structure, BlockchainUnitType.SETTINGS);
        }
    }

    @Override
    public BigInteger getMaxBlockSizeNetwork(P2PConnection connection) throws IOException, ClassNotFoundException {

        return (p2PObjectService.getJSONdecoded(SettingsType.MAX_BLOCK_HEIGHT.toString(),connection, BigInteger.class));
    }

    public void setMaxBlockSizeNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException{

        p2PObjectService.putJSONencoded(blockHeight, SettingsType.MAX_BLOCK_HEIGHT.toString(), connection);
    }

    public void setBlockHeightHashNetwork(BigInteger blockHeight, String strHash, P2PConnection connection) throws IOException {

        p2PObjectService.putJSONencoded(strHash, getHeightBlockHashString(blockHeight), connection);
    }

    public String getBlockHeightHashNetwork(BigInteger blockHeight, P2PConnection connection) throws IOException, ClassNotFoundException {

        return(p2PObjectService.getJSONdecoded(getHeightBlockHashString(blockHeight), connection, String.class));
    }

    @Override
    public String getBlockHashFromBlockHeight(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException {
        return ((String) apsServ.get(getHeightBlockHashString(blockHeight), structure, BlockchainUnitType.BLOCK_INDEX));
    }

    public String getHeightBlockHashString(BigInteger blockHeight){
        return(SettingsType.HEIGHT_BLOCK.toString() + "_" + blockHeight.toString(10));
    }
}
