package com.example.ruby.getgps.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.ui.activities.EditTripActivity;
import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.ui.activities.MyWorkSourcesActivity;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.TimeHelper;
import com.example.ruby.getgps.utils.TripHelper;

import java.util.List;

import timber.log.Timber;

public class AllTripCardsAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

    private final List<Trip> arrayTrips;
    private final Context context;
    private final String personal;
    private final String charity;
    private final String medical;
    private final String work;

    public AllTripCardsAdapter(List<Trip> arrayTrips, Context context) {
        this.arrayTrips = arrayTrips;
        this.context = context;

        if (context != null) {
            personal = context.getResources().getString(R.string.str_personal);
            charity = context.getResources().getString(R.string.str_charity);
            medical = context.getResources().getString(R.string.str_medical);
            work = context.getResources().getString(R.string.str_work);
        }else{
            personal = charity = medical = work = "";
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cardViewAll = inflater.inflate(R.layout.cardview_all_trip, parent, false);
        return new ViewHolderAllTripCard(cardViewAll);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Trip trip = arrayTrips.get(position);
        String purpose = trip.getPurpose();
        ViewHolderAllTripCard viewHolderAllTripCard = (ViewHolderAllTripCard) holder;
        viewHolderAllTripCard.tv_deduction.setText(context.getString(R.string.money, trip.getDeduction()));
        viewHolderAllTripCard.tv_miles.setText(String.valueOf(trip.getMiles()));
        viewHolderAllTripCard.tv_start_address.setText(trip.getFrom());
        viewHolderAllTripCard.tv_end_address.setText(trip.getTo());
        viewHolderAllTripCard.tv_start_time.setText(TimeHelper.dateTo12TimeFormat(trip.getStartedAt()));
        viewHolderAllTripCard.tv_end_time.setText(TimeHelper.dateTo12TimeFormat(trip.getEndedAt()));
        if (purpose != null && purpose.equals(context.getString(R.string.str_work))) {
            viewHolderAllTripCard.tv_purpose.setText(trip.getIncomeSource() == null || trip.getIncomeSource().isEmpty() ? purpose : trip.getIncomeSource());
        }else{
            viewHolderAllTripCard.tv_purpose.setText(
                    purpose != null && !purpose.trim().isEmpty() ? purpose : context.getString(R.string.trip_uncategorized));
        }
        setStaticMapListeners(viewHolderAllTripCard, position);
        checkPurposeOfTrip(purpose, viewHolderAllTripCard);

        mItemManger.bindView(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return arrayTrips.size();
    }


    private void checkPurposeOfTrip(String purpose, ViewHolderAllTripCard viewHolderAllTripCard) {
        if (purpose == null) {
            viewHolderAllTripCard.iv_purpose.setImageResource(R.drawable.ic_unclassified_gray);
            viewHolderAllTripCard.tv_purpose.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_grey));
            viewHolderAllTripCard.tv_miles.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_grey));
            viewHolderAllTripCard.tv_distance_unit.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_grey));
        }else if (purpose.equalsIgnoreCase(context.getString(R.string.str_work))) {
            viewHolderAllTripCard.iv_purpose.setImageResource(R.drawable.ic_work_green);
            viewHolderAllTripCard.tv_purpose.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryEv));
            viewHolderAllTripCard.tv_miles.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryEv));
            viewHolderAllTripCard.tv_distance_unit.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryEv));
        } else if (purpose.equalsIgnoreCase(context.getString(R.string.str_personal))) {
            viewHolderAllTripCard.iv_purpose.setImageResource(R.drawable.ic_personal_blue);
            viewHolderAllTripCard.tv_purpose.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_blue));
            viewHolderAllTripCard.tv_miles.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_blue));
            viewHolderAllTripCard.tv_distance_unit.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_blue));
        } else {
            viewHolderAllTripCard.iv_purpose.setImageResource(R.drawable.ic_heart_orange);
            viewHolderAllTripCard.tv_purpose.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_orange));
            viewHolderAllTripCard.tv_miles.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_orange));
            viewHolderAllTripCard.tv_distance_unit.setTextColor(ContextCompat.getColor(context, R.color.pie_chart_orange));
        }
    }

    /**
     * Assigns listeners for actions to Card view
     *
     * @param viewHolderAllTripCard reference from the card element
     */
    private void setStaticMapListeners(final ViewHolderAllTripCard viewHolderAllTripCard, final int position) {
        viewHolderAllTripCard.ll_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwipeClick(personal, viewHolderAllTripCard);
            }
        });
        viewHolderAllTripCard.ll_charity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwipeClick(charity, viewHolderAllTripCard);
            }
        });
        viewHolderAllTripCard.ll_medical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwipeClick(medical, viewHolderAllTripCard);
            }
        });

        viewHolderAllTripCard.cv_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trip trip = arrayTrips.get(position);
                Intent intent = new Intent(context, EditTripActivity.class);
                intent.putExtra(Constants.VIEW_HOLDER_POSITION, viewHolderAllTripCard.getAdapterPosition());
                intent.putExtra(Constants.TRIP_OBJECT_EXTRA, trip);
                context.startActivity(intent);
            }
        });
    }

    public void onSwipeRight(int position) {
        MainActivity mainActivity = (MainActivity) context;
        Intent intent = new Intent(context, MyWorkSourcesActivity.class);

        Timber.d("method=onSwipeRight position='%s'", position);
        Trip trip = arrayTrips.get(position);
        trip.setIncomeSource(null);

        //render to MyWorkSourcesActivity if user has one or more income sources
        if (!mainActivity.getUser().getIncomeSources().isEmpty()) {
            intent.putExtra(Constants.TRIP_WORK_SOURCE_USER_EXTRA, mainActivity.getUser());
            intent.putExtra(Constants.TRIP_WORK_SOURCE_TRIP_EXTRA, trip);
            mainActivity.startActivityForResult(intent, Constants.TRIP_WORK_SOURCE);
        }else if (trip.getPurpose() == null || !trip.getPurpose().equals(work)) {
            String prevPurpose = trip.getPurpose();
            trip.setPurpose(work);
            TripHelper.updateTrip(context, trip, prevPurpose);
        }
        notifyItemChanged(position);
    }

    private void onSwipeClick(String purpose, final ViewHolderAllTripCard viewHolder) {
        if (viewHolder.getAdapterPosition() < arrayTrips.size()) {
            final int position = viewHolder.getAdapterPosition();

            Trip trip = arrayTrips.get(position);
            if (trip.getPurpose() == null || !trip.getPurpose().equals(purpose)) {   //null represents Unclassified
                String prevPurpose = trip.getPurpose();
                trip.setPurpose(purpose);
                TripHelper.updateTrip(context, trip, prevPurpose);
                notifyDataSetChanged();

                //Notify changes to UI after a delay
                final Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    public void run() {
                        viewHolder.swipe_layout.close();
                    }
                }, 250);
            } else {
                viewHolder.swipe_layout.close();
            }
        }
    }

    /**
     * Removes Static card from adapter
     *
     * @param position position of card
     */
    public void removeStaticMap(int position) {
        arrayTrips.remove(position);
        notifyItemRemoved(position);
    }

    public void removeTripFromArray(String tokenId) {
        for (int i = 0; i < arrayTrips.size(); i++) {
            if (arrayTrips.get(i).getTokenId().equals(tokenId)) {
                arrayTrips.remove(i);
            }
        }
        announceDataHasChanged();
    }

    /**
     * Tells all interested views that list's data has been modified.
     */
    public void announceDataHasChanged() {
        notifyDataSetChanged();
        TripTabFragment.getInstance().setupCardViewMessage(arrayTrips.isEmpty(), true);
    }

    /**
     * Tells that corresponding trip's income source has been changed.
     */
    public void announceTripWorkSourceHasBeenUpdated(Trip trip) {
        for (Trip t : arrayTrips) {
            if(t.getTokenId().equals(trip.getTokenId())){
                int position = arrayTrips.indexOf(t);
                t.setPurpose(trip.getPurpose());
                t.setIncomeSource(trip.getIncomeSource());
                notifyItemChanged(position);
                break;
            }
        }
    }

    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public List<Trip> getArrayTrips() {
        return arrayTrips;
    }

    public void addNewPostedTrip(Trip trip) {
        Timber.d("method=addNewPostedTrip newTripPurpose='%s'", trip.getPurpose());
        arrayTrips.add(0, trip);
    }

    /**
     * Clean all elements of the recycler
     */
    public void clear() {
        if (arrayTrips != null) {
            arrayTrips.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * Add a list of items
     *
     * @param list list of trips from the webservice
     */
    public void addAll(List<Trip> list) {
        arrayTrips.addAll(list);
        notifyDataSetChanged();
    }

}
