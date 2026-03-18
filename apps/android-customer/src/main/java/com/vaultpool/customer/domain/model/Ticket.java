package com.vaultpool.customer.domain.model;

public class Ticket {
    private String title;
    private String description;
    private String price;
    private String flashSaleText;
    private String imageUrl;

    public Ticket(String title, String description, String price, String flashSaleText, String imageUrl) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.flashSaleText = flashSaleText;
        this.imageUrl = imageUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getFlashSaleText() { return flashSaleText; }
    public String getImageUrl() { return imageUrl; }
}
