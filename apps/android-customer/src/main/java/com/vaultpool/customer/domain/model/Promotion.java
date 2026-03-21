package com.vaultpool.customer.domain.model;

public class Promotion {
    private String title;
    private String imageUrl;

    public Promotion(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}
