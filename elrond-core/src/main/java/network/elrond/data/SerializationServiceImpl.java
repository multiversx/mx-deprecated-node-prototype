package network.elrond.data;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import network.elrond.core.Util;
import network.elrond.service.AppServiceProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;

public class SerializationServiceImpl implements SerializationService {


    @Override
    public <T> String encodeJSON(T object) {
        return encodeJSON(object, "", "");
    }

    public <T> String encodeJSON(T object, String filterName, String... ignoredFields) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filter = new SimpleFilterProvider();

        if (ignoredFields!=null && ignoredFields.length > 0 &&
                filterName!=null && !filterName.isEmpty()) {
            filter.addFilter(filterName, SimpleBeanPropertyFilter.serializeAllExcept(ignoredFields));
        }
        else{
            filter.setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
        }

        mapper.setFilterProvider(filter);

        try {
            return(mapper.writeValueAsString(object));
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
        if(object == null){
            throw new IllegalArgumentException();
        }

        String[] filterStrings = null;
        String filter = null;
        if(object instanceof BaseObject){
            BaseObject baseObject = (BaseObject) object;
            filterStrings = baseObject.getIgnoredFields();
            filter = baseObject.getClass().getAnnotation(JsonFilter.class).value();
        }

        String json;

        if (withSig) {
            json = AppServiceProvider.getSerializationService().encodeJSON(object);
        } else {
            json = AppServiceProvider.getSerializationService().encodeJSON(object, filter, filterStrings);
        }
        return (Util.SHA3.digest(json.getBytes()));
    }

    @Override
    public String getHashString(Object object, boolean withSig) {
        return new String(Base64.encode(getHash(object, withSig)));
    }


}
