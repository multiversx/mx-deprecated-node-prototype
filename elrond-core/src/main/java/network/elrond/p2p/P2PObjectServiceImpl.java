package network.elrond.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import network.elrond.service.AppServiceProvider;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

public class P2PObjectServiceImpl implements P2PObjectService {


    @Override
    public Object get(P2PConnection connection, String key) throws ClassNotFoundException, IOException {

        PeerDHT peer = connection.getDht();
        FutureGet futureGet = peer.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly(500);
        if (futureGet.isSuccess()) {
            Iterator<Data> iterator = futureGet.dataMap().values().iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            Data data = iterator.next();
            return data.object();
        } else {
            LoggerFactory.getLogger(P2PBroadcastServiceImpl.class).warn("Timeout getting! hash: " + key);
        }
        return null;
    }

    @Override
    public FuturePut put(P2PConnection connection, String key, Object value) throws IOException {
        PeerDHT peer = connection.getDht();

        FuturePut fp = peer.put(Number160.createHash(key)).data(new Data(value)).start();

        fp.awaitUninterruptibly();

        return (fp);
    }

    public void putJSONencoded(Object object, String hash, P2PConnection connection) throws IOException{
        if (object == null || hash == null) {
            return;
        }

        String strJSONData = AppServiceProvider.getSerializationService().encodeJSON(object);

        AppServiceProvider.getP2PObjectService().put(connection, hash.toString(), strJSONData);
    }

    public <T> T getJSONdecoded(String hash, P2PConnection connection, Class<T> clazz) throws IOException, ClassNotFoundException{
        if (hash == null) {
            return null;
        }

        String strJSONData = (String) AppServiceProvider.getP2PObjectService().get(connection, hash);

        if (strJSONData == null){
            return (null);
        }

        return (T)AppServiceProvider.getSerializationService().decodeJSON(strJSONData, clazz);
    }

}
