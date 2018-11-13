package network.elrond.crypto;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;

import network.elrond.db.ByteArrayWrapper;


public class HashUtil {

    private static final int MAX_ENTRIES = 100; // Should contain most commonly hashed values
    private static final Map<ByteArrayWrapper, byte[]> SHA3_CACHE = Collections.synchronizedMap(new LRUMap<>(MAX_ENTRIES));

    public static byte[] sha3(byte[] input) {
        ByteArrayWrapper inputByteArray = new ByteArrayWrapper(input);
        byte[] result = SHA3_CACHE.get(inputByteArray);
        if(result != null)
            return result;
        result = SHA3Helper.sha3(input);
        SHA3_CACHE.put(inputByteArray, result);
        return result;
    }
}
