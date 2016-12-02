package com.example.ruby.getgps.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.ui.adapters.AllTripCardsAdapter;
import com.example.ruby.getgps.ui.adapters.NewTripCardsAdapter;
import com.example.ruby.getgps.ui.adapters.ViewHolderAllTripCard;
import com.example.ruby.getgps.ui.adapters.ViewHolderLiveMap;


public class TouchHelper extends ItemTouchHelper.Callback {

    private NewTripCardsAdapter mNewTripCardsAdapter;
    private AllTripCardsAdapter mAllTripCardsAdapter;
    private final boolean mSwipable = true;

    public TouchHelper(NewTripCardsAdapter mNewTripCardsAdapter, AllTripCardsAdapter mAllTripCardsAdapter) {
        this.mNewTripCardsAdapter = mNewTripCardsAdapter;
        this.mAllTripCardsAdapter = mAllTripCardsAdapter;
    }

    public TouchHelper() {
    }

    public void setmNewTripCardsAdapter(NewTripCardsAdapter mNewTripCardsAdapter) {
        this.mNewTripCardsAdapter = mNewTripCardsAdapter;
    }

    public void setmAllTripCardsAdapter(AllTripCardsAdapter mAllTripCardsAdapter) {
        this.mAllTripCardsAdapter = mAllTripCardsAdapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof ViewHolderLiveMap)) {
            return makeMovementFlags(ItemTouchHelper.LEFT, ItemTouchHelper.RIGHT);
        }
        return 0;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            Paint paint = new Paint();
            paint.setColor(ContextCompat.getColor(recyclerView.getContext(), R.color.colorPrimaryEv));
            Bitmap bitmap;
            bitmap = BitmapFactory.decodeResource(
                    recyclerView.getContext().getResources(), R.drawable.ic_work_white);
            float smallMargin = recyclerView.getContext().getResources().getDimension(R.dimen.xxsmall_margin);
            String work = recyclerView.getContext().getResources().getString(R.string.str_work);
            if (dX > 0) { // swiping right
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop() + smallMargin, dX,
                        (float) itemView.getBottom() - smallMargin, paint);
            }

            int bitmapY = itemView.getTop() +
                    ( itemView.getBottom() -  itemView.getTop() - bitmap.getHeight())/2 - (int) recyclerView.getContext().getResources().getDimension(R.dimen.medium_text_size);

            c.drawBitmap(bitmap,
                    (float) itemView.getLeft() + recyclerView.getContext().getResources().getDimension(R.dimen.xxlarge_margin),
                    bitmapY,
                    paint);
            paint.setTextSize(recyclerView.getContext().getResources().getDimension(R.dimen.medium_text_size));
            paint.setColor(ContextCompat.getColor(recyclerView.getContext(), android.R.color.white));
            int drawTextY = itemView.getTop() +
                    ( itemView.getBottom() -  itemView.getTop() - bitmap.getHeight())/2+ (int) recyclerView.getContext().getResources().getDimension(R.dimen.xxlarge_margin);
            c.drawText(work, (float) itemView.getLeft() + recyclerView.getContext().getResources().getDimension(R.dimen.xxlarge_margin),
                    drawTextY, paint);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return mSwipable;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (viewHolder instanceof ViewHolderAllTripCard){
            mAllTripCardsAdapter.onSwipeRight(viewHolder.getAdapterPosition());
        } else {
            mNewTripCardsAdapter.onSwipeRight(viewHolder.getAdapterPosition());
        }
    }


}