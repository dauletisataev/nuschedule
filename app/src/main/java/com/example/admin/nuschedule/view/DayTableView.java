package com.example.admin.nuschedule.view;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.activities.AddEventActivity;
import com.example.admin.nuschedule.activities.LessonDetailsActivity;
import com.example.admin.nuschedule.fragments.DayFragment;
import com.example.admin.nuschedule.models.Lessons;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.example.admin.nuschedule.viewModels.LessonViewModel;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static com.example.admin.nuschedule.other.Constants.TAG;
import static com.example.admin.nuschedule.other.Constants.dayOfWeek;

public class DayTableView extends Fragment implements View.OnTouchListener, View.OnClickListener{
    private static final String ARG_PARAM1 = "day";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String day;
    private int[] touchDownXY = new int[2];
    LessonViewModel lessonViewModel;
    View rootView;
    TextView  daytOfWeekTxt;
    Lessons lessons;
    Utils utils;
    int[] androidColors;
    RelativeLayout relativeLayout;
    Calendar c;
    View pointer;
    Context context;
    private DayScrollListener dayScrollListener;
    public DayTableView() {
        // Required empty public constructor
    }

    public static DayTableView newInstance(String param1) {
        DayTableView fragment = new DayTableView();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }
    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            day = bundle.getString(ARG_PARAM1);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        readBundle(getArguments());
        utils = new Utils();
        c = Calendar.getInstance();
        rootView = inflater.inflate(R.layout.fragment_day_table_view, container, false);
        relativeLayout = (RelativeLayout) rootView.findViewById(R.id.lessonsLayout);
        //View pointerView = inflater.inflate(R.layout.view_pointer, relativeLayout, true);
        relativeLayout.setOnTouchListener(this);
        relativeLayout.setOnClickListener(this);
        pointer = inflater.inflate(R.layout.view_pointer, null);

        dayScrollListener = (DayTableView.DayScrollListener) getContext();
        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.calendarScrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            float y1 = 0;
            float y0 = 0;
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if(dayScrollListener == null) return false;
                dayScrollListener.onDayScroll();
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    y0 = motionEvent.getY();
                    Log.i("Y", motionEvent.getAction() + " + " + (y0 - y1));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    y1 = motionEvent.getY();
                    if (y0 - y1 > 0) {
                        Log.i("Y", motionEvent.getAction() + " + " + (y0 - y1));
                    } else if (y0 - y1 < 0) {
                        Log.e("Y", motionEvent.getAction() + " - " + (y0 - y1));
                    }
                }
                return false;
            }
        });
        /*if(Build.VERSION.SDK_INT <23) {
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    Log.d(TAG, "onScrollChanged: ");
                    if(dayScrollListener == null) return;
                    dayScrollListener.onDayScroll();
                    ImageView existingBtn = (ImageView) relativeLayout.findViewById(R.id.add_new_btn);
                    if (existingBtn != null) {
                        ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
                    }
                }
            });
        }else scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(dayScrollListener == null) return;
                dayScrollListener.onDayScroll();
                ImageView existingBtn = (ImageView) relativeLayout.findViewById(R.id.add_new_btn);
                if (existingBtn != null) {
                    ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
                }
            }
        });*/
        setPointer();
        daytOfWeekTxt = (TextView) rootView.findViewById(R.id.dayOfWeek);
        daytOfWeekTxt.setText(day);
        lessons = new Lessons();
        androidColors = getResources().getIntArray(R.array.mainColors);

        lessonViewModel = ViewModelProviders.of(this).get(LessonViewModel.class);
        lessonViewModel.initialize(getContext());
        lessonViewModel.getLessonsByDay(day).observe(this, observer);
        return rootView;
    }

    Observer<List<LessonModel>> observer = new Observer<List<LessonModel>>() {
        @Override
        public void onChanged(@Nullable List<LessonModel> lessonModels) {
            Log.d("mLog", "lessons by day onChanged: " + lessonModels);
            for (LessonModel ls : lessonModels)
                if (!lessons.doesExist(ls.getDay(), ls)) {
                    lessons.addLesson(ls);
                    Log.d(TAG, "onChanged: adding lesson" + ls);
                    addLessonView(ls);
                }
        }
    };
    void setPointer(){
        int currentTime = (c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE));
        int todayIndex = c.get(Calendar.DAY_OF_WEEK) -2;
        if(todayIndex == -1) todayIndex = 6;
        int dayIndex = Arrays.asList(dayOfWeek).indexOf(day);
        if(dayIndex != todayIndex) return;
        if(currentTime> 1260) currentTime = 1260;
        currentTime -= 540+10;
        if(currentTime < 0) return;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = utils.dp2px(currentTime, getActivity());
        ViewCompat.setElevation(pointer, 100);
        relativeLayout.addView(pointer, params);
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


        ImageView existingBtn = (ImageView) relativeLayout.findViewById(R.id.add_new_btn);
        if (existingBtn != null) {
            ((RelativeLayout) existingBtn.getParent()).removeView(existingBtn);
        }


        if (lessons.intersect(day, utils.pxToDp(touchDownXY[1]) + 540) != null)
            return;

        ImageView addBtn = new ImageView(getActivity());
        addBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_plus));
        addBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        addBtn.setId(R.id.add_new_btn);

        int height = 60;
        int marginTop = utils.pxToDp(touchDownXY[1]) / 60 * 60;
        LessonModel intersectLesson = null;
        intersectLesson = lessons.intersect(day, marginTop + 540);
        if (intersectLesson != null) {
            marginTop = utils.getMinutes(intersectLesson.getEndTime()) - 540;
            LessonModel intersectLessonTwo = lessons.intersect(day,marginTop + 600);
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
        params.leftMargin = utils.dp2px(10, getActivity());
        relativeLayout.addView(addBtn, params);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d("mLog", "onACTIVITY result triggered: "+ requestCode);
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
    private void addLessonView(final LessonModel lesson) {
        Utils utils = new Utils();
        relativeLayout = (RelativeLayout) rootView.findViewById(R.id.lessonsLayout);


        final LessonEventView newLesson = new LessonEventView(getActivity(), lesson);
        newLesson.setNameTextLong(lesson.getTitle());
        newLesson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView existingBtn = (ImageView) relativeLayout.findViewById(R.id.add_new_btn);
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
        params.leftMargin = utils.dp2px(10, getActivity());
        relativeLayout.addView(newLesson, params);

    }

    public interface DayScrollListener {
        void onDayScroll();
    }
    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        if (c instanceof DayTableView.DayScrollListener) {
            context =c;
            dayScrollListener = (DayTableView.DayScrollListener) c;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dayScrollListener = (DayTableView.DayScrollListener) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        dayScrollListener = null;
    }

}
