package com.example.admin.nuschedule.database;

import android.os.AsyncTask;

import com.example.admin.nuschedule.database.AppDatabase;
import com.example.admin.nuschedule.database.LessonModelDao;
import com.example.admin.nuschedule.room_model.LessonModel;

import java.util.List;

public class PopulateDbAsync  extends AsyncTask<Void, Void, Void> {

    private final LessonModelDao mDao;
    private final List<LessonModel> lessons;

    public PopulateDbAsync(AppDatabase db, List<LessonModel>lessons) {
        mDao = db.lessonModelDao();
        this.lessons =  lessons;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        for(LessonModel lesson : lessons)
            mDao.addLesson(lesson);
        return null;
    }
}