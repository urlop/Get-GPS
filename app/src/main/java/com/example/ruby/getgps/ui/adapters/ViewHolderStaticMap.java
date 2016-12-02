package com.example.ruby.getgps.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.example.ruby.getgps.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Custom ViewHolder for simple trip CardView
 *
 * @see android.support.v7.widget.RecyclerView.ViewHolder
 */
class ViewHolderStaticMap extends RecyclerView.ViewHolder {

    @Bind(R.id.iv_trip_map)
    ImageView iv_trip_map;
    @Bind(R.id.tv_start_address)
    TextView tv_start_address;
    @Bind(R.id.tv_end_address)
    TextView tv_end_address;
    protected
    @Bind(R.id.tv_start_hour)
    TextView tv_start_hour;
    protected
    @Bind(R.id.tv_end_hour)
    TextView tv_end_hour;
    @Bind(R.id.cv_tv_trip_distance)
    TextView cv_tv_trip_distance;
    @Bind(R.id.cv_tv_trip_deduction)
    TextView cv_tv_trip_deduction;
    @Bind(R.id.iv_delete)
    ImageView iv_delete;
    @Bind(R.id.swipe)
    SwipeLayout swipe_layout;
    @Bind(R.id.ll_personal)
    LinearLayout ll_personal;
    @Bind(R.id.ll_medical)
    LinearLayout ll_medical;
    @Bind(R.id.ll_charity)
    LinearLayout ll_charity;
    @Bind(R.id.tv_month)
    TextView tv_month;
    @Bind(R.id.tv_day)
    TextView tv_day;

    public ViewHolderStaticMap(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}
