package network.elrond.blockchain;

import network.elrond.core.LRUMap;
import network.elrond.p2p.P2PConnection;
import network.elrond.service.AppServiceProvider;

import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class AppPersistanceServiceImpl implements BlockchainService {
    /**
     * Check if data is in local memory (memory->database)
     *
     * @param hash block hash
     * @return
     */
    @Override
    public synchronized <H extends Object, B> boolean contains(H hash, Blockchain structure, BlockchainUnitType type) throws IOException, ClassNotFoundException {

        BlockchainPersistenceUnit<H, B> unit = structure.getUnit(type);
        LRUMap<H, B> cache = unit.cache;

        if (cache.contains(hash)) {
            return true;
        }
        B block = get(hash, structure, type);
        return block != null;
    }


    /**
     * Put object on object chain (memory->database)
     *
     * @param hash   object hash
     * @param object
     * @throws IOException
     */
    @Override
    public synchronized <H extends Object, B> void put(H hash, B object, Blockchain structure, BlockchainUnitType type) throws IOException {
        if (object == null || hash == null) {
            return;
        }

        BlockchainPersistenceUnit<H, B> unit = structure.getUnit(type);

        unit.cache.put(hash, object);
        String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
        unit.database.put(bytes(hash.toString()), bytes(strJSONData));
    }

    /**
     * Get object form memory (memory->database)
     *
     * @param hash block hash
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public synchronized <H extends Object, B> B get(H hash, Blockchain structure, BlockchainUnitType type) throws IOException, ClassNotFoundException {


        BlockchainPersistenceUnit<H, B> unit = structure.getUnit(type);

        LRUMap<H, B> cache = unit.cache;

        boolean exists = cache.get(hash) != null;
        if (!exists) {
            B object = getDataFromDatabase(hash, unit);

            if (object != null) {
                cache.put(hash, object);
            }
        }

        return cache.get(hash);
    }

    private <B, H extends Object> B getDataFromDatabase(H hash, BlockchainPersistenceUnit<H, B> unit) {
        byte[] data = unit.database.get(bytes(hash.toString()));
        if (data == null) {
            return null;
        }

        String strJSONData = asString(data);
        return decodeObject(unit.clazz, strJSONData);
    }

    private <B> B decodeObject(Class<B> clazz, String strJSONData) {
        if (strJSONData == null) {
            return null;
        }
        return AppServiceProvider.getSerializationService().decodeJSON(strJSONData, clazz);
    }
}
