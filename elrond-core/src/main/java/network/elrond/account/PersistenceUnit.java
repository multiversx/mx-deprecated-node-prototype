package network.elrond.account;

import java.io.IOException;

public interface PersistenceUnit<K, V> {

	void put(byte[] key, byte[] val);

	byte[] get(byte[] key);

	void close() throws IOException;

}
