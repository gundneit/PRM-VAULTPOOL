package com.vaultpool.customer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CartCountDto {
    @SerializedName("count")
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
