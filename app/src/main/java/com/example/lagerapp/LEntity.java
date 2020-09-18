package com.example.lagerapp;

public class LEntity {
    private String ean;
    private String article;
    private String name;
    private int amount;
    private String location;

    public void setEan(String ean) {
        this.ean = ean;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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
