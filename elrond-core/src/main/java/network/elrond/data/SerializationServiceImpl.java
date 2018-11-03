package network.elrond.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongycastle.util.encoders.Base64;

import java.io.IOException;

public class SerializationServiceImpl implements SerializationService {
    private static final Logger logger = LogManager.getLogger(SerializationServiceImpl.class);

    @Override
    public <T> String encodeJSON(T object) {
        logger.traceEntry("params {}", object);
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filter = new SimpleFilterProvider();

        filter.setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());

        mapper.setFilterProvider(filter);

        try {
            return logger.traceExit(mapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            logger.catching(e);
            return logger.traceExit((String)null);
        }
    }

    @Override
    public <T> T decodeJSON(String strJSONData, Class<T> clazz) {
        logger.traceEntry("params {} {}", strJSONData, clazz);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return logger.traceExit(mapper.readValue(strJSONData, clazz));
        } catch (IOException e) {
            logger.catching(e);
            return logger.traceExit((T)null);
        }
    }

    @Override
	public byte[] getHash(Object object) {
        logger.traceEntry("params {}", object);
        Util.check(object != null, "object is null");

        String json = AppServiceProvider.getSerializationService().encodeJSON(object);
        Util.check(json != null, "json is null");

//        MessageDigest instance = null;
////        try {
////            instance = MessageDigest.getInstance("SHA3-256");
////        } catch (NoSuchAlgorithmException ex) {
////            throw new RuntimeException(ex);
////        }
////        return instance.digest(json.getBytes());
        return logger.traceExit((Util.SHA3.get().digest(json.getBytes())));
    }

    @Override
    public String getHashString(Object object) {
        return new String(Base64.encode(getHash(object)));
    }


}
