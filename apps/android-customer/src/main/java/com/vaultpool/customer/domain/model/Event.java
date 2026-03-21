package com.vaultpool.customer.domain.model;

public class Event {
    private String title;
    private String imageUrl;

    public Event(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}
