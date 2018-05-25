package network.elrond.data;

import network.elrond.blockchain.Blockchain;
import network.elrond.blockchain.BlockchainService;
import network.elrond.blockchain.BlockchainUnitType;
import network.elrond.blockchain.SettingsType;
import network.elrond.core.Util;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrapServiceImpl implements BootstrapService {

    private BlockchainService apsServ = AppServiceProvider.getAppPersistanceService();
    private SerializationService serServ = AppServiceProvider.getSerializationService();

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
        String strJSONData = (String) AppServiceProvider.getP2PObjectService().get(connection, SettingsType.MAX_BLOCK_HEIGHT.toString());

        if (strJSONData == null) {
            return (Util.BIG_INT_MIN_ONE);
        }

        return (new BigInteger(serServ.decodeJSON(strJSONData, String.class)));
    }

    @Override
    public String getBlockHashFromBlockHeight(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException {
        return ((String) apsServ.get(blockHeight, structure, BlockchainUnitType.BLOCK_INDEX));
    }


}
