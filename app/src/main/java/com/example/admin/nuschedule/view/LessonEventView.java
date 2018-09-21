package com.example.admin.nuschedule.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.room_model.LessonModel;

import java.util.Calendar;
import java.util.Random;

public class LessonEventView extends LinearLayout {
    LinearLayout layout = null;
    TextView nameTextView = null;
    TextView roomTextView = null;
    Context mContext = null;
    int mShortAnimationDuration;
    public LessonEventView(Context context, LessonModel lesson) {
        super(context);
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(null, R.styleable.LessonEventView);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);

        layout = (LinearLayout) li.inflate(R.layout.view_lesson_event, this, true);
        Random rnd = new Random();
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_longAnimTime);
        nameTextView = (TextView) layout.findViewById(R.id.lesson_name);
        roomTextView = (TextView) layout.findViewById(R.id.lesson_room);

        roomTextView.setText(lesson.getRoom());
        nameTextView.setText(lesson.getTitle());
        setBackgroundColor((int)lesson.getColor());
        setTag(lesson.getId());
        setAlpha(0f);
        setVisibility(GONE);

        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();


        setVisibility(View.VISIBLE);
            animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);


    }
    public  void remove(final ViewGroup parent){
        super.onDetachedFromWindow();
        animate()
                .alpha(0f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        parent.removeView(LessonEventView.this);
                    }
                });

    }


    @SuppressWarnings("unused")
    public void setNameTextLong(String text) {
        nameTextView.setText(text);
    }
    @SuppressWarnings("unused")
    public void setRoomText(String text) {
        roomTextView.setText(text);
    }

    @SuppressWarnings("unused")
    public String getRoomText() {
        return roomTextView.getText().toString();
    }

    @SuppressWarnings("unused")
    public String getNameText() {
        return nameTextView.getText().toString();
    }

}
