package com.vaultpool.customer.domain.model;

public class Pool {
    private String name;
    private String location;
    private String price;
    private float rating;
    private String imageUrl;

    public Pool(String name, String location, String price, float rating, String imageUrl) {
        this.name = name;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public float getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
}
