package com.example.admin.nuschedule.models;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.room_model.LessonModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Lenovo on 06.02.2018.
 */
public class Lessons {
    Utils utils = new Utils();
    Hashtable<String, Hashtable<Long, LessonModel>> lessons;
    int size;

    public Lessons(){
        lessons = new Hashtable<>();
        size = 0;
    }



    public void addLesson(LessonModel lesson){
        if(lessons.get(lesson.getDay()) == null) lessons.put(lesson.getDay(), new Hashtable<Long, LessonModel>());
        lessons.get(lesson.getDay()).put(lesson.getId(), lesson);
        size++;
    }
    public boolean alreadyInserted(LessonModel ls){
        if(lessons.get(ls.getDay()) == null) return false;
        Log.d(TAG, "alreadyInserted: got lesson: "+ ls);
        for(Hashtable<Long, LessonModel> oneDay: lessons.values())
        for(LessonModel lesson : oneDay.values()){
            Log.d(TAG, "alreadyInserted: comparing with lesson: "+ lesson);
            if(lesson.getTitle().equals(ls.getTitle()) && lesson.getType().equals(ls.getType())) {
                Log.d(TAG, "alreadyInserted: are equal");
                return true;
            }
        }
        return false;
    }
    public boolean doesExist(String day, LessonModel ls){
        if(lessons.get(day) == null) return false;
        return lessons.get(day).get(ls.getId()) != null;
    }
    public Hashtable<Long, LessonModel> get(String day){
        return lessons.get(day);
    }

    public int size(){
        return size;
    }

    @Override
    public String toString() {
        String result="";
        return result;
    }
    public LessonModel intersect(String day, int lessonStr){
        Log.d("mLog", "got lessonStr: "+lessonStr+" day: "+day);
        if(lessons.get(day) == null) {
            Log.d("mLog", "hashmap for "+day+" is null");
            return null;
        }
        for(LessonModel ls : lessons.get(day).values()){
            int lsStrTime =  utils.getMinutes(ls.getStartTime());
            int lsEndTime =  utils.getMinutes(ls.getEndTime());
            Log.d("mLog", "Comparing: "+lsStrTime+" <> "+lessonStr+" <> "+lsEndTime);
            if( lessonStr >= lsStrTime && lessonStr <= lsEndTime){
                return ls;
            }
        }
        return null;
    }

    public boolean deleteLesson(String day, long lessonId){
        if(lessons.get(day) == null) {
            return false;
        }
        if(lessons.get(day).remove(lessonId) != null) return true;
        return false;
    }
}