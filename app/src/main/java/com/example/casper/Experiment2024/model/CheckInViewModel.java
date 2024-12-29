package com.example.casper.Experiment2024.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class CheckInViewModel extends AndroidViewModel {

    private CheckInRepository repository;
    private LiveData<List<CheckIn>> allCheckIns;

    public CheckInViewModel(@NonNull Application application) {
        super(application);
        repository = new CheckInRepository(application);
        allCheckIns = repository.getAllCheckIns();
    }

    public void insert(CheckIn checkIn) {
        repository.insert(checkIn);
    }

    public LiveData<List<CheckIn>> getAllCheckIns() {
        return allCheckIns;
    }
    public void insertWithLocation(String description, double latitude, double longitude) {
        CheckIn checkIn = new CheckIn(description, latitude, longitude);
        repository.insert(checkIn);
    }

}