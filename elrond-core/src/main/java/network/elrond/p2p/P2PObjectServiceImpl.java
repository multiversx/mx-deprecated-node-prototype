package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

public class P2PObjectServiceImpl implements P2PObjectService {

    Logger logger = LoggerFactory.getLogger(P2PObjectServiceImpl.class);

    @Override
    public <T> T get(P2PConnection connection, String key, Class<T> clazz) throws ClassNotFoundException, IOException {

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
            logger.debug("Retrieved key: {0} => {1}", key, object);
            return object;
        } else {
            LoggerFactory.getLogger(P2PBroadcastServiceImpl.class).info("Timeout getting! hash: " + key);
            logger.debug("Timeout getting! key:  {0}", key);
        }
        return null;
    }

    @Override
    public <T extends Serializable> FuturePut put(P2PConnection connection, String key, T value) throws IOException {
        PeerDHT peer = connection.getDht();

        String clazzName = value.getClass().getName();

        FuturePut fp = peer.put(Number160.createHash(key + clazzName)).data(new Data(value)).start();
        logger.debug("Put object with key {0}", key + clazzName);

        fp.awaitUninterruptibly();

        return (fp);
    }

}
