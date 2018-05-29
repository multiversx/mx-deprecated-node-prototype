package network.elrond.p2p;

import net.tomp2p.dht.FuturePut;

import java.io.IOException;

public interface P2PObjectService {

    Object get(P2PConnection connection, String key) throws ClassNotFoundException, IOException;

    FuturePut put(P2PConnection connection, String key, Object value) throws IOException;

    void putJSONencoded(Object object, String hash, P2PConnection connection) throws IOException;

    <T> T getJSONdecoded(String hash, P2PConnection connection, Class<T> clazz) throws IOException, ClassNotFoundException;
}
