package com.example.timer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SessionDao {

    @Insert
    void insert(StudySession session);

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    List<StudySession> getAllSessions();

    @Query("SELECT SUM(duration) FROM sessions WHERE date = :date")
    Integer getTotalForDay(String date);

    @Query("SELECT SUM(duration) FROM sessions WHERE date BETWEEN :startDate AND :endDate")
    Integer getTotalForRange(String startDate, String endDate);

    @Query("SELECT SUM(duration) FROM sessions")
    Integer getTotalOverall();
}

