package com.example.casper.Experiment2024; // 使用你实际的包名

public class Book {
    private String title; // 书名
    private int coverResourceId; // 封面图片资源ID

    public Book(String title, int coverResourceId) {
        this.title = title;
        this.coverResourceId = coverResourceId;
    }

    // 返回书名
    public String getTitle() {
        return title;
    }

    // 返回封面资源ID
    public int getCoverResourceId() {
        return coverResourceId;
    }
}
