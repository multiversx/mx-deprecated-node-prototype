package network.elrond.data;

public interface SerializationService {

    <T> String encodeJSON(T object);

    <T> String encodeJSON(T object, String filterName, String... ignoredFields);

    <T> T decodeJSON(String strJSONData, Class<T> clazz);

    byte[] getHash(Object object, boolean withSig);
}
