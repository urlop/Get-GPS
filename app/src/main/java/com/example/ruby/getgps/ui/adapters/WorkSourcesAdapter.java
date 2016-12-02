package com.example.ruby.getgps.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.WorkSource;

import java.util.List;

public class WorkSourcesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<WorkSource> arrayElements;
    private final Context context;
    private List<String> userBusinessLine;

    public WorkSourcesAdapter(List<WorkSource> arrayElements, Context context) {
        this.arrayElements = arrayElements;
        this.context = context;
    }

    public WorkSourcesAdapter(List<WorkSource> arrayElements, Context context, List<String> userBusinessLine) {
        this.arrayElements = arrayElements;
        this.context = context;
        this.userBusinessLine = userBusinessLine;
        fillUserWorkSources();
    }

    private void fillUserWorkSources() {
        for (String incomeSources : userBusinessLine) {
            for (WorkSource workSource : arrayElements) {
                if (incomeSources.equals(workSource.getName())) {
                    workSource.setSelected(true);
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View containerView = inflater.inflate(R.layout.item_work_sources, parent, false);
        return new ViewHolderWorkSource(containerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final WorkSource element = arrayElements.get(position);

        ViewHolderWorkSource viewHolder = (ViewHolderWorkSource) holder;
        viewHolder.tv_ws_name.setText(element.getName());
        if (element.getIcon() != null) {
            viewHolder.iv_ws_icon.setImageDrawable(element.getIcon());
            viewHolder.tv_ws_icon.setText("");
        } else {
            String[] nameWords = element.getName().split(" ");
            String nameInitials = String.valueOf(nameWords[0].charAt(0)) + (nameWords.length > 1 ? String.valueOf(nameWords[1].charAt(0)) : "");
            viewHolder.tv_ws_icon.setText(nameInitials);
            viewHolder.iv_ws_icon.setImageResource(android.R.color.transparent);
        }
        viewHolder.ib_ws_circle.getBackground().setColorFilter(
                element.isSelected() ? Color.parseColor(element.getColor()) :
                        ContextCompat.getColor(context, R.color.ripple),
                PorterDuff.Mode.MULTIPLY);

        viewHolder.ib_ws_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                element.setSelected(!element.isSelected());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayElements.size();
    }
}
