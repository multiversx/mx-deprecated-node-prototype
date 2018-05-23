package network.elrond.data;

import network.elrond.application.AppState;
import network.elrond.blockchain.*;
import network.elrond.core.Util;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.math.BigInteger;

public class BootstrapServiceImpl implements BootstrapService {
    private BlockchainService apsServ = AppServiceProvider.getAppPersistanceService();
    private SerializationService serServ = AppServiceProvider.getSerializationService();

    public BigInteger getMaxBlockSizeLocal(Blockchain structure) throws IOException, ClassNotFoundException{
        String maxHeight = apsServ.get(SettingsType.MAX_BLOCK_HEIGHT.toString(), structure, BlockchainUnitType.SETTINGS);

        if (maxHeight == null){
            apsServ.put(SettingsType.MAX_BLOCK_HEIGHT.toString(),
                    serServ.encodeJSON(BigInteger.ZERO), structure, BlockchainUnitType.SETTINGS);
            return (BigInteger.ZERO);
        }

        return (new BigInteger(maxHeight));
    }

    public void setMaxBlockSizeLocal(Blockchain structure, BigInteger height) throws IOException, ClassNotFoundException{
        BigInteger maxHeight = getMaxBlockSizeLocal(structure);

        if (height.compareTo(maxHeight) > 0){
            apsServ.put(SettingsType.MAX_BLOCK_HEIGHT.toString(),
                    serServ.encodeJSON(height), structure, BlockchainUnitType.SETTINGS);
        }
    }

    public BigInteger getMaxBlockSizeNetwork(P2PConnection connection) throws IOException, ClassNotFoundException,
            NullPointerException{
        String strJSONData = (String) AppServiceProvider.getP2PObjectService().get(connection, SettingsType.MAX_BLOCK_HEIGHT.toString());

        if (strJSONData == null) {
            return(Util.BIG_INT_MIN_ONE);
        }

        return (new BigInteger(serServ.decodeJSON(strJSONData, String.class)));
    }

    public String getBlockHashFromBlockHeight(Blockchain structure, BigInteger blockHeight) throws IOException, ClassNotFoundException{
        return((String)apsServ.get(blockHeight, structure, BlockchainUnitType.BLOCK_INDEX));
    }



}
