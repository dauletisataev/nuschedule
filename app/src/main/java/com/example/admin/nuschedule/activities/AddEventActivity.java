package com.example.admin.nuschedule.activities;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import petrov.kristiyan.colorpicker.ColorPicker;

import static com.example.admin.nuschedule.other.Constants.STUDENT_ID;
import static com.example.admin.nuschedule.other.Constants.TAG;

public class AddEventActivity extends AppCompatActivity implements View.OnClickListener{
    Utils utils;
    TextView startTime, endTime;
    EditText mTitle, mDescription, mType, mInstructor, mRoom;
    HashMap<String, Object> result;
    SquareImage colorPickerBtn;
    DBHelper dbHelper;
    SQLiteDatabase SQLdatabase;
    String day = "Monday";
    int lesson_color = Color.parseColor("#00aff0");
    ArrayList<String> daysChecked = new ArrayList<>();
    Toolbar toolbar;
    LessonViewModel lessonViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        lessonViewModel = ViewModelProviders.of(this).get(LessonViewModel.class);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("New lesson");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            findViewById(R.id.shadow_view).setVisibility(View.GONE);
        }

        changeTintToColor(lesson_color);
        String startT = getIntent().getStringExtra("START_TIME");
        String endT = getIntent().getStringExtra("END_TIME");
        day = getIntent().getStringExtra("DAY");
        CheckBox checkBox = ((LinearLayout) findViewById(R.id.checkbox_layout)).findViewWithTag(day);
        checkBox.setChecked(true);

        startTime = (TextView) findViewById(R.id.startTime);
        endTime = (TextView) findViewById(R.id.endTime);

        mTitle = (EditText) findViewById(R.id.lesson_title);
        mDescription = (EditText) findViewById(R.id.lesson_description);
        mType = (EditText) findViewById(R.id.lesson_type);
        mInstructor = (EditText) findViewById(R.id.lesson_instructor);
        mRoom = (EditText) findViewById(R.id.lesson_room);

        colorPickerBtn = (SquareImage) findViewById(R.id.color_picker_btn);
        colorPickerBtn.setOnClickListener(this);

        startTime.setOnClickListener(this);
        startTime.setText(startT);
        endTime.setOnClickListener(this);
        endTime.setText(endT);

        result = new HashMap<>();
        result.put("startTime", startT);
        result.put("endTime", endT);

        utils = new Utils();


    }
    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }
        if (id == R.id.save_button) {
            if (isFilledCorrect()) {
                getCheckedDays();
                List<LessonModel> lessons = new ArrayList<>();
                for (int i = 0; i < daysChecked.size(); i++) {
                    LessonModel lesson = new LessonModel(
                            mTitle.getText().toString(),
                            startTime.getText().toString(),
                            endTime.getText().toString(),
                            mInstructor.getText().toString(),
                            mRoom.getText().toString(),
                            daysChecked.get(i),
                            lesson_color,
                            mType.getText().toString(),
                            mDescription.getText().toString()
                    );
                    lessons.add(lesson);
                    //Log.d("mLog", "inserted id: "+insertedId);
                    //insertedIds.add((int) insertedId);
                }
                new insertAsyncTask(lessons).execute(lessons);



            }
        }
        return super.onOptionsItemSelected(item);
    }
    private class insertAsyncTask extends AsyncTask<List<LessonModel>, Void, List<Long>> {
        List<Long> lesson_ids;
        ArrayList<Integer> lesson_ids_int = new ArrayList<>();
        List<LessonModel> lessons;

        insertAsyncTask(List<LessonModel> lessons) {
            this.lessons = lessons;
        }

        @Override
        protected List<Long>  doInBackground(final List<LessonModel>... params) {
            lesson_ids = lessonViewModel.insertLessons(params[0]);
            return lesson_ids;
        }

        @Override
        protected void onPostExecute(List<Long> result) {
            super.onPostExecute(result);
            for(long i : lesson_ids) {
                Log.d(TAG, "onPostExecute id: "+i);
                lesson_ids_int.add((int) i);
            }
            Log.d(TAG, "onPostExecute: " + result);
            Intent returnIntent = new Intent();
            returnIntent.putIntegerArrayListExtra("resultIds", lesson_ids_int );
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
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
                        result.put("startTime", time);
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
                        result.put("endTime", time);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
                break;
            case R.id.color_picker_btn:
                ColorPicker colorPicker = new ColorPicker(AddEventActivity.this);
                colorPicker.setColors(getResources().getIntArray(R.array.mainColors));
                colorPicker.show();
                colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position,int mColor) {
                        lesson_color = mColor;
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
    private void changeTintToColor(int tintColor){
        Log.d("mLog", "changeTintToColor: " + tintColor);
        ((SquareImage) findViewById(R.id.color_picker_btn)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_info)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_person)).setColorFilter(tintColor);
        ((SquareImage) findViewById(R.id.img_place)).setColorFilter(tintColor);
        ((TextView) findViewById(R.id.choose_txt)).setTextColor(tintColor);
        toolbar.setBackgroundColor(tintColor);

    }
    private void getCheckedDays(){
        if(((CheckBox) findViewById(R.id.checkbox_monday)).isChecked()) daysChecked.add((String) ((CheckBox) findViewById(R.id.checkbox_monday)).getTag());
        if(((CheckBox) findViewById(R.id.checkbox_tuesday)).isChecked()) daysChecked.add((String) ((CheckBox) findViewById(R.id.checkbox_tuesday)).getTag());
        if(((CheckBox) findViewById(R.id.checkbox_wednesday)).isChecked()) daysChecked.add((String) ((CheckBox) findViewById(R.id.checkbox_wednesday)).getTag());
        if(((CheckBox) findViewById(R.id.checkbox_thursday)).isChecked()) daysChecked.add((String) ((CheckBox) findViewById(R.id.checkbox_thursday)).getTag());
        if(((CheckBox) findViewById(R.id.checkbox_friday)).isChecked()) daysChecked.add((String) ((CheckBox) findViewById(R.id.checkbox_friday)).getTag());
        Log.d("mLog", "getCheckedDays: "+daysChecked.toString());
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
