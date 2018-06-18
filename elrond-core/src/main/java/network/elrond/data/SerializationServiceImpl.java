package network.elrond.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import network.elrond.core.Util;
import network.elrond.crypto.SHA3Helper;
import network.elrond.service.AppServiceProvider;
import network.elrond.crypto.util.encoders.Base64;

import java.io.IOException;

public class SerializationServiceImpl implements SerializationService {


    @Override
    public <T> String encodeJSON(T object) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filter = new SimpleFilterProvider();

        filter.setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());

        mapper.setFilterProvider(filter);

        try {
            return (mapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <T> T decodeJSON(String strJSONData, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(strJSONData, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] getHash(Object object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }

        String json = AppServiceProvider.getSerializationService().encodeJSON(object);
        Util.check(json != null, "json is null");

//        MessageDigest instance = null;
////        try {
////            instance = MessageDigest.getInstance("SHA3-256");
////        } catch (NoSuchAlgorithmException ex) {
////            throw new RuntimeException(ex);
////        }
////        return instance.digest(json.getBytes());
        return (SHA3Helper.sha3(json.getBytes()));
    }

    @Override
    public String getHashString(Object object) {
        return new String(Base64.encode(getHash(object)));
    }


}
