package com.lavela.pool.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

/**
 * ZaloPay callback payload (Order callback, type=1).
 * Doc: callback data includes `data` (JSON string), `mac` (HMAC over data using key2), `type`.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZaloPayCallbackRequest {
    /**
     * JSON string representing order callback data (ZaloPay wire format).
     * Deserializer also accepts a JSON object in Swagger for convenience.
     */
    @JsonDeserialize(using = ZaloPayCallbackDataDeserializer.class)
    @JsonAlias({ "DATA" })
    private String data;

    /**
     * HMAC hex string (64 hex chars; spaces/newlines are stripped by the server).
     */
    @JsonAlias({ "MAC" })
    private String mac;

    /**
     * type=1: Order
     */
    private Integer type;
}

