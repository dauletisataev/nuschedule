package com.example.admin.nuschedule.activities;

import android.app.Activity;
import android.app.TimePickerDialog;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.database.DBHelper;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.example.admin.nuschedule.view.SquareImage;
import com.example.admin.nuschedule.viewModels.LessonViewModel;

import java.util.ArrayList;
import java.util.Calendar;

import petrov.kristiyan.colorpicker.ColorPicker;

import static com.example.admin.nuschedule.other.Constants.STUDENT_ID;
import static com.example.admin.nuschedule.other.Constants.TAG;

public class EditLessonActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    TextView startTime, endTime;
    SquareImage colorPickerBtn;
    EditText mTitle, mDescription, mType, mInstructor, mRoom;
    RadioGroup radioGroup;
    LessonModel lesson;
    long lessonId;
    DBHelper dbHelper;
    SQLiteDatabase SQLdatabase;
    ArrayList<String> daysChecked;
    Utils utils = new Utils();
    LessonViewModel lessonViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lesson);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("New lesson");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        Log.d("mLog", "Build SDK version: "+ android.os.Build.VERSION.SDK_INT);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.shadow_view).setVisibility(View.GONE);
        }
        lessonId = getIntent().getLongExtra("lessonId", -1);
        Log.d("mLog", "recieved ID: "+ lessonId);
        initializeViews();
        lessonViewModel = ViewModelProviders.of(this).get(LessonViewModel.class);
        lessonViewModel.getLessonById(lessonId).observe(this, new Observer<LessonModel>() {
            @Override
            public void onChanged(@Nullable LessonModel lessonModel) {
                if(lessonModel != null) {
                    lesson = lessonModel;
                    setValues(lessonModel);
                }
                else {
                    Toast.makeText(EditLessonActivity.this, "Lesson null", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Calendar mcurrentTime;
        int hour, minute;
        TimePickerDialog mTimePicker;

        switch (v.getId()) {
            case R.id.startTime:
                mcurrentTime = Calendar.getInstance();
                hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                minute = mcurrentTime.get(Calendar.MINUTE);
                mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time = utils.getTimeType(selectedHour, selectedMinute);
                        startTime.setText(time);
                        lesson.setStartTime(time);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
                break;
            case R.id.endTime:
                mcurrentTime = Calendar.getInstance();
                hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                minute = mcurrentTime.get(Calendar.MINUTE);
                mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time = utils.getTimeType(selectedHour, selectedMinute);
                        endTime.setText(time);
                        lesson.setEndTime(time);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
                break;
            case R.id.color_picker_btn:
                ColorPicker colorPicker = new ColorPicker(EditLessonActivity.this);
                colorPicker.setColors(getResources().getIntArray(R.array.mainColors));
                colorPicker.show();
                colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position,int mColor) {
                        lesson.setColor( mColor);
                        changeTintToColor(mColor);
                    }

                    @Override
                    public void onCancel(){
                        // put code
                    }
                });
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }
        if (id == R.id.save_button) {
            if (isFilledCorrect()){
                Log.d(TAG, "selected day: "+((RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId())).getTag().toString());
                lesson.setDay(((RadioButton)radioGroup.findViewById(radioGroup.getCheckedRadioButtonId())).getTag().toString());
                lesson.setTitle(mTitle.getText().toString());
                lesson.setDescription(mDescription.getText().toString());
                lesson.setType(mType.getText().toString());
                lesson.setInstructor(mInstructor.getText().toString());
                lesson.setRoom(mRoom.getText().toString());
                new updateAsyncTask(lesson).execute();
            }

        }
        return super.onOptionsItemSelected(item);
    }
    private class updateAsyncTask extends AsyncTask<Void, Void, Void> {
        LessonModel lesson;

        updateAsyncTask(LessonModel ls) {
            lesson = ls;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            lessonViewModel.updateLesson(lesson);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: updated" );
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

    }
    private void initializeViews(){
        startTime = (TextView) findViewById(R.id.startTime);
        endTime = (TextView) findViewById(R.id.endTime);

        mTitle = (EditText) findViewById(R.id.lesson_title);
        mDescription = (EditText) findViewById(R.id.lesson_description);
        mType = (EditText) findViewById(R.id.lesson_type);
        mInstructor = (EditText) findViewById(R.id.lesson_instructor);
        mRoom = (EditText) findViewById(R.id.lesson_room);
        radioGroup = (RadioGroup)  findViewById(R.id.radio_group);
        colorPickerBtn = (SquareImage) findViewById(R.id.color_picker_btn);
    }

    private void setValues(final LessonModel lesson){
        getSupportActionBar().setTitle(lesson.getTitle());
        toolbar.setBackgroundColor((int)lesson.getColor());
        changeTintToColor((int)lesson.getColor());

        startTime.setText(lesson.getStartTime());
        startTime.setOnClickListener(this);
        endTime.setText(lesson.getEndTime());
        endTime.setOnClickListener(this);
        colorPickerBtn.setOnClickListener(this);
        mTitle.setText(lesson.getTitle());
        mDescription.setText(lesson.getDescription());
        mType.setText(lesson.getType());
        mInstructor.setText(lesson.getInstructor());
        mRoom.setText(lesson.getRoom());
        radioGroup.check((radioGroup.getChildAt(lesson.getDayIndex())).getId());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
        lesson.setDay((String) ((RadioButton) findViewById(checkedId)).getTag());
            }
        });
    }

    private void changeTintToColor(int tintColor){
        Log.d("mLog", "changeTintToColor: " + tintColor);
        ((SquareImage) findViewById(R.id.color_picker_btn)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_info)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_person)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_place)).setColorFilter(tintColor);
        ((TextView) findViewById(R.id.choose_txt)).setTextColor(tintColor);
        toolbar.setBackgroundColor(tintColor);

    }
    private boolean isFilledCorrect(){

        if( mTitle.getText().toString().trim().length() == 0 || mRoom.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "Please, fill the required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if( utils.getMinutes(startTime.getText().toString()) < 540 || utils.getMinutes(startTime.getText().toString())>1260
                || utils.getMinutes(endTime.getText().toString()) < 540 || utils.getMinutes(endTime.getText().toString())>1260) {
            Toast.makeText(this, "The time chosen is out of allowed scope (9:00 AM to 9:00 PM)", Toast.LENGTH_SHORT).show();
            return false;
        }
        if( utils.getMinutes(startTime.getText().toString()) >= utils.getMinutes(endTime.getText().toString()) ){
            Toast.makeText(this, "The time chosen does not follow logical order", Toast.LENGTH_SHORT).show();
            return false;
        }
        if( (utils.getMinutes(endTime.getText().toString()) - utils.getMinutes(startTime.getText().toString()) ) <15){
            Toast.makeText(this, "The chosen time interval is too short", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
