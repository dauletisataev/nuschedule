package com.example.admin.nuschedule.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.models.Lessons;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;
import java.util.Random;
import static com.example.admin.nuschedule.other.Constants.STUDENT_ID;
import static com.example.admin.nuschedule.other.Constants.TAG;
import static com.example.admin.nuschedule.other.Constants.dayOfWeek;

public class LessonRepository {
    private LessonModelDao mDao;
    public LessonRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mDao = db.lessonModelDao();
    }
    public LiveData<List<LessonModel>> getAllLessons() {
        return mDao.getAllLessons();
    }

    public void initialize(Context context){
        new asyncTask(context).execute();
    }
    private class asyncTask extends AsyncTask<Void, Void, Long> {
        long size;
        Context context;
        asyncTask(Context c){
            context = c;
        }
        @Override
        protected Long doInBackground(final Void... params) {
             size = mDao.getSize();
             return null;
        }

        @Override
        protected void onPostExecute(Long tableSize) {
            super.onPostExecute(tableSize);
            Log.d(TAG, "onPostExecute: "+tableSize);
            if(size == 0) loadLessonsFromFB(context);
            Log.d(TAG, "tableSize: " + size);
        }
    }
    private void loadLessonsFromFB(final Context context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("schedules/" + STUDENT_ID);
        final Lessons lessonHt = new Lessons();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    Log.d(TAG, "no data in firebase ");
                    return;
                }
                for (int i = 0; i < dayOfWeek.length; i++) {
                    int[] androidColors = context.getResources().getIntArray(R.array.mainColors);
                    DataSnapshot snapshot = dataSnapshot.child(dayOfWeek[i]);
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        LessonModel lesson = postSnapshot.getValue(LessonModel.class);
                        lesson.setDay(dayOfWeek[i]);
                        lesson.setRepeat(true);
                        lesson.setDate(0);
                        lesson.setHasTask(false);
                        long colorId =  androidColors[new Random().nextInt(androidColors.length)];
                        Log.d(TAG, "colorId: " + colorId);
                        lesson.setColor(colorId);
                        Log.d(TAG, "loading from fb: getColor" + lesson.getColor()+"  "+  lesson);
                       // if(lessonHt.alreadyInserted(lesson)) return;
                        lessonHt.addLesson(lesson);
                        new insertAsyncTask(lesson).execute();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    private class insertAsyncTask extends AsyncTask<Void, Void, Long> {
        long lesson_id;
        LessonModel lesson;

        insertAsyncTask(LessonModel lessonModel) {
            Log.d(TAG, "insertAsyncTask constructor: "+ lessonModel);
            lesson = lessonModel;
        }

        @Override
        protected Long doInBackground(final Void... params) {
            Log.d(TAG, "inserting lesson in bg: "+ lesson);
            lesson_id = insert(lesson);
            return lesson_id;
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: " + result);
        }
    }
    public LiveData<List<LessonModel>> getLessonsByDay(String day) {
        return mDao.getLessonsByDay(day);
    }

    public void delete(long id){
        mDao.deleteLesson(id);
    }

    public long insert (LessonModel lessonModel){
        return mDao.addLesson(lessonModel);
    }

    public void update(LessonModel ls){
        mDao.updateLesson(ls);
    }

    public List<Long> insertLessons (List<LessonModel> lessons){
        return mDao.addLessons(lessons);
    }

    public LiveData<LessonModel> getLessonById(long id){
        return mDao.getLessonbyId(id);
    }
}
