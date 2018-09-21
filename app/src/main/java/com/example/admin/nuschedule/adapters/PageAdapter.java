package com.example.admin.nuschedule.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.admin.nuschedule.view.DayTableView;

import static com.example.admin.nuschedule.other.Constants.dayOfWeek;

public class PageAdapter extends FragmentStatePagerAdapter {

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if(position>0 && position<6) {
            fragment = DayTableView.newInstance(dayOfWeek[position-1]);

        }else
            if(position==0) fragment = DayTableView.newInstance(dayOfWeek[4]);
        else
            fragment = DayTableView.newInstance(dayOfWeek[0]);
        return fragment;

    }

    @Override
    public int getCount() {
        return dayOfWeek.length+2;
    }

}