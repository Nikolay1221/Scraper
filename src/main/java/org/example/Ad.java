package org.example;

import org.bson.Document;

public class Ad {
    private String title;
    private String description;
    private int price;
    private String url;

    public Ad(String title, String description, int price, String url) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.url = url;
    }

    public Document toDocument() {
        return new Document("title", title)
                .append("description", description)
                .append("price", price)
                .append("url", url);
    }

    public static Ad fromDocument(Document document) {
        String title = document.getString("title");
        String description = document.getString("description");
        int price = document.getInteger("price");
        String url = document.getString("url");

        return new Ad(title, description, price, url);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}