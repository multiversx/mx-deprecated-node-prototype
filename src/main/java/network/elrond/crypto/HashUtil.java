package network.elrond.crypto;

import network.elrond.db.ByteArrayWrapper;

import network.elrond.core.LRUMap;

public class HashUtil {

    private static final int MAX_ENTRIES = 100; // Should contain most commonly hashed values
    private static LRUMap<ByteArrayWrapper, byte[]> sha3Cache = new LRUMap<>(0, MAX_ENTRIES);

    public static byte[] sha3(byte[] input) {
        ByteArrayWrapper inputByteArray = new ByteArrayWrapper(input);
        byte[] result = sha3Cache.get(inputByteArray);
        if(result != null)
            return result;
        result = SHA3Helper.sha3(input);
        sha3Cache.put(inputByteArray, result);
        return result;
    }
}
