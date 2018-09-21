package com.example.admin.nuschedule.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.activities.LessonDetailsActivity;
import com.example.admin.nuschedule.models.Lessons;
import com.example.admin.nuschedule.other.Utils;
import com.example.admin.nuschedule.room_model.LessonModel;
import com.example.admin.nuschedule.view.LessonEventView;

import org.w3c.dom.Text;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import static com.example.admin.nuschedule.other.Constants.dayOfFullWeek;
import static com.example.admin.nuschedule.other.Constants.dayOfWeek;
import static com.example.admin.nuschedule.other.Constants.TAG;

public class RvScheduleAdapter extends RecyclerView.Adapter<RvScheduleAdapter.DayViewHolder> {
    Lessons lessons;
    Utils utils = new Utils();
    private final OnItemClickListener listener;

    public RvScheduleAdapter(Lessons lessons, OnItemClickListener listener) {
        Log.d(TAG, "RvScheduleAdapter: constructor: " + lessons.toString());
        this.lessons = lessons;
        this.listener = listener;
    }
    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_day_schedule, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final DayViewHolder viewHolder, int i) {
        //i %= dayOfWeek.length;
        int todayIndex =  utils.getTodayIndex();
        Calendar c = Calendar.getInstance();
        int diff = i -todayIndex;
        c.add(Calendar.DAY_OF_MONTH, diff);
        int dayIndex = c.get(Calendar.DAY_OF_WEEK) -2;
        Log.d(TAG, "onBindViewHolder: i: "+i+ " diff: "+diff+ " dayIndex: "+dayIndex);
        viewHolder.lessonsLayout.removeAllViews();
        if(dayIndex == -1 || dayIndex > 4) {

            if(dayIndex == -1) {
                dayIndex =6;
                Log.d(TAG, "sunday triggered ");
            }
            else Log.d(TAG, "saturday triggered ");
            String day = dayOfFullWeek[dayIndex];
            viewHolder.tDay.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
            viewHolder.tDayOfWeek.setText(day.substring(0, 3));
            viewHolder.noLesson.setVisibility(View.VISIBLE);
            return;
        }
        viewHolder.noLesson.setVisibility(View.GONE);
        Log.d(TAG, "working day triggered ");
        String day = dayOfWeek[dayIndex];
        int dayOfMonth =c.get(Calendar.DAY_OF_MONTH);
        viewHolder.tDay.setText(String.valueOf(dayOfMonth));
        viewHolder.tDayOfWeek.setText(day.substring(0, 2));

        if(lessons.get(day) == null) return;

        if(dayOfMonth == 1) {
            viewHolder.monthText.setVisibility(View.VISIBLE);
            viewHolder.monthText.setText(new DateFormatSymbols().getMonths()[c.get(Calendar.MONTH)]);
        }
        for(final LessonModel lesson: lessons.get(day).values()){
            final LessonEventView newLesson = new LessonEventView(viewHolder.lessonsLayout.getContext(), lesson);
            newLesson.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(lesson.getId());
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, utils.dp2px(60, viewHolder.lessonsLayout.getContext()));
            newLesson.setRoomText(lesson.getStartTime() + " - "+ lesson.getEndTime()+", "+lesson.getRoom());
            params.topMargin = utils.dp2px(10, viewHolder.lessonsLayout.getContext());

            viewHolder.lessonsLayout.addView(newLesson, params);

        }
    }
    @Override
    public int getItemCount() {
        return lessons.size() == 0 ? 0 : Integer.MAX_VALUE;
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tDay;
        TextView tDayOfWeek;
        TextView monthText;
        TextView noLesson;
        LinearLayout lessonsLayout;

        DayViewHolder(View itemView) {
            super(itemView);
            tDay = (TextView) itemView.findViewById(R.id.dayText);
            tDayOfWeek = (TextView) itemView.findViewById(R.id.dayOfWeekText);
            lessonsLayout = (LinearLayout) itemView.findViewById(R.id.lessonsLayout);
            monthText = (TextView) itemView.findViewById(R.id.monthText);
            noLesson = (TextView) itemView.findViewById(R.id.noLesson);

        }

    }
    public interface OnItemClickListener {
        void onItemClick(long id);
    }
}
