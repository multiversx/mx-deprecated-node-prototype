package network.elrond.blockchain;

import network.elrond.core.LRUMap;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class BlockchainServiceImpl implements BlockchainService {


    /**
     * Check if block is in blockchain (memory->database->network)
     *
     * @param hash block hash
     * @return
     */
    @Override
    public synchronized <H extends Object, B extends Serializable> boolean contains(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {

        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
        P2PConnection connection = blockchain.getConnection();
        LRUMap<H, B> cache = unit.getCache();

        if (cache.contains(hash)) {
            return true;
        }
        B block = get(hash, blockchain, type);
        return block != null;
    }


    /**
     * Put object on object chain (memory->database->network)
     *
     * @param hash   object hash
     * @param object
     * @throws IOException
     */
    @Override
    public synchronized <H extends Object, B extends Serializable> void put(H hash, B object, Blockchain blockchain, BlockchainUnitType type) throws IOException {


        if (object == null || hash == null) {
            return;
        }

        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
        P2PConnection connection = blockchain.getConnection();

        unit.getCache().put(hash, object);
        String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
        unit.put(bytes(hash.toString()), bytes(strJSONData));

        if (!isOffline(connection)) {
            AppServiceProvider.getP2PObjectService().put(connection, hash.toString(), object);
        }

    }

    @Override
    public <H extends Object, B extends Serializable> List<B> getAll(List<H> hashes, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {

        List<B> list = new ArrayList<>();

        for (H hash : hashes) {
            list.add(get(hash, blockchain, type));
        }

        return list;
    }

    /**
     * Get block form blockchain (memory->database->network)
     *
     * @param hash block hash
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public synchronized <H extends Object, B extends Serializable> B get(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {

        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
        P2PConnection connection = blockchain.getConnection();

        LRUMap<H, B> cache = unit.getCache();

        boolean exists = cache.get(hash) != null;
        if (!exists) {
            B object = getDataFromDatabase(hash, unit);
            if (object == null) {
                object = getDataFromNetwork(hash, unit, connection);
            }

            if (object != null) {
                cache.put(hash, object);
                String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
                unit.put(bytes(hash.toString()), bytes(strJSONData));
            }
        }

        B result = cache.get(hash);
        return result;

    }

    private <H extends Object, B extends Serializable> B getDataFromNetwork(H hash, BlockchainPersistenceUnit<H, B> unit, P2PConnection connection)
            throws ClassNotFoundException, IOException {

        if (isOffline(connection)) {
            return null;
        }
        return AppServiceProvider.getP2PObjectService().get(connection, hash.toString(), unit.clazz);
    }

    private <B extends Serializable, H extends Object> B getDataFromDatabase(H hash, BlockchainPersistenceUnit<H, B> unit) {
        byte[] data = unit.get(bytes(hash.toString()));
        if (data == null) {
            return null;
        }

        String strJSONData = asString(data);
        return decodeObject(unit.clazz, strJSONData);
    }

    private <B extends Serializable> B decodeObject(Class<B> clazz, String strJSONData) {
        if (strJSONData == null) {
            return null;
        }
        return AppServiceProvider.getSerializationService().decodeJSON(strJSONData, clazz);
    }

    protected boolean isOffline(P2PConnection connection) {
        return connection == null;
    }

}
