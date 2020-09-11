package com.example.lagerapp;

public class ListObject {
    private String ean;
    private String article;
    private String name;

    public ListObject(String ean, String article, String name) {
        this.ean = ean;
        this.article = article;
        this.name = name;
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
}
