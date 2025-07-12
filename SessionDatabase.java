package com.example.timer;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {StudySession.class}, version = 4)
public abstract class SessionDatabase extends RoomDatabase {
    public abstract SessionDao sessionDao();

    private static SessionDatabase INSTANCE;

    public static SessionDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SessionDatabase.class, "session_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
