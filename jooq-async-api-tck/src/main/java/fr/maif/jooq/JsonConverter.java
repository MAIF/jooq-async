package fr.maif.jooq;

import org.jooq.Converter;
import org.jooq.JSONB;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.node.NullNode;

public class JsonConverter implements Converter<JSONB, JsonNode> {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static ObjectWriter writer = mapper.writerFor(JsonNode.class);

    @Override
    public JsonNode from(JSONB databaseObject) {

        if (databaseObject != null && databaseObject.data() != null) {
            return mapper.readTree(databaseObject.data());
        } else {
            return NullNode.getInstance();
        }
    }

    @Override
    public JSONB to(JsonNode userObject) {
        return JSONB.valueOf(writer.writeValueAsString(userObject));
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
