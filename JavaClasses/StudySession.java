package com.example.timer;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "sessions")
public class StudySession {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String date;

    private int duration; // in minutes

    @NonNull
    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(@NonNull final String startTime) {
        this.startTime = startTime;
    }

    @NonNull
    private String startTime;  // e.g., "14:30"

    @NonNull
    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(@NonNull final String endTime) {
        this.endTime = endTime;
    }

    @NonNull
    private String endTime;    // e.g., "14:50"

    public StudySession(@NonNull String date, int duration, @NonNull String startTime, @NonNull String endTime) {
        this.date = date;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
