package network.elrond.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class SerializationServiceImpl implements SerializationService {


    @Override
    public <T> String encodeJSON(T object) {
        ObjectMapper mapper = new ObjectMapper();
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


}
