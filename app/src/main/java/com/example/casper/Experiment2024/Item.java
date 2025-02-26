package com.example.casper.Experiment2024;

import java.io.Serializable;

public class Item implements Serializable {
    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getResourceId() {
        return resourceId;
    }

    private final String title;
    private final double price;
    private final int resourceId;

    public Item(String itemTitle, double itemPrice, int itemPictureResourceId) {
        this.title=itemTitle;
        this.price=itemPrice;
        this.resourceId=itemPictureResourceId;
    }
}
