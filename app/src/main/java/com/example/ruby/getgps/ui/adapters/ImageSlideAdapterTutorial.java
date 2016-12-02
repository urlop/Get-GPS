package com.example.ruby.getgps.ui.adapters;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.ui.fragments.SplashFragment;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageSlideAdapterTutorial extends PagerAdapter {

    private final FragmentActivity activity;
    private final List<Drawable> images_tutorial;
    private final List<String> titles_strings;
    private final List<String> message_strings;
    private final SplashFragment tutorialFragment;

    @Bind(R.id.image_display)
    ImageView image_display;

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.tv_message)
    TextView tv_message;

    public ImageSlideAdapterTutorial(FragmentActivity activity, List<Drawable> images_tutorial, List<String> titles_strings,
                                     List<String> message_strings,
                                     Fragment fragment) {
        this.activity = activity;
        this.tutorialFragment = (SplashFragment) fragment;
        this.images_tutorial = images_tutorial;
        this.titles_strings = titles_strings;
        this.message_strings = message_strings;

    }

    @Override
    public int getCount() {
        return images_tutorial.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.sliding_content, container, false);
        ButterKnife.bind(this, view);
        setUpView(position);
        container.addView(view);
        return view;
    }

    private void setUpView(int position) {
        image_display.setImageDrawable(images_tutorial.get(position));
        tv_title.setText(titles_strings.get(position));
        tv_message.setText(message_strings.get(position));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


}
