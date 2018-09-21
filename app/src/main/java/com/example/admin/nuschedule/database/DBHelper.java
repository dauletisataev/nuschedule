package com.example.admin.nuschedule.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "scheduleDB";
    public static String TABLE_SCHEDULE_ID;

    public static final String KEY_ID = "_id";
    public static final String KEY_STARTTIME = "start_time";
    public static final String KEY_ENDTIME = "end_time";
    public static final String KEY_TITLE = "title";
    public static final String KEY_INSTRUCTOR = "instructor";
    public static final String KEY_ROOM = "room";
    public static final String KEY_DAY = "day";
    public static final String KEY_TYPE = "type";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_COLOR = "color";
    public static final String KEY_LESSON_ID = "lesson_id";




    public DBHelper(Context context,  String student_id) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_SCHEDULE_ID = "id_"+student_id;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_SCHEDULE_ID + " (" + KEY_ID
                + " integer primary key," + KEY_STARTTIME + " text," + KEY_ENDTIME + " text," + KEY_INSTRUCTOR + " text," + KEY_TITLE +" text,"+ KEY_TYPE +" text,"+ KEY_DESCRIPTION +" text," + KEY_ROOM + " text,"+ KEY_COLOR + " integer," + KEY_DAY + " text" + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_SCHEDULE_ID);
        onCreate(db);
    }

}
