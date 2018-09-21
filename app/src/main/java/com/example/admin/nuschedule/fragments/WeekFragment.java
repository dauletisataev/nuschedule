package com.example.admin.nuschedule.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.activities.AddEventActivity;
import com.example.admin.nuschedule.activities.LessonDetailsActivity;
import com.example.admin.nuschedule.models.Lessons;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.reciever.NotificationReciever;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.example.admin.nuschedule.view.DayTableView;
import com.example.admin.nuschedule.view.LessonEventView;
import com.example.admin.nuschedule.viewModels.LessonViewModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.example.admin.nuschedule.other.Constants.MY_PREFS_NAME;
import static com.example.admin.nuschedule.other.Constants.TAG;
import static com.example.admin.nuschedule.other.Constants.dayOfWeek;

public class WeekFragment extends Fragment implements View.OnTouchListener, View.OnClickListener {

    RelativeLayout currentRelativeLayout;
    public Lessons lessons;
    private int[] touchDownXY = new int[2];
    Utils utils;
    int[] androidColors;
    Menu menu;
    View rootView;
    View pointer;
    public LessonViewModel lessonViewModel;
    private ScrollListener scrollListener;

    public WeekFragment() {
    }

    public static WeekFragment newInstance(String param1, String param2) {
        WeekFragment fragment = new WeekFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_week, container, false);
        rootView = v;
        utils = new Utils();
        super.onCreate(savedInstanceState);
        ((RelativeLayout) v.findViewById(R.id.mondayRelativeLayout)).setOnTouchListener(this);
        ((RelativeLayout) v.findViewById(R.id.tuesdayRelativeLayout)).setOnTouchListener(this);
        ((RelativeLayout) v.findViewById(R.id.wednesdayRelativeLayout)).setOnTouchListener(this);
        ((RelativeLayout) v.findViewById(R.id.thursdayRelativeLayout)).setOnTouchListener(this);
        ((RelativeLayout) v.findViewById(R.id.fridayRelativeLayout)).setOnTouchListener(this);
        ((RelativeLayout) v.findViewById(R.id.mondayRelativeLayout)).setOnClickListener(this);
        ((RelativeLayout) v.findViewById(R.id.tuesdayRelativeLayout)).setOnClickListener(this);
        ((RelativeLayout) v.findViewById(R.id.wednesdayRelativeLayout)).setOnClickListener(this);
        ((RelativeLayout) v.findViewById(R.id.thursdayRelativeLayout)).setOnClickListener(this);
        ((RelativeLayout) v.findViewById(R.id.fridayRelativeLayout)).setOnClickListener(this);
        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.calendarScrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if(scrollListener == null) return false;
                scrollListener.onScroll();
                return false;
            }
        });
        pointer = inflater.inflate(R.layout.view_pointer, null);
        setPointer();
        lessonViewModel = ViewModelProviders.of(this).get(LessonViewModel.class);
        lessonViewModel.initialize(getContext());
        lessonViewModel.getAllLessons().observe(this, observer);
        androidColors = getResources().getIntArray(R.array.mainColors);
        lessons = new Lessons();
        for(int i: androidColors) Log.d(TAG, "android color: " + i);
        int todayIndex = utils.getTodayIndex();
        if (todayIndex <= 4) {
            String textViewId = dayOfWeek[todayIndex].toLowerCase() + "TextView";
            int txtId = getResources().getIdentifier(textViewId, "id", getActivity().getPackageName());
            ((TextView) v.findViewById(txtId)).setTextColor(getResources().getColor(R.color.black_de));
        }
        return v;
    }

    void setPointer(){
        Calendar c = Calendar.getInstance();
        int currentTime = (c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE));
        if(currentTime> 1260) currentTime = 1260;
        currentTime -= 540+10;
        if(currentTime < 0) return;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = utils.dp2px(currentTime, getActivity());
        ViewCompat.setElevation(pointer, 100);
        int dayIndex = c.get(Calendar.DAY_OF_WEEK) -2;
        if(dayIndex == -1 || dayIndex > 4) dayIndex =4;
        pointer.findViewById(R.id.circle).setVisibility(View.GONE);
        String layoutId = dayOfWeek[dayIndex].toLowerCase() + "RelativeLayout";
        int relativeId = getResources().getIdentifier(layoutId, "id", getActivity().getPackageName());
        currentRelativeLayout = (RelativeLayout) rootView.findViewById(relativeId);
        currentRelativeLayout.addView(pointer, params);
    }

    Observer<List<LessonModel>> observer = new Observer<List<LessonModel>>() {
        @Override
        public void onChanged(@Nullable List<LessonModel> mLessons) {
            for (LessonModel ls : mLessons) {
                if(!lessons.doesExist(ls.getDay(), ls)) {
                    lessons.addLesson(ls);
                    Log.d(TAG, "onChanged: adding lesson"+ ls);
                    addLessonView(ls);
                }
            }
        }
    };

    private void addLessonView(final LessonModel lesson) {
        String layoutId = lesson.getDay().toLowerCase() + "RelativeLayout";
        int relativeId = getResources().getIdentifier(layoutId, "id", getActivity().getPackageName());
        currentRelativeLayout = (RelativeLayout) rootView.findViewById(relativeId);
        final LessonEventView newLesson = new LessonEventView(getActivity(), lesson);
        newLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout tableLayout = (LinearLayout) rootView.findViewById(R.id.calendarSplitterRelativeLayout);
                ImageView existingBtn = (ImageView) tableLayout.findViewById(R.id.add_new_btn);
                if (existingBtn != null) {
                    ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
                }
                Intent intent = new Intent(getContext(), LessonDetailsActivity.class);
                intent.putExtra("lessonId", lesson.getId());
                Log.d("mLog", "send ID: " + lesson.getId());
                startActivityForResult(intent, 2);

            }
        });
        int startTimeInDp = utils.getMinutes(lesson.getStartTime()) - 540;
        int heightTimeInDp = utils.getMinutes(lesson.getEndTime()) - utils.getMinutes(lesson.getStartTime());
        if (heightTimeInDp % 60 == 50) heightTimeInDp += 10;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, utils.dp2px(heightTimeInDp, getActivity()));

        params.topMargin = utils.dp2px(startTimeInDp, getActivity());

        currentRelativeLayout.addView(newLesson, params);

    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchDownXY[0] = (int) event.getX();
            touchDownXY[1] = (int) event.getY();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int clickedId = v.getId();
        String clickedDay = dayOfWeek[Integer.valueOf(v.getTag().toString())];
        currentRelativeLayout = (RelativeLayout) rootView.findViewById(clickedId);

        LinearLayout tableLayout = (LinearLayout) rootView.findViewById(R.id.calendarSplitterRelativeLayout);
        ImageView existingBtn = (ImageView) tableLayout.findViewById(R.id.add_new_btn);
        if (existingBtn != null) {
            ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
        }

        if (lessons.intersect(clickedDay, utils.pxToDp(touchDownXY[1]) + 540) != null)
            return;

        ImageView addBtn = new ImageView(getActivity());
        addBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_plus));
        addBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        addBtn.setId(R.id.add_new_btn);

        int height = 60;
        int marginTop = utils.pxToDp(touchDownXY[1]) / 60 * 60;
        LessonModel intersectLesson = null;
        if (clickedDay != null) intersectLesson = lessons.intersect(clickedDay, marginTop + 540);
        if (intersectLesson != null) {
            marginTop = utils.getMinutes(intersectLesson.getEndTime()) - 540;
            LessonModel intersectLessonTwo = lessons.intersect(clickedDay,marginTop + 600);
            if (intersectLessonTwo != null) {
                height = utils.getMinutes(intersectLessonTwo.getStartTime()) - utils.getMinutes(intersectLesson.getEndTime());
                if (height < 15) return;
            }
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, utils.dp2px(height, getActivity()));
        params.topMargin = utils.dp2px(marginTop, getActivity());
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v.getLayoutParams();
                Intent intent = new Intent(getContext(), AddEventActivity.class);
                intent.putExtra("START_TIME", utils.getStringTime(utils.pxToDp(lp.topMargin)));
                intent.putExtra("END_TIME", utils.getStringTime(utils.pxToDp(lp.topMargin + lp.height)));
                intent.putExtra("COLOR", androidColors[new Random().nextInt(androidColors.length)]);
                intent.putExtra("DAY", dayOfWeek[((LinearLayout) v.getParent().getParent()).indexOfChild((RelativeLayout) v.getParent()) - 1]);
                Log.d("mLog", "send day: " + dayOfWeek[((LinearLayout) v.getParent().getParent()).indexOfChild((RelativeLayout) v.getParent()) - 1]);
                startActivityForResult(intent, 1);
            }
        });


        currentRelativeLayout.addView(addBtn, params);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
         if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                long lessonId  = data.getLongExtra("resultId", -1);
                int btnType = data.getIntExtra("btnType", -1);
                if(btnType == 0){
                      if(lessons.deleteLesson( data.getStringExtra("day"), lessonId)){
                        LinearLayout tableLayout = (LinearLayout) rootView.findViewById(R.id.calendarSplitterRelativeLayout);
                        LessonEventView lessonView = (LessonEventView) tableLayout.findViewWithTag(lessonId);
                        if( lessonView != null)
                            lessonView.remove((RelativeLayout) lessonView.getParent());
                        else
                            Log.d("mLog", "deleteLesson: couldn't find view with tag "+ lessonId);

                    }
                }
            }
        }
    }


    private void setAlarmService(LessonModel lesson) {
        Calendar c = Calendar.getInstance();
        int dayIndex = Arrays.asList(dayOfWeek).indexOf(lesson.getDay());
        int minutes = utils.getMinutes(lesson.getStartTime());
        int difference = dayIndex - utils.getTodayIndex();
        if (difference < 0) difference = 7 + difference;
        if (difference == 0 && (minutes < (c.get(Calendar.HOUR_OF_DAY) * 60) + c.get(Calendar.MINUTE)))
            difference = 7;
        Log.d("mLog", "got today and differece: " + c + "\n" + "difference: " + difference + "\n" + "lesson day: " + lesson.getDay());
        c.add(Calendar.DAY_OF_MONTH, difference);
        Log.d("mLog", "adding time: hour - " + minutes / 60 + " minutes - " + minutes % 60);
        c.set(Calendar.HOUR_OF_DAY, minutes / 60);
        c.set(Calendar.MINUTE, minutes % 60);
        c.set(Calendar.SECOND, 0);
        Log.d("mLog", "after add calendar: " + c.getTimeInMillis());
        Intent intent = new Intent(getActivity(), NotificationReciever.class);
        Log.d(TAG, "setAlarmService for lesson: " + lesson.getTitle() + " " + lesson.getType() + " " + lesson.getStartTime());
        intent.putExtra("title", lesson.getTitle());
        intent.putExtra("lesson_id", lesson.getId());
        intent.putExtra("subText", lesson.getStartTime() + "-" + lesson.getEndTime() + ", " + lesson.getType() + "\n" + lesson.getRoom());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), (int)lesson.getId(), intent, 0);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 19)
            am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() - 10 * 60 * 1000, pendingIntent);
        else
            am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), am.INTERVAL_DAY * 7, pendingIntent);

    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        if (c instanceof WeekFragment.ScrollListener) {
            scrollListener = (WeekFragment.ScrollListener) c;
        } else {
            throw new RuntimeException(c.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        scrollListener = null;
    }

    public interface ScrollListener {
        void onScroll();
    }
}
