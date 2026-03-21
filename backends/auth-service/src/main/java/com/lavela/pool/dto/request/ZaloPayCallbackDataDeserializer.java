package com.lavela.pool.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * ZaloPay spec sends {@code data} as a JSON <strong>string</strong>. Swagger users often paste a nested
 * object instead, which breaks strict {@code String} binding and causes
 * {@code HttpMessageNotReadableException}. Accept both: string as-is, object/array serialized to compact JSON.
 */
public class ZaloPayCallbackDataDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        return switch (p.currentToken()) {
            case VALUE_STRING -> p.getValueAsString();
            case START_OBJECT, START_ARRAY -> {
                JsonNode node = mapper.readTree(p);
                yield mapper.writeValueAsString(node);
            }
            case VALUE_NULL -> null;
            default -> throw ctxt.wrongTokenException(p, JsonToken.VALUE_STRING,
                    "ZaloPay callback `data` must be a JSON string or object");
        };
    }
}
