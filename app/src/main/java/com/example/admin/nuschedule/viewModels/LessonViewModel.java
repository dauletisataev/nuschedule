package com.example.admin.nuschedule.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.example.admin.nuschedule.database.AppDatabase;
import com.example.admin.nuschedule.database.LessonRepository;
import com.example.admin.nuschedule.room_model.LessonModel;

import java.util.List;

public class LessonViewModel extends AndroidViewModel {
    private LessonRepository mRepository;

    private AppDatabase appDatabase;

    public LessonViewModel(Application application) {
        super(application);
        mRepository = new LessonRepository(application);

    }
    public void initialize(Context context){ mRepository.initialize(context); }
    public LiveData<List<LessonModel>> getAllLessons() { return mRepository.getAllLessons(); }
    public LiveData<List<LessonModel>> getLessonsByDay(String day) { return mRepository.getLessonsByDay(day); }
    public LiveData<LessonModel> getLessonById(long id){
        return mRepository.getLessonById(id);
    }
    public long insert(LessonModel lessonModel) {
        return mRepository.insert(lessonModel);
    }
    public void delete(long id){
        mRepository.delete(id);
    }
    public void updateLesson(LessonModel ls){
        mRepository.update(ls);
    }
    public List<Long> insertLessons(List<LessonModel> lessons) {
        return mRepository.insertLessons(lessons);
    }

}