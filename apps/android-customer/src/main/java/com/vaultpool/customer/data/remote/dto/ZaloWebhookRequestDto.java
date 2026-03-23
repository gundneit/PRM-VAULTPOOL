package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ZaloWebhookRequestDto {
    @SerializedName("data")
    private String data;

    @SerializedName("mac")
    private String mac;

    @SerializedName("type")
    private int type;

    public ZaloWebhookRequestDto(String data, String mac, int type) {
        this.data = data;
        this.mac = mac;
        this.type = type;
    }

    // Getters and Setters
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
}
