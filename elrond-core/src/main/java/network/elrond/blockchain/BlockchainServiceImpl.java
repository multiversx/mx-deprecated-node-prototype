package network.elrond.blockchain;

import network.elrond.core.LRUMap;
import network.elrond.core.Util;
import network.elrond.p2p.P2PConnection;
import network.elrond.p2p.P2PRequestChannel;
import network.elrond.p2p.P2PRequestChannelName;
import network.elrond.service.AppServiceProvider;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class BlockchainServiceImpl implements BlockchainService {

    private static final Logger logger = LogManager.getLogger(BlockchainServiceImpl.class);

    /**
     * Check if block is in blockchain (memory->database->network)
     *
     * @param hash block hash
     * @return
     */
    @Override
    public synchronized <H extends Object, B extends Serializable> boolean contains(H hash, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {
        logger.traceEntry("params: {} {} {}", hash, blockchain, type);
        Util.check(hash != null, "hash!=null");
        Util.check(blockchain != null, "blockchain!=null");
        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
        P2PConnection connection = blockchain.getConnection();
        LRUMap<H, B> cache = unit.getCache();

        if (cache.contains(hash)) {
            logger.trace("cache contains hash");
            return logger.traceExit(true);
        }
        B block = get(hash, blockchain, type);
        return logger.traceExit(block != null);
    }


    /**
     * Put object on object chain (memory->database->network) and wait
     *
     * @param hash   object hash
     * @param object
     * @throws IOException
     */
    //@Override
    public synchronized <H extends Object, B extends Serializable> void putAndWait(H hash, B object, Blockchain blockchain, BlockchainUnitType type) throws IOException {
        boolean await = true;
        put(hash, object, blockchain, type, await);
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
        boolean await = false;
        put(hash, object, blockchain, type, await);
    }

    private <H extends Object, B extends Serializable> void put(H hash, B object, Blockchain blockchain, BlockchainUnitType type, boolean await) throws IOException {
        logger.traceEntry("params: {} {} {} {}", hash, object, blockchain, type);

        Util.check(hash != null, "hash!=null");
        Util.check(object != null, "object!=null");
        Util.check(blockchain != null, "blockchain!=null");

//        if (object == null || hash == null) {
//            logger.trace("object or hash is null");
//            logger.traceExit();
//            return;
//        }

        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
        P2PConnection connection = blockchain.getConnection();

        unit.getCache().put(hash, object);
        String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
        unit.put(bytes(hash.toString()), bytes(strJSONData));

        logger.trace("Locally stored!");

        if (!isOffline(connection)) {

            AppServiceProvider.getP2PObjectService().put(connection, hash.toString(), object, await, true);
            logger.trace("DHT stored!");
        }

        logger.traceExit();
    }

    @Override
    public <H extends Object, B extends Serializable> List<B> getAll(List<H> hashes, Blockchain blockchain, BlockchainUnitType type) throws IOException, ClassNotFoundException {
        logger.traceEntry("params: {} {} {}", hashes, blockchain, type);

        Util.check(hashes != null, "hashes!=null");
        Util.check(blockchain != null, "blockchain!=null");

        List<B> list = new ArrayList<>();

        for (H hash : hashes) {
            list.add(get(hash, blockchain, type));
        }

        return logger.traceExit(list);
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
        logger.traceEntry("params: {} {} {}", hash, blockchain, type);

        Util.check(hash != null, "hash!=null");
        Util.check(blockchain != null, "blockchain!=null");

        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);
        P2PConnection connection = blockchain.getConnection();

        LRUMap<H, B> cache = unit.getCache();

        boolean exists = cache.get(hash) != null;
        if (!exists) {
            B object = getDataFromDatabase(hash, unit);
            if (object == null) {
                logger.trace("Getting from DHT...");
                object = getDataFromNetwork(hash, unit, connection);
                if (object == null) {
                    object = requestData(hash, type, connection);
                }
            }

            if (object != null) {
                cache.put(hash, object);
                String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);
                unit.put(bytes(hash.toString()), bytes(strJSONData));
                logger.trace("Got from local storace, placed on DHT!");
            }
        }

        B result = cache.get(hash);
        return logger.traceExit(result);

    }

    public synchronized <H extends Object, B extends Serializable> B getLocal(H hash, Blockchain blockchain, BlockchainUnitType type) {
        logger.traceEntry("params: {} {} {}", hash, blockchain, type);

        Util.check(hash != null, "hash!=null");
        Util.check(blockchain != null, "blockchain!=null");

        BlockchainPersistenceUnit<H, B> unit = blockchain.getUnit(type);

        LRUMap<H, B> cache = unit.getCache();

        boolean exists = cache.get(hash) != null;
        if (!exists) {
            B object = getDataFromDatabase(hash, unit);

            if (object != null) {
                cache.put(hash, object);
                logger.trace("Got from local storace");
            }
        }

        B result = cache.get(hash);
        return logger.traceExit(result);
    }

    private <H extends Object, B extends Serializable> B getDataFromNetwork(H hash, BlockchainPersistenceUnit<H, B> unit, P2PConnection connection)
            throws ClassNotFoundException, IOException {
        logger.traceEntry("params: {} {} {}", hash, unit, connection);

        if (isOffline(connection)) {
            logger.trace("offline!");
            logger.traceExit();
            return null;
        }
        return AppServiceProvider.getP2PObjectService().get(connection, hash.toString(), unit.clazz);
    }

    private <B extends Serializable, H extends Object> B getDataFromDatabase(H hash, BlockchainPersistenceUnit<H, B> unit) {
        logger.traceEntry("params: {} {}", hash, unit);
        byte[] data = unit.get(bytes(hash.toString()));
        if (data == null) {
            logger.trace("data do not exists!");
            logger.traceExit();
            return null;
        }

        String strJSONData = asString(data);
        return logger.traceExit(decodeObject(unit.clazz, strJSONData));
    }

    private <B extends Serializable> B decodeObject(Class<B> clazz, String strJSONData) {
        logger.traceEntry("params: {} {}", clazz, strJSONData);
        if (strJSONData == null) {
            logger.trace("strJSONData is null");
            logger.traceExit();
            return null;
        }
        return logger.traceExit(AppServiceProvider.getSerializationService().decodeJSON(strJSONData, clazz));
    }

    protected boolean isOffline(P2PConnection connection) {
        return connection == null;
    }


    private <H extends Object, B extends Serializable> B requestData(H hash, BlockchainUnitType unitType, P2PConnection connection) {
        logger.traceEntry("params: {} {} {}", hash, unitType, connection);

        B response;
        P2PRequestChannel channel = connection.getRequestChannel(unitType.name());
        if (channel == null) {
            return logger.traceExit((B) null);
        }

        P2PRequestChannelName channelName = channel.getName();
        Shard shard = connection.getShard();
        String hashStr = hash.toString();

        response = AppServiceProvider.getP2PRequestService().get(channel, shard, channelName, hashStr);

        logger.warn("Requested {} with hash {}. Received: {}", channel.getName(), hash, response);
        return logger.traceExit(response);
    }
}
