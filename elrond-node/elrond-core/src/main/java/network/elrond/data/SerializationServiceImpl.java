package network.elrond.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;

public class SerializationServiceImpl implements SerializationService {


    @Override
    public <T> String encodeJSON(T object) {
        ObjectMapper mapper = new ObjectMapper();
        FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filter);
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T> String encodeJSON(T object, String filterName, String... ignoredFields) {
        ObjectMapper mapper = new ObjectMapper();

        if (ignoredFields.length > 0) {
            FilterProvider filter = new SimpleFilterProvider();
            ((SimpleFilterProvider) filter).addFilter(filterName, SimpleBeanPropertyFilter.serializeAllExcept(ignoredFields));
            mapper.setFilterProvider(filter);

            try {
                return(mapper.writeValueAsString(object));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }

        FilterProvider filter = new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filter);

        try {
            return mapper.writeValueAsString(object);
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

    public byte[] getHash(Object object, boolean withSig) {
        String json;

        if (withSig) {
            json = AppServiceProvider.getSerializationService().encodeJSON(object);
        } else {
            json = AppServiceProvider.getSerializationService().encodeJSON(object, "filterSigs", "sig1", "sig2");
        }
        return (Util.SHA3.digest(json.getBytes()));
    }

    @Override
    public String getHashString(Object object, boolean withSig) {
        return new String(Base64.encode(getHash(object, true)));
    }


}
