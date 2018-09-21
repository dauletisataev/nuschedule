package com.example.admin.nuschedule.reciever;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.example.admin.nuschedule.database.DBHelper;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.room_model.LessonModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class AutoStartUp extends BroadcastReceiver {
    public String STUDENT_ID = "201599251";
    public static final String MY_PREFS_NAME = "NuSchedulePref";
    DBHelper dbHelper;
    SQLiteDatabase SQLdatabase;
    Context mContext;
    String[] dayOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    Utils utils = new Utils();
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        SharedPreferences preferences = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (preferences.contains("notify_mode") && preferences.getBoolean("notify_mode", false)) {
            initializeDB(STUDENT_ID);
            Cursor cursor = SQLdatabase.query(DBHelper.TABLE_SCHEDULE_ID, null, null, null, null, null, null);

            if (cursor.moveToFirst()) { int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                int dayIndex = cursor.getColumnIndex(DBHelper.KEY_DAY);
                int startIndex = cursor.getColumnIndex(DBHelper.KEY_STARTTIME);
                int endIndex = cursor.getColumnIndex(DBHelper.KEY_ENDTIME);
                int titleIndex = cursor.getColumnIndex(DBHelper.KEY_TITLE);
                int roomIndex = cursor.getColumnIndex(DBHelper.KEY_ROOM);
                int typeIndex = cursor.getColumnIndex(DBHelper.KEY_TYPE);
                int instructorIndex = cursor.getColumnIndex(DBHelper.KEY_INSTRUCTOR);
                int colorIndex = cursor.getColumnIndex(DBHelper.KEY_COLOR);

                do {
//                    LessonModel lesson = new LessonModel(cursor.getString(titleIndex), cursor.getString(startIndex),cursor.getString(endIndex),cursor.getString(instructorIndex),cursor.getString(roomIndex),cursor.getString(dayIndex), cursor.getInt(colorIndex));
//                    lesson.setId(cursor.getInt(idIndex));
//                    lesson.setType(cursor.getString(typeIndex));
//                    setAlarmService(lesson);

                } while (cursor.moveToNext());

            } else
                Log.d("mLog","0 rows");

            cursor.close();
            dbHelper.close();
        }
    }

    private void setAlarmService(LessonModel lesson){
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
        Intent intent = new Intent(mContext, NotificationReciever.class);
        Log.d("mLog", "from service setAlarmService for lesson: "+lesson.getTitle()+" "+lesson.getType()+" "+lesson.getStartTime());
        intent.putExtra("title", lesson.getTitle());
        intent.putExtra("lesson_id", lesson.getId());
        intent.putExtra("subText", lesson.getStartTime()+"-"+lesson.getEndTime()+", "+lesson.getType()+"\n"+lesson.getRoom());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, (int) lesson.getId(), intent, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= 19)
            am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() - 10*60*1000, pendingIntent);
        else
            am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), am.INTERVAL_DAY*7, pendingIntent);

    }
    private void initializeDB(String id){
        dbHelper = new DBHelper(mContext, id);
        SQLdatabase = dbHelper.getWritableDatabase();
    }

}
