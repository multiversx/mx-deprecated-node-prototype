package network.elrond.data;

public interface SerializationService {

    <T> String encodeJSON(T object);

    <T> T decodeJSON(String strJSONData, Class<T> clazz);

    byte[] getHash(Object object);

    byte[] getHash(String hash);

    String getHashString(Object object);

    String getHashString(byte[] hash);

}
