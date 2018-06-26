package network.elrond.p2p;

import net.tomp2p.dht.FuturePut;

import java.io.IOException;
import java.io.Serializable;

public interface P2PObjectService {

    <T> T get(P2PConnection connection, String key, Class<T> clazz) throws ClassNotFoundException, IOException;

    <T extends Serializable> FuturePut put(P2PConnection connection, String key, T value) throws IOException;

    <T extends Serializable> FuturePut putIfAbsent(P2PConnection connection, String key, T value) throws Exception;

}
