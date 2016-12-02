package com.example.ruby.getgps.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ruby.getgps.R;

import butterknife.Bind;
import butterknife.ButterKnife;


class ViewHolderWorkSource extends RecyclerView.ViewHolder {

    @Bind(R.id.ib_ws_circle)
    ImageButton ib_ws_circle;

    @Bind(R.id.iv_ws_icon)
    ImageView iv_ws_icon;

    @Bind(R.id.tv_ws_name)
    TextView tv_ws_name;

    @Bind(R.id.tv_ws_icon)
    TextView tv_ws_icon;

    public ViewHolderWorkSource(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
