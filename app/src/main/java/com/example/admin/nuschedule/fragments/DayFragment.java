package com.example.admin.nuschedule.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.nuschedule.R;
import com.example.admin.nuschedule.adapters.PageAdapter;
import com.example.admin.nuschedule.other.Utils;

public class DayFragment extends Fragment implements ViewPager.OnPageChangeListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    ViewPager viewPager;
    PageAdapter adapter;
    //private DayScrollListener dayScrollListener;
    public DayFragment() {
    }

    public static DayFragment newInstance(String param1, String param2) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        View v = inflater.inflate(R.layout.fragment_day, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.pager);
        adapter = new PageAdapter(getChildFragmentManager());
        Utils utils = new Utils();
        viewPager.setAdapter(adapter);
        Log.d("myLog", "today Index: "+utils.getTodayIndex());
        viewPager.setCurrentItem(utils.getTodayIndex()+1);
        viewPager.setOnPageChangeListener(this);
        return v;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int curr = viewPager.getCurrentItem();
            int lastReal = viewPager.getAdapter().getCount() - 2;
            if (curr == 0) {
                viewPager.setCurrentItem(lastReal, false);
            } else if (curr > lastReal) {
                viewPager.setCurrentItem(1, false);
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

   /* public interface DayScrollListener {
        public void onDayScroll();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DayFragment.DayScrollListener) {
            dayScrollListener = (DayFragment.DayScrollListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        dayScrollListener = null;
    }*/
}
