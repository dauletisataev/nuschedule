package com.example.admin.nuschedule.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.admin.nuschedule.room_model.LessonModel;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface LessonModelDao {

    @Query("select * from lesson")
    LiveData<List<LessonModel>> getAllLessons();

    @Query("select * from lesson where id = :id")
    LiveData<LessonModel> getLessonbyId(long id);

    @Query("select * from lesson where day = :day")
    LiveData<List<LessonModel>> getLessonsByDay(String day);
    @Insert
    long addLesson(LessonModel lessonModel);
    @Insert
    List<Long> addLessons(List<LessonModel> lessons);

    @Update(onConflict = REPLACE)
    void updateLesson(LessonModel lesson);

    @Query("DELETE FROM lesson WHERE id = :id")
    void deleteLesson(long id);

    @Query("SELECT COUNT(*) FROM lesson")
    long getSize();
}
