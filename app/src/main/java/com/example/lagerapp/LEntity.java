package com.example.lagerapp;

public class LEntity {
    private String ean;
    private String article;
    private String name;
    private int amount;
    private String location;

    public LEntity(String ean, String article, String name, int amount, String location) {
        this.ean = ean;
        this.article = article;
        this.name = name;
        this.amount = amount;
        this.location = location;
    }

    public String getEan() {
        return ean;
    }

    public String getArticle() {
        return article;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public String getLocation() {
        return location;
    }
}
