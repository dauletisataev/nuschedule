package com.example.admin.nuschedule.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.admin.nuschedule.room_model.LessonModel;

@Database(entities = {LessonModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LessonModelDao lessonModelDao();
    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "nuschedule")
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

}