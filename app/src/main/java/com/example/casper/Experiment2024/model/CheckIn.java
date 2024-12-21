package com.example.casper.Experiment2024.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "check_in_table")
public class CheckIn {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private double latitude;
    private double longitude;
    private String description;
    private String imagePath; // 存储照片路径

    // 默认构造函数（Room需要）
    public CheckIn() {}

    // 构造函数，用于插入新的打卡记录
    public CheckIn(String description) {
        this.description = description;
        Date timestamp = new Date(); // 自动记录当前时间
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}