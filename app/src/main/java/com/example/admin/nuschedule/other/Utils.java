package com.example.admin.nuschedule.other;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.activities.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public Utils(){

    }
    public int getMinutes(String timeValue){

        String[] splitByColon = timeValue.split(":");
        int hoursValue = Integer.parseInt(splitByColon[0]);
        String[] splitForMins = splitByColon[1].split(" ");
        if(splitForMins[1].equals("PM"))
        {
            if(hoursValue!=12) hoursValue = hoursValue + 12;
        }

        int minutesValue = Integer.parseInt(splitForMins[0]);

        return 60*hoursValue + minutesValue;

    }

    public String getStringTime(int timeValue){
            timeValue += 540;
            int hour = timeValue/60;
            int minutes = timeValue % 60;
            String type = "AM";
            if(hour>12) {
                hour %= 12;
                type = "PM";
            }
            String hourS = String.valueOf(hour);
            String minuteS = String.valueOf(minutes);
            if(hour<10) hourS = "0"+hour;
            if(minutes<10) minuteS = "0"+minutes;
        return hourS + ":"+minuteS+" "+type;
    }
    public int dp2px(int dp, Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
    public String getTimeType(int hours, int minutes){
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            final Date dateObj = sdf.parse(String.valueOf(hours)+":"+String.valueOf(minutes));

            return new SimpleDateFormat("hh:mm aa").format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
            return "err";
        }
    }

    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public int getTodayIndex(){
        int i = 1;
        Calendar c = Calendar.getInstance();
        i = c.get(Calendar.DAY_OF_WEEK)-2;
        if(i==-1) i =6;
        Log.d("mLog", "getTodayIndex: "+i);
        return i;
    }
    public void showNotif(Context context){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Calendar calendar = Calendar.getInstance();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 2, intent, 0);
         NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("NU schedule")
                .setContentText("notification is on");
        mBuilder.setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationManager.IMPORTANCE_HIGH);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setVibrate(new long[] { 10, 200});
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(2, mBuilder.build());
    }

    public int[] getMainColors(Context context){
        return context.getResources().getIntArray(R.array.mainColors);
    }
}
