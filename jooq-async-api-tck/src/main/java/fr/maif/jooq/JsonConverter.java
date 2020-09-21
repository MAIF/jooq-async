package fr.maif.jooq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.NullNode;
import org.jooq.Converter;
import org.jooq.JSONB;

import java.io.IOException;

public class JsonConverter implements Converter<JSONB, JsonNode> {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static ObjectWriter writer = mapper.writerFor(JsonNode.class);
    @Override
    public JsonNode from(JSONB databaseObject) {

        if (databaseObject != null && databaseObject.data() != null) {
            try {
                return mapper.readTree(databaseObject.data());
            } catch (IOException e) {
                throw new RuntimeException("Error reading json"+databaseObject.data(), e);
            }
        } else {
            return NullNode.getInstance();
        }
    }

    @Override
    public JSONB to(JsonNode userObject) {
        try {
            return JSONB.valueOf(writer.writeValueAsString(userObject));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing json"+userObject, e);
        }
    }

    @Override
    public Class<JSONB> fromType() {
        return JSONB.class;
    }

    @Override
    public Class<JsonNode> toType() {
        return JsonNode.class;
    }
}
