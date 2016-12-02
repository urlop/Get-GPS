package com.example.ruby.getgps.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.ui.adapters.ImageSlideAdapterTutorial;
import com.example.ruby.getgps.utils.PageIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SplashFragment extends Fragment {

    public static final String ARG_ITEM_ID = "SplashFragment";


    @Bind(R.id.view_pager)
    ViewPager mViewPager;

    @Bind(R.id.indicator)
    PageIndicator mIndicator;

    private boolean stopSliding = false;

    private Runnable animateViewPager;
    private Handler handler;
    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;


    private final List<Drawable> images_tutorial = new ArrayList<>();
    private List<String> titles_strings = new ArrayList<>();
    private List<String> messages_strings = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        ButterKnife.bind(this, view);
        setData();
        mViewPager.setAdapter(new ImageSlideAdapterTutorial(
                getActivity(), images_tutorial, titles_strings, messages_strings, SplashFragment.this));
        mIndicator.setViewPager(mViewPager);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {

                    case MotionEvent.ACTION_CANCEL:
                        break;

                    case MotionEvent.ACTION_UP:
                        // calls when touch release on ViewPager
                        if (images_tutorial != null && images_tutorial.size() != 0) {
                            stopSliding = false;
                            runnable(images_tutorial.size());
                            handler.postDelayed(animateViewPager,
                                    ANIM_VIEWPAGER_DELAY_USER_VIEW);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // calls when ViewPager touch
                        if (handler != null && !stopSliding) {
                            stopSliding = true;
                            handler.removeCallbacks(animateViewPager);
                        }
                        break;
                }
                return false;
            }
        });


        return view;
    }

    private void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!stopSliding) {
                    if (mViewPager.getCurrentItem() == size - 1) {
                        mViewPager.setCurrentItem(0);
                    } else {
                        mViewPager.setCurrentItem(
                                mViewPager.getCurrentItem() + 1, true);
                    }
                    handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                }
            }
        };
    }


    private void setData() {
        Drawable login_logo;
        login_logo = ContextCompat.getDrawable(getActivity(), R.drawable.ic_everlance_splash);
        images_tutorial.add(login_logo);
        images_tutorial.add(login_logo);
        images_tutorial.add(login_logo);
        images_tutorial.add(login_logo);

        String[] titles_array = getResources().getStringArray(R.array.titles_strings);
        String[] messages_array = getResources().getStringArray(R.array.messages_strings);
        titles_strings = new ArrayList<>(Arrays.asList(titles_array));
        messages_strings = new ArrayList<>(Arrays.asList(messages_array));
    }
}
