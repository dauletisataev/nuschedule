package com.example.admin.nuschedule.activities;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.database.DBHelper;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.example.admin.nuschedule.view.LessonEventView;
import com.example.admin.nuschedule.view.SquareImage;
import com.example.admin.nuschedule.viewModels.LessonViewModel;

import static com.example.admin.nuschedule.other.Constants.STUDENT_ID;
import static com.example.admin.nuschedule.other.Constants.TAG;

public class LessonDetailsActivity extends AppCompatActivity {
    DBHelper dbHelper;
    SQLiteDatabase SQLdatabase;
    long lessonId;
    Toolbar toolbar;
    TextView lesson_title, lesson_days, lesson_time, lesson_type, lesson_instructor, lesson_room;
    LessonViewModel lessonViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_details);
        lessonViewModel = ViewModelProviders.of(this).get(LessonViewModel.class);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.shadow_view).setVisibility(View.GONE);
        }
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lessonId = getIntent().getLongExtra("lessonId", -1);
        Log.d("mLog", "recieved ID: " + lessonId);

        initializeViews();
        lessonViewModel.getLessonById(lessonId).observe(this, new Observer<LessonModel>() {
            @Override
            public void onChanged(@Nullable LessonModel lessonModel) {
                if(lessonModel != null) setValues(lessonModel);
                else {
                    Toast.makeText(LessonDetailsActivity.this, "Lesson deleted", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

    }

    private void initializeViews(){
        lesson_title = (TextView) findViewById(R.id.lesson_title);
        lesson_days = (TextView) findViewById(R.id.lesson_days);
        lesson_time = (TextView) findViewById(R.id.lesson_time);
        lesson_type = (TextView) findViewById(R.id.lesson_type);
        lesson_instructor = (TextView) findViewById(R.id.lesson_instructor);
        lesson_room = (TextView) findViewById(R.id.lesson_room);
    }

    private void setValues(LessonModel lesson){
       getSupportActionBar().setTitle(lesson.getTitle());
       toolbar.setBackgroundColor((int)lesson.getColor());
       lesson_title.setText(lesson.getTitle());
       lesson_days.setText(lesson.getDay());
       lesson_time.setText(lesson.getStartTime()+" - "+lesson.getEndTime());
       lesson_type.setText(lesson.getType());
       lesson_instructor.setText(lesson.getInstructor());
       lesson_room.setText(lesson.getRoom());
       changeTintToColor((int) lesson.getColor());
    }

    private void changeTintToColor(int tintColor){
        ((SquareImage) findViewById(R.id.img_title)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_day)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_time)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_info)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_person)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_place)).setColorFilter(tintColor);
        toolbar.setBackgroundColor(tintColor);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lesson_details, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }
        if (id == R.id.edit_btn)
            goToEdit();
        if (id == R.id.delete_button)
            new deleteAsyncTask(lessonId).execute();

        return super.onOptionsItemSelected(item);
    }
    private void goToEdit(){
        Intent intent = new Intent(this, EditLessonActivity.class);
        intent.putExtra("lessonId", lessonId);
        startActivityForResult(intent, 3);
    }
    private class deleteAsyncTask extends AsyncTask<Void, Void, Long> {
        long lesson_id;

        deleteAsyncTask(long id) {
            lesson_id = id;
        }


        @Override
        protected Long doInBackground(Void... voids) {
            lessonViewModel.delete(lessonId);
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: deleted" +lesson_id);
            Intent intent = new Intent();
            intent.putExtra("btnType", 0);
            intent.putExtra("day", lesson_days.getText());
            intent.putExtra("resultId", lesson_id);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

}
