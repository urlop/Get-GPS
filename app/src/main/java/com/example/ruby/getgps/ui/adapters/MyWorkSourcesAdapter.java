package com.example.ruby.getgps.ui.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.WorkSource;
import com.example.ruby.getgps.ui.activities.MyWorkSourcesActivity;

import java.util.List;

public class MyWorkSourcesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<WorkSource> arrayElements;
    private final MyWorkSourcesActivity activity;

    public MyWorkSourcesAdapter(List<WorkSource> arrayElements, MyWorkSourcesActivity activity) {
        this.arrayElements = arrayElements;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View containerView = inflater.inflate(R.layout.item_my_work_sources, parent, false);
        return new ViewHolderWorkSource(containerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final WorkSource element = arrayElements.get(position);

        ViewHolderWorkSource viewHolder = (ViewHolderWorkSource) holder;
        viewHolder.tv_ws_name.setText(element.getName());
        if (element.getIcon() != null){
            viewHolder.iv_ws_icon.setImageDrawable(element.getIcon());
            viewHolder.tv_ws_icon.setText("");
        } else {
            String[] nameWords = element.getName().split(" ");
            String nameInitials = String.valueOf(nameWords[0].charAt(0)) + (nameWords.length > 1 ? String.valueOf(nameWords[1].charAt(0)) : "");
            viewHolder.tv_ws_icon.setText(nameInitials);
            viewHolder.iv_ws_icon.setImageResource(android.R.color.transparent);
        }
        viewHolder.ib_ws_circle.getBackground().setColorFilter(Color.parseColor(element.getColor()), PorterDuff.Mode.MULTIPLY);

        viewHolder.ib_ws_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.sendTripWorkSource(!element.getName().equals(activity.getString(R.string.mws_none)) ? element.getName() : "");
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayElements.size();
    }
}
