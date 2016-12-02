package com.example.ruby.getgps.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.example.ruby.getgps.R;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewHolderAllTripCard extends RecyclerView.ViewHolder {

    @Bind(R.id.cv_container)
    CardView cv_container;

    @Bind(R.id.tv_purpose)
    TextView tv_purpose;

    @Bind(R.id.tv_miles)
    TextView tv_miles;

    @Bind(R.id.tv_start_time)
    TextView tv_start_time;

    @Bind(R.id.tv_start_address)
    TextView tv_start_address;

    @Bind(R.id.tv_end_time)
    TextView tv_end_time;

    @Bind(R.id.tv_end_address)
    TextView tv_end_address;

    @Bind(R.id.tv_deduction)
    TextView tv_deduction;

    @Bind(R.id.iv_purpose)
    ImageView iv_purpose;

    @Bind(R.id.tv_distance_unit)
    TextView tv_distance_unit;

    @Bind(R.id.swipe)
    SwipeLayout swipe_layout;
    @Bind(R.id.ll_personal)
    LinearLayout ll_personal;
    @Bind(R.id.ll_medical)
    LinearLayout ll_medical;
    @Bind(R.id.ll_charity)
    LinearLayout ll_charity;

    public ViewHolderAllTripCard(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        swipe_layout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipe_layout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
                YoYo.with(Techniques.Tada).duration(Constants.ANIMATION_DURATION).delay(Constants.ANIMATION_DELAY).playOn(layout.findViewById(R.id.iv_personal));
                YoYo.with(Techniques.Tada).duration(Constants.ANIMATION_DURATION).delay(Constants.ANIMATION_DELAY).playOn(layout.findViewById(R.id.iv_charity));
                YoYo.with(Techniques.Tada).duration(Constants.ANIMATION_DURATION).delay(Constants.ANIMATION_DELAY).playOn(layout.findViewById(R.id.iv_medical));
                TripTabFragment.getInstance().mItemTouchHelper.attachToRecyclerView(null);
            }

            @Override
            public void onClose(SwipeLayout layout) {
                TripTabFragment.getInstance().mItemTouchHelper.attachToRecyclerView(TripTabFragment.getInstance().mRecyclerView);
            }
        });
    }
}
