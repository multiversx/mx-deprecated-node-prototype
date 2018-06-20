package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

public class P2PObjectServiceImpl implements P2PObjectService {
    private static final Logger logger = LogManager.getLogger(P2PObjectServiceImpl.class);

    @Override
    public <T> T get(P2PConnection connection, String key, Class<T> clazz) throws ClassNotFoundException, IOException {
        logger.traceEntry("params: {} {} {}", connection, key, clazz);
        PeerDHT peer = connection.getDht();
        String clazzName = clazz.getName();

        FutureGet futureGet = peer.get(Number160.createHash(key + clazzName)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            Iterator<Data> iterator = futureGet.dataMap().values().iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            Data data = iterator.next();
            T object = (T) data.object();
            logger.trace("Retrieved key: {} => {}", key, object);
            return logger.traceExit(object);
        } else {
            logger.warn("Timeout getting data with hash {}", key);
        }
        return logger.traceExit((T)null);
    }

    @Override
    public <T extends Serializable> FuturePut put(P2PConnection connection, String key, T value) throws IOException {
        logger.traceEntry("params: {} {} {}", connection, key, value);
        PeerDHT peer = connection.getDht();

        String clazzName = value.getClass().getName();

        FuturePut fp = peer.put(Number160.createHash(key + clazzName)).data(new Data(value)).start();
        logger.trace("Put object with key {}", key + clazzName);

        fp.awaitUninterruptibly();

        return logger.traceExit(fp);
    }

}
