package com.example.casper.Experiment2024.model;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckInRepository {

    private CheckInDao checkInDao;
    private LiveData<List<CheckIn>> allCheckIns;

    private ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public CheckInRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        checkInDao = database.checkInDao();
        allCheckIns = checkInDao.getAllCheckIns();
    }

    public void insert(CheckIn checkIn) {
        databaseWriteExecutor.execute(() -> checkInDao.insert(checkIn));
    }

    public LiveData<List<CheckIn>> getAllCheckIns() {
        return allCheckIns;
    }
    
}