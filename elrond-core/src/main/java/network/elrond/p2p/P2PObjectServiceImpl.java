package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import network.elrond.core.Util;
import network.elrond.sharding.Shard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

public class P2PObjectServiceImpl implements P2PObjectService {
    private static final Logger logger = LogManager.getLogger(P2PObjectServiceImpl.class);

    @Override
    public <T> DHTResponseObject<T> get(P2PConnection connection, String key, Class<T> clazz) throws ClassNotFoundException, IOException {
        logger.traceEntry("params: {} {} {}", connection, key, clazz);

        PeerDHT peer = connection.getDht();

        Number160 hash = getDHTHash(connection, key, clazz);
        FutureGet futureGet;
//        int retries = Util.MAX_RETRIES_GET;
//
//        do {
//            retries--;
//            futureGet = peer.get(hash).start();
//            futureGet.awaitUninterruptibly(3000);
//        } while (!futureGet.isSuccess() && retries > 0);

        futureGet = peer.get(hash).start();
        futureGet.awaitUninterruptibly(3000);

        if (futureGet.isSuccess()) {
            Iterator<Data> iterator = futureGet.dataMap().values().iterator();
            if (!iterator.hasNext()) {
                logger.warn("DHT data null on success");
                return logger.traceExit(new DHTResponseObject<>(null, ResponseDHT.SUCCESS));
            }
            Data data = iterator.next();
            T object = (T) data.object();
            logger.warn("Retrieved key: {} => {}", key, object);
            return logger.traceExit(new DHTResponseObject<>(object, ResponseDHT.SUCCESS));

        } else {
            logger.warn("Timeout getting data with hash {}", key);
        }

        return logger.traceExit(new DHTResponseObject<>(null, ResponseDHT.TIMEOUT));
    }

    @Override
    public <T extends Serializable> FuturePut put(P2PConnection connection, String key, T value) throws IOException {
        return put(connection, key, value, true, true);
    }

    @Override
    public <T extends Serializable> FuturePut put(P2PConnection connection, String key, T value, boolean await, boolean canOverwrite) throws IOException {
        logger.traceEntry("params: {} {} {}", connection, key, value);
        PeerDHT peer = connection.getDht();

        Number160 hash = getDHTHash(connection, key, value.getClass());

        PutBuilder builder = peer.put(hash);
        if (!canOverwrite) {
            builder.putIfAbsent();
        }

        FuturePut fp = builder.data(new Data(value)).start();
        logger.trace("Put object with key {}", key);

        if (await) {
            logger.trace("awaiting");
            int retries = Util.MAX_RETRIES_PUT;

            fp.awaitUninterruptibly();
            while (!fp.isSuccess() && retries > 0) {
                retries--;
                fp = builder.data(new Data(value)).start();
                fp.awaitUninterruptibly();
            }

            if (fp.isSuccess()) {
                logger.warn("put finished with {} retries", Util.MAX_RETRIES_PUT - retries);
            } else {
                logger.warn("put not successful after {} retries", Util.MAX_RETRIES_PUT);
            }
        }

        return logger.traceExit(fp);
    }


    private <T> Number160 getDHTHash(P2PConnection connection, String key, Class<T> clazz) {

        String clazzName = clazz.getName();
        Shard shard = connection.getShard();
        Integer shardIndex = shard.getIndex();

        String string = key + "_#_" + clazzName + "_#_" + shardIndex;
        logger.trace("Computing hash {} for key:{} class:{} shard:{}", string, key, clazzName, shardIndex);

        return Number160.createHash(string);
    }

}
