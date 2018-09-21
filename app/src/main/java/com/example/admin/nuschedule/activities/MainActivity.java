package com.example.admin.nuschedule.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.database.DBHelper;
import com.example.admin.nuschedule.reciever.NotificationReciever;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.view.LessonEventView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static com.example.admin.nuschedule.other.Constants.MY_PREFS_NAME;
import static com.example.admin.nuschedule.other.Constants.STUDENT_ID;
import static com.example.admin.nuschedule.other.Constants.dayOfWeek;
public class MainActivity extends AppCompatActivity{

    RelativeLayout currentRelativeLayout;
    private int[] touchDownXY = new int[2];
    Utils utils;
    DBHelper dbHelper;
    SQLiteDatabase SQLdatabase;
    int[] androidColors;
    Toolbar toolbar;
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        utils = new Utils();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        androidColors = getResources().getIntArray(R.array.mainColors);
        String textViewId = dayOfWeek[utils.getTodayIndex()].toLowerCase()+"TextView";
        int txtId = getResources().getIdentifier(textViewId, "id", getPackageName());
        ((TextView) findViewById(txtId)).setTextColor(getResources().getColor(R.color.black_de));
    }
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        final MenuItem notifyBtn = menu.findItem(R.id.notify_btn);
        final MenuItem silenceBtn = menu.findItem(R.id.silence_btn);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Boolean notify_mode = prefs.getBoolean("notify_mode", true);
        if (!prefs.contains("notify_mode")) {
             notifyBtn.setVisible(false);
             silenceBtn.setVisible(true);
        } else{
            if(!notify_mode) {
                notifyBtn.setVisible(true);
                silenceBtn.setVisible(false);
            }else {
                notifyBtn.setVisible(false);
                silenceBtn.setVisible(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }
        if (id == R.id.new_btn) {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            intent.putExtra("START_TIME", "9:00 AM");
            intent.putExtra("END_TIME", "10:00 AM");
            intent.putExtra("COLOR", androidColors[new Random().nextInt(androidColors.length)] );
            intent.putExtra("DAY", dayOfWeek[utils.getTodayIndex()]);
            Log.d("mLog", "send day: "+dayOfWeek[utils.getTodayIndex()]);
            startActivityForResult(intent, 1);
        }
        if(id == R.id.notify_btn){
            utils.showNotif(MainActivity.this);
            notificationOn();
        }
        if(id == R.id.silence_btn){
            notificationOff();

            Toast.makeText(this, "Notification is off", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private  void importLessonsFromFB(){
        String ID = STUDENT_ID;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("schedules/"+ID);


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // This method is called once with the initial value and again
                // whenever data at this location is updated
                days = new ArrayList<>();
                //Toast.makeText(MainActivity.this, "got data", Toast.LENGTH_SHORT).show();
                if(dataSnapshot.getChildrenCount() == 0){
                    Toast.makeText( getApplicationContext()  , "ID does not exist", Toast.LENGTH_SHORT).show();
                    return;
                }
                int id = Integer.valueOf(dataSnapshot.getKey());

                //initializeDB(STUDENT_ID);
                initializeDB(STUDENT_ID);
                for (int i=0; i<dayOfWeek.length; i++) {
                    DataSnapshot snapshot = dataSnapshot.child(dayOfWeek[i]);

                    List<Lesson> dayLessons =  new ArrayList<>();
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        Lesson lesson = postSnapshot.getValue(Lesson.class);
                        lesson.setDay(dayOfWeek[i]);
                        int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
                        lesson.setColor(randomAndroidColor);
                        int lessonId = addToDatabase(lesson);
                        lesson.setId(lessonId);
                        dayLessons.add(lesson);
                    }
                    //Day day = new Day(snapshot.getKey(), dayLessons);
                    Log.d("lessons in ONE DAY", dayLessons.toString());
                    days.add( day);
                }


                addLessonViews(days);
                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                Boolean notify_mode = prefs.getBoolean("notify_mode", true);
                if (!prefs.contains("notify_mode")) {
                    notificationOn();
                }
             }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    private  void importLessonsFromDB(){
        //initializeDB(STUDENT_ID);
        Cursor cursor = SQLdatabase.query(DBHelper.TABLE_SCHEDULE_ID, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            days = new ArrayList<>();
            days.add(new Day("Monday"));days.add(new Day("Tuesday"));days.add(new Day("Wednesday"));days.add(new Day("Thursday"));days.add(new Day("Friday"));
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int dayIndex = cursor.getColumnIndex(DBHelper.KEY_DAY);
            int startIndex = cursor.getColumnIndex(DBHelper.KEY_STARTTIME);
            int endIndex = cursor.getColumnIndex(DBHelper.KEY_ENDTIME);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int roomIndex = cursor.getColumnIndex(DBHelper.KEY_ROOM);
            int typeIndex = cursor.getColumnIndex(DBHelper.KEY_TYPE);
            int instructorIndex = cursor.getColumnIndex(DBHelper.KEY_INSTRUCTOR);
            int colorIndex = cursor.getColumnIndex(DBHelper.KEY_COLOR);

            do {
                Log.d("mLog", "_id= "+cursor.getInt(idIndex)+", day = " + cursor.getString(dayIndex) +
                        ", startTime = " + cursor.getString(startIndex) +
                        ", title = " + cursor.getString(titleIndex)+
                        ", endTime = " + cursor.getString(endIndex));
                int index = Arrays.asList(dayOfWeek).indexOf(cursor.getString(dayIndex));
                Lesson lesson = new Lesson(cursor.getString(titleIndex), cursor.getString(startIndex),cursor.getString(endIndex),cursor.getString(instructorIndex),cursor.getString(roomIndex),cursor.getString(dayIndex), cursor.getInt(colorIndex));
                lesson.setId(cursor.getInt(idIndex));
                lesson.setType(cursor.getString(typeIndex));
                days.get(index).addLesson(lesson);

            } while (cursor.moveToNext());
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            Boolean notify_mode = prefs.getBoolean("notify_mode", true);
            if (!prefs.contains("notify_mode")) {
                notificationOn();
            }

            addLessonViews(days);

        } else
            Log.d("mLog","0 rows");

        cursor.close();
        dbHelper.close();
    }
    private void addLessonViews(List<Day> days){
        for(Day day : days){
            for(final Lesson lesson : day.getLessons()){
                //Toast.makeText(MainActivity.this, "adding views", Toast.LENGTH_SHORT).show();

                String layoutId = day.getName().toLowerCase()+"RelativeLayout";
                int relativeId = getResources().getIdentifier(layoutId, "id", getPackageName());
                currentRelativeLayout = (RelativeLayout) findViewById(relativeId);
                final LessonEventView newLesson = new LessonEventView(getApplicationContext(), null);
                //Log.d(TAG, "addLessonViews: "+lesson);
                newLesson.setNameText(lesson.getTitle());
                newLesson.setTag(lesson.getId());
                newLesson.setRoomText(lesson.getRoom());
                newLesson.setBackgroundColor(lesson.getColor());
                newLesson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), LessonDetailsActivity.class);
                        intent.putExtra("lessonId", lesson.getId());
                        Log.d("mLog", "send ID: "+ lesson.getId());
                        startActivityForResult(intent, 2);

                    }
                });
                int startTimeInDp = utils.getMinutes(lesson.getStartTime())-540;
                int heightTimeInDp = utils.getMinutes(lesson.getEndTime())-utils.getMinutes(lesson.getStartTime());
                if(heightTimeInDp%60 == 50) heightTimeInDp += 10;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, utils.dp2px(heightTimeInDp, getApplicationContext()));

                params.topMargin = utils.dp2px(startTimeInDp, getApplicationContext());

                currentRelativeLayout.addView(newLesson, params);
            }
        }
    }
    private void addNewLesson(final int lessonId) {
        //delete add btn from the view
        LinearLayout tableLayout = (LinearLayout) findViewById(R.id.calendarSplitterRelativeLayout);
        ImageView existingBtn = (ImageView) tableLayout.findViewById(R.id.add_new_btn);
        if( existingBtn != null){
            //Toast.makeText(this, "btn exists", Toast.LENGTH_SHORT).show();
            ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
        };

        initializeDB(STUDENT_ID);
        final Cursor cursor = SQLdatabase.rawQuery("SELECT * FROM "+DBHelper.TABLE_SCHEDULE_ID+" WHERE _id = " + lessonId, null);
        //Log.d(TAG, "count cursor: "+ cursor.getCount());
        if (cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int dayIndex = cursor.getColumnIndex(DBHelper.KEY_DAY);
            int startIndex = cursor.getColumnIndex(DBHelper.KEY_STARTTIME);
            int endIndex = cursor.getColumnIndex(DBHelper.KEY_ENDTIME);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int typeIndex = cursor.getColumnIndex(DBHelper.KEY_TYPE);
            int descIndex = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            int roomIndex = cursor.getColumnIndex(DBHelper.KEY_ROOM);
            int instructorIndex = cursor.getColumnIndex(DBHelper.KEY_INSTRUCTOR);
            int colorIndex = cursor.getColumnIndex(DBHelper.KEY_COLOR);
            do{
                Log.d("mLog", "_id= "+cursor.getInt(idIndex)+", day = " + cursor.getString(dayIndex) +
                        ", startTime = " + cursor.getString(startIndex) +
                        ", title = " + cursor.getString(titleIndex)+
                        ", endTime = " + cursor.getString(endIndex));
                Lesson mLesson = new Lesson(cursor.getString(titleIndex), cursor.getString(startIndex),cursor.getString(endIndex),cursor.getString(instructorIndex),cursor.getString(roomIndex),cursor.getString(dayIndex), cursor.getInt(colorIndex));
                mLesson.setType(cursor.getString(typeIndex));
                mLesson.setDescription(cursor.getString(descIndex));
                mLesson.setId(cursor.getInt(idIndex));
                days.get(mLesson.getDayIndex()).addLesson(mLesson);
                String layoutId = cursor.getString(dayIndex).toLowerCase()+"RelativeLayout";
                int relativeId = getResources().getIdentifier(layoutId, "id", getPackageName());
                currentRelativeLayout = (RelativeLayout) findViewById(relativeId);
                LessonEventView newLesson = new LessonEventView(getApplicationContext(), null);
                newLesson.setNameText(cursor.getString(titleIndex));
                newLesson.setRoomText(cursor.getString(roomIndex));
                newLesson.setTag(cursor.getInt(idIndex));
                newLesson.setBackgroundColor(cursor.getInt(colorIndex));
                newLesson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), LessonDetailsActivity.class);
                        intent.putExtra("lessonId", lessonId);
                        Log.d("mLog", "recieved ID: "+ lessonId);
                        startActivityForResult(intent, 2);
                    }
                });
                int startTimeInDp = utils.getMinutes(cursor.getString(startIndex))-540;
                int heightTimeInDp = utils.getMinutes(cursor.getString(endIndex))-utils.getMinutes(cursor.getString(startIndex));
                if(heightTimeInDp%60 == 50) heightTimeInDp += 10;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, utils.dp2px(heightTimeInDp, getApplicationContext()));

                params.topMargin = utils.dp2px(startTimeInDp, getApplicationContext());

                currentRelativeLayout.addView(newLesson, params);

            }
            while(cursor.moveToNext());
        }
        cursor.close();
        dbHelper.close();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN){
            touchDownXY[0] = (int) event.getX();
            touchDownXY[1] = (int) event.getY();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Day clickedDay;
        int clickedId = v.getId();
        currentRelativeLayout = (RelativeLayout) findViewById(clickedId);
        if(days!= null) clickedDay = days.get(Integer.valueOf(v.getTag().toString()));
        else clickedDay = null;

        LinearLayout tableLayout = (LinearLayout) findViewById(R.id.calendarSplitterRelativeLayout);
        ImageView existingBtn = (ImageView) tableLayout.findViewById(R.id.add_new_btn);
        if( existingBtn != null){
            //Toast.makeText(this, "btn exists", Toast.LENGTH_SHORT).show();
            ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
        }

        if(clickedDay!=null && clickedDay.intersect(utils.pxToDp(touchDownXY[1])+540) != null) return;

        ImageView addBtn = new ImageView(this);
        addBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_plus));
        addBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        addBtn.setId(R.id.add_new_btn);

        int height = 60;
        int marginTop = utils.pxToDp(touchDownXY[1])/60 *60;
        Lesson intersectLesson = null;
        if(clickedDay != null) intersectLesson= clickedDay.intersect(marginTop+540);
        if( intersectLesson!= null){
            marginTop = utils.getMinutes(intersectLesson.getEndTime())-540;
            Lesson intersectLessonTwo = clickedDay.intersect(marginTop+600);
            if(intersectLessonTwo != null){
                height = utils.getMinutes(intersectLessonTwo.getStartTime())-utils.getMinutes(intersectLesson.getEndTime());
                if(height < 15) return;
            }
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, utils.dp2px(height, getApplicationContext()));
        params.topMargin = utils.dp2px( marginTop, getApplicationContext());
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                intent.putExtra("START_TIME", utils.getStringTime(utils.pxToDp(lp.topMargin)));
                intent.putExtra("END_TIME", utils.getStringTime(utils.pxToDp(lp.topMargin+lp.height)));
                intent.putExtra("COLOR", androidColors[new Random().nextInt(androidColors.length)] );
                intent.putExtra("DAY", dayOfWeek[((LinearLayout) v.getParent().getParent()).indexOfChild((RelativeLayout) v.getParent())-1]);
                Log.d("mLog", "send day: "+dayOfWeek[((LinearLayout) v.getParent().getParent()).indexOfChild((RelativeLayout) v.getParent())-1]);
                startActivityForResult(intent, 1);
             }
        });


        currentRelativeLayout.addView(addBtn, params);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList<Integer> resultIds  = data.getIntegerArrayListExtra("resultIds");
                Log.d("mLog", "got result ID: "+ resultIds.toString());
                for (int i = 0; i < resultIds.size(); i++) {
                    addNewLesson(resultIds.get(i));
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.w("mLog", "no data inserted :(");
            }
        } else
        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                int lessonId  = data.getIntExtra("resultId", -1);
                int btnType = data.getIntExtra("btnType", -1);
                if(btnType == 0){
                    Log.d("mLog", "btnType delete ");
                    deleteLesson(lessonId, data.getStringExtra("day"));
                }else if(btnType == 1){
                    Intent intent = new Intent(getApplicationContext(), EditLessonActivity.class);
                    intent.putExtra("lessonId",lessonId);
                    startActivityForResult(intent, 3);
                }
                Log.d("mLog", "got result : "+ lessonId+" "+btnType);
                //Intent intent  = new Intent(MainActivity.this, );
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.w("mLog", "no menu button pressed");
            }
        }
        if (requestCode == 3) {
            if(resultCode == Activity.RESULT_OK){
                int lessonId  = data.getIntExtra("lessonId", -1);
                if(lessonId > 0){
                    Log.d("mLog", "btnType delete ");
                    LinearLayout tableLayout = (LinearLayout) findViewById(R.id.calendarSplitterRelativeLayout);
                    LessonEventView lessonView = (LessonEventView) tableLayout.findViewWithTag(lessonId);
                    if( lessonView != null){
                        ((RelativeLayout) lessonView.getParent()).removeView(lessonView);
                        int dayIndex = Arrays.asList(dayOfWeek).indexOf(data.getStringExtra("day"));
                        days.get(dayIndex).deleteLesson(lessonId);
                    } else{
                        Log.d("mLog", "deleteLesson: couldn't find view with tag "+ lessonId);
                    }
                    addNewLesson(lessonId);
                }
                Log.d("mLog", "got result : "+ lessonId);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.w("mLog", "no menu button pressed");
            }
        }
    }

    public boolean tableExists(){
        Cursor cursor = SQLdatabase.query(DBHelper.TABLE_SCHEDULE_ID, null, null, null, null, null, null);
        if(cursor.getCount() == 0) return false;
        return true;
    }
    private int addToDatabase(Lesson lesson){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_STARTTIME, lesson.getStartTime());
        contentValues.put(DBHelper.KEY_ENDTIME, lesson.getEndTime());
        contentValues.put(DBHelper.KEY_TITLE, lesson.getTitle());
        contentValues.put(DBHelper.KEY_INSTRUCTOR, lesson.getInstructor());
        contentValues.put(DBHelper.KEY_ROOM, lesson.getRoom());
        contentValues.put(DBHelper.KEY_DAY, lesson.getDay());
        contentValues.put(DBHelper.KEY_TYPE, lesson.getType());
        contentValues.put(DBHelper.KEY_COLOR, lesson.getColor());

        long insertedId = SQLdatabase.insert(dbHelper.TABLE_SCHEDULE_ID, null, contentValues);

        return (int) insertedId;
    }
    private void deleteLesson(int lessonId, String day){
        initializeDB(STUDENT_ID);
        Log.d("mLog", "trying to deleteLesson: "+lessonId);
        int deletedRows  = SQLdatabase.delete(dbHelper.TABLE_SCHEDULE_ID, dbHelper.KEY_ID + "="+lessonId,null);
        Log.d("mLog", "deleted Lesson: "+deletedRows);
        dbHelper.close();
        if(deletedRows == 1){
            LinearLayout tableLayout = (LinearLayout) findViewById(R.id.calendarSplitterRelativeLayout);
            LessonEventView lessonView = (LessonEventView) tableLayout.findViewWithTag(lessonId);
            if( lessonView != null){
                ((RelativeLayout) lessonView.getParent()).removeView(lessonView);
                int dayIndex = Arrays.asList(dayOfWeek).indexOf(day);
                days.get(dayIndex).deleteLesson(lessonId);
            } else{
                Log.d("mLog", "deleteLesson: couldn't find view with tag "+ lessonId);
            }
        }
    }
    private void initializeDB(String id){
        dbHelper = new DBHelper(this, id);
        SQLdatabase = dbHelper.getWritableDatabase();
    }
    private Lesson getLesson(int lessonId){
        initializeDB(STUDENT_ID);
        Lesson mLesson = null;
        final Cursor cursor = SQLdatabase.rawQuery("SELECT * FROM "+ DBHelper.TABLE_SCHEDULE_ID+" WHERE _id = " + lessonId, null);
        Log.d("mLog", "count cursor: "+ cursor.getCount());
        if (cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int dayIndex = cursor.getColumnIndex(DBHelper.KEY_DAY);
            int startIndex = cursor.getColumnIndex(DBHelper.KEY_STARTTIME);
            int endIndex = cursor.getColumnIndex(DBHelper.KEY_ENDTIME);
            int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
            int roomIndex = cursor.getColumnIndex(DBHelper.KEY_ROOM);
            int instructorIndex = cursor.getColumnIndex(DBHelper.KEY_INSTRUCTOR);
            int colorIndex = cursor.getColumnIndex(DBHelper.KEY_COLOR);
            do{
                Log.d("mLog", "_id= "+cursor.getInt(idIndex)+", day = " + cursor.getString(dayIndex) +
                        ", startTime = " + cursor.getString(startIndex) +
                        ", title = " + cursor.getString(titleIndex)+
                        ", endTime = " + cursor.getString(endIndex));
                mLesson = new Lesson(cursor.getString(titleIndex), cursor.getString(startIndex),cursor.getString(endIndex),cursor.getString(instructorIndex),cursor.getString(roomIndex),cursor.getString(dayIndex), cursor.getInt(colorIndex));
                mLesson.setId(cursor.getInt(idIndex));
            }
            while(cursor.moveToNext());
        }cursor.close();
        dbHelper.close();
        return mLesson;
    }

    private void notificationOn(){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("notify_mode", true);
        editor.apply();

        MenuItem notifyBtn = menu.findItem(R.id.notify_btn);
        MenuItem silenceBtn = menu.findItem(R.id.silence_btn);
        notifyBtn.setVisible(false);
        silenceBtn.setVisible(true);

        if(days != null)
        for(Day day : days){
            for(Lesson lesson: day.getLessons() ){
                setAlarmService(lesson);
            }
        }
        

    }
    private void notificationOff(){
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("notify_mode", false);

        MenuItem notifyBtn = menu.findItem(R.id.notify_btn);
        MenuItem silenceBtn = menu.findItem(R.id.silence_btn);
        notifyBtn.setVisible(true);
        silenceBtn.setVisible(false);

        if(days!=null)
        for(Day day : days){
            for(Lesson lesson: day.getLessons() ){
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent myIntent = new Intent(getApplicationContext(), NotificationReciever.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), lesson.getId(), myIntent, 0);
                alarmManager.cancel(pendingIntent);
            }
        }

    }
    private void setAlarmService(Lesson lesson){
        Calendar c = Calendar.getInstance();
        int dayIndex = Arrays.asList(dayOfWeek).indexOf(lesson.getDay());
        int minutes = utils.getMinutes(lesson.getStartTime());
        int difference = dayIndex-utils.getTodayIndex();
        if(difference < 0) difference = 7+difference;
        if(difference == 0 && (minutes<(c.get(Calendar.HOUR_OF_DAY)*60)+c.get(Calendar.MINUTE))) difference = 7;
        Log.d("mLog", "got today and differece: "+ c+"\n"+"difference: "+difference+"\n"+"lesson day: "+lesson.getDay());
        c.add(Calendar.DAY_OF_MONTH, difference);
        Log.d("mLog", "adding time: hour - "+ minutes/60 +" minutes - "+ minutes%60);
        c.set(Calendar.HOUR_OF_DAY, minutes/60);
        c.set(Calendar.MINUTE,minutes%60);
        c.set(Calendar.SECOND, 0);
        Log.d("mLog", "after add calendar: "+c.getTimeInMillis());
        Intent intent = new Intent(MainActivity.this, NotificationReciever.class);
        //Log.d(TAG, "setAlarmService for lesson: "+lesson.getTitle()+" "+lesson.getType()+" "+lesson.getStartTime());
        intent.putExtra("title", lesson.getTitle());
        intent.putExtra("lesson_id", lesson.getId());
        intent.putExtra("subText", lesson.getStartTime()+"-"+lesson.getEndTime()+", "+lesson.getType()+"\n"+lesson.getRoom());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, lesson.getId(), intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= 19)
            am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() - 10*60*1000, pendingIntent);
        else
            am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), am.INTERVAL_DAY*7, pendingIntent);

    }
    */
}
