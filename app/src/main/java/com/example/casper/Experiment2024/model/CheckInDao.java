package com.example.casper.Experiment2024.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CheckInDao {

    @Insert
    void insert(CheckIn checkIn);

    @Query("SELECT * FROM check_in_table ORDER BY id DESC")
    LiveData<List<CheckIn>> getAllCheckIns();
}