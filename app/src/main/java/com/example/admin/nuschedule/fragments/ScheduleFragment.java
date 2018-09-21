package com.example.admin.nuschedule.fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.activities.LessonDetailsActivity;
import com.example.admin.nuschedule.adapters.RvScheduleAdapter;
import com.example.admin.nuschedule.models.Lessons;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.example.admin.nuschedule.view.LessonEventView;
import com.example.admin.nuschedule.viewModels.LessonViewModel;

import java.util.Calendar;
import java.util.List;

import static com.example.admin.nuschedule.other.Constants.TAG;

public class ScheduleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    LessonViewModel lessonViewModel;
    Lessons lessons;
    View rootView;
    Utils utils;
    int[] androidColors;
    RecyclerView rv;
    RvScheduleAdapter adapter;
    // TODO: Rename and change types of parameters
    private String mParam1;

    public ScheduleFragment() {
        // Required empty public constructor
    }
    public static ScheduleFragment newInstance(String param1 ) {
        ScheduleFragment fragment = new ScheduleFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        utils = new Utils();
        rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        rv = (RecyclerView) rootView.findViewById(R.id.scheduleRecycler);
        lessons = new Lessons();
        adapter = new RvScheduleAdapter(lessons, new RvScheduleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(long id) {
                Intent intent = new Intent(getContext(), LessonDetailsActivity.class);
                intent.putExtra("lessonId", id);
                Log.d("mLog", "send ID: " + id);
                startActivityForResult(intent, 2);
            }
        });
        androidColors = getResources().getIntArray(R.array.mainColors);

        lessonViewModel = ViewModelProviders.of(this).get(LessonViewModel.class);
        lessonViewModel.initialize(getContext());
        lessonViewModel.getAllLessons().observe(this, observer);

        return rootView;

    }
    Observer<List<LessonModel>> observer = new Observer<List<LessonModel>>() {
        @Override
        public void onChanged(@Nullable List<LessonModel> lessonModels) {
            Log.d("mLog", "lessons by day onChanged: " + lessonModels);
            for (LessonModel ls : lessonModels) {
                if (!lessons.doesExist(ls.getDay(), ls)) {
                    lessons.addLesson(ls);
                    Log.d(TAG, "onChanged: adding lesson" + ls);
                }
            }
            adapter.notifyDataSetChanged();
            rv.setAdapter(adapter);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d("mLog", "onACTIVITY result triggered: "+ requestCode);
        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                long lessonId  = data.getLongExtra("resultId", -1);
                int btnType = data.getIntExtra("btnType", -1);
                if(btnType == 0){
                    if(lessons.deleteLesson( data.getStringExtra("day"), lessonId)){
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
