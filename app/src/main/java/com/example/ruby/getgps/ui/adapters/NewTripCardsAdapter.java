package com.example.ruby.getgps.ui.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.ui.activities.MyWorkSourcesActivity;
import com.example.ruby.getgps.ui.fragments.HomeTabFragment;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.GeneralHelper;
import com.example.ruby.getgps.utils.TimeHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * For CardViews in TripTabFragment's list
 */
public class NewTripCardsAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {

    private ViewHolderLiveMap viewHolderLiveMap;
    private ArrayList<Trip> arrayTrips;
    private final Context context;
    private final SwipeItemRecyclerMangerImpl mItemManager = new SwipeItemRecyclerMangerImpl(this);
    private ViewHolderStaticMap viewHolderStaticMap;

    public NewTripCardsAdapter(ArrayList<Trip> arrayTrips, Context context) {
        this.arrayTrips = arrayTrips;
        if (this.arrayTrips == null) {
            this.arrayTrips = new ArrayList<>();
        }
        this.context = context;
    }

    /**
     * Puts a map layout to the first card.
     * And a simple layout with CardView on the rest
     *
     * @param parent   ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType view type of the new View. Differences a map view from the rest
     * @return respective view holder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Timber.d("method=onCreateViewHolder");
        if (viewType == Constants.TRIP_CARD_VIEW_TYPE_LIVE) {
            Timber.d("method=onCreateViewHolder viewType=0");
            if (viewHolderLiveMap == null) {
                Timber.d("method=onCreateViewHolder viewType=0 viewHolderLiveMap == null");
                try {
                    View cardViewLive = inflater.inflate(R.layout.cardview_live_trip, parent, false);
                    viewHolderLiveMap = new ViewHolderLiveMap(cardViewLive);
                } catch (InflateException e) {
                    Timber.e(e, "method=onCreateViewHolder error=Problem when inflating live card UI");
                } catch (Exception e){
                    Timber.e(e, "method=onCreateViewHolder error=Error when creating live card UI");
                }
            }
            viewHolder = viewHolderLiveMap;
        } else {
            View cardViewStatic = inflater.inflate(R.layout.cardview_new_trip, parent, false);
            viewHolder = new ViewHolderStaticMap(cardViewStatic);
        }
        return viewHolder;
    }

    /**
     * Displays the data at the specified position.
     * This method should update the contents of the itemView to reflect the item at the given position.
     *
     * @param holder   ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == Constants.TRIP_CARD_VIEW_TYPE_LIVE) {
            viewHolderLiveMap = (ViewHolderLiveMap) holder;
        } else {
            viewHolderStaticMap = (ViewHolderStaticMap) holder;
            setStaticMapListeners(viewHolderStaticMap, position);
        }
    }

    /**
     * Assigns values to elements inside default view
     *
     * @param holder   ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position position of the item within the adapter's data set.
     */
    private void setStaticMapElements(final ViewHolderStaticMap holder, final int position) {
        final Trip trip = arrayTrips.get(position);
        holder.cv_tv_trip_deduction.setText(context.getString(R.string.money, trip.getDeduction()));
        holder.tv_start_hour.setText(TimeHelper.dateTo12TimeFormat(trip.getStartedAt()));
        holder.tv_end_hour.setText(TimeHelper.dateTo12TimeFormat(trip.getEndedAt()));
        holder.tv_start_address.setText(trip.getFrom());
        holder.tv_end_address.setText(trip.getTo());
        holder.cv_tv_trip_distance.setText(String.valueOf(trip.getMiles()));
        holder.tv_month.setText(TimeHelper.getMonth(trip.getDateFormatted()));
        holder.tv_day.setText(TimeHelper.getDay(trip.getDateFormatted()));
        if (trip.getMapboxUrl() != null && !trip.getMapboxUrl().trim().isEmpty()) {
            Picasso.with(context).load(trip.getMapboxUrl()).fit().centerCrop().into(holder.iv_trip_map);
        }
    }

    /**
     * Assigns listeners for actions to Static map view
     *
     * @param viewHolderStaticMap reference from the static map element
     */
    private void setStaticMapListeners(final ViewHolderStaticMap viewHolderStaticMap, int position) {
        viewHolderStaticMap.swipe_layout.setShowMode(SwipeLayout.ShowMode.LayDown);
        viewHolderStaticMap.swipe_layout.addSwipeListener(new SimpleSwipeListener() {
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
        viewHolderStaticMap.ll_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwipeClick(viewHolderStaticMap, context.getResources().getString(R.string.str_personal));
            }
        });
        viewHolderStaticMap.ll_charity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwipeClick(viewHolderStaticMap, context.getResources().getString(R.string.str_charity));
            }
        });
        viewHolderStaticMap.ll_medical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwipeClick(viewHolderStaticMap, context.getResources().getString(R.string.str_medical));
            }
        });

        final Trip trip = arrayTrips.get(position);
        viewHolderStaticMap.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TripHelper.deleteTrip(context, trip, false, viewHolderStaticMap.getAdapterPosition());
                if (TripTabFragment.getInstance().getMainActivity().getUser() != null && HomeTabFragment.getInstance().isAdded()) {
                    GeneralHelper.substractingMilesUnclassifiedTrips(TripTabFragment.getInstance().getMainActivity().getUser(), trip);
                    HomeTabFragment.getInstance().setUpPieChart();
                }
            }
        });

        setStaticMapElements(viewHolderStaticMap, position);
        mItemManager.bindView(viewHolderStaticMap.itemView, position);
    }

    /**
     * Return the view type of the item at position for the purposes of view recycling.
     * The default implementation of this method returns 0, making the assumption of a single view type for the adapter.
     * Unlike ListView adapters, types need not be contiguous. Consider using id resources to uniquely identify item view types.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at position. Type codes need not be contiguous. 0 if array element is empty = mapView. 1 if array element is a normal trip
     */
    @Override
    public int getItemViewType(int position) {
        if (arrayTrips.get(position) == null) { //null trip == live map
            return Constants.TRIP_CARD_VIEW_TYPE_LIVE;
        }
        return Constants.TRIP_CARD_VIEW_TYPE_STATIC;
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return arrayTrips.size();
    }

    /**
     * Checks if a live card is already added, if not, adds one
     */
    public void addLiveCardToArray() {
        boolean liveCardAdded = false;
        for (Trip trip : arrayTrips) {
            if (trip == null) {
                liveCardAdded = true;
            }
        }
        if (!liveCardAdded) {
            arrayTrips.add(0, null);
            if (viewHolderLiveMap != null) {
                viewHolderLiveMap.tv_trip_distance.setText(context.getString(R.string.default_card_miles));
                viewHolderLiveMap.tv_trip_deduction.setText(context.getString(R.string.default_card_deduction));
            }
        }
        TripTabFragment.getInstance().scrollToTop();
    }

    /**
     * Removes the liveCard from the array
     */
    public void removeLiveCardFromArray() {
        arrayTrips.remove(null);
    }

    /**
     * Adds the trip from the response into the array
     *
     * @param trip Trip from the webservice response when posting a trip
     */
    public void addNewPostedTrip(Trip trip) {
        arrayTrips.add(arrayTrips.contains(null) ? 1 : 0, trip);
    }

    public ViewHolderLiveMap getViewHolderLiveMap() {
        return viewHolderLiveMap;
    }

    /**
     * Sets viewHolderLiveMap null to evade java.lang.IllegalStateException
     */
    public void nullifyViewHolderLiveMap() {
        viewHolderLiveMap = null;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    private void updateTrip(Trip trip) {
        Call<Trip> call = RequestManager.getDefault(context).updateTrip(trip.getTokenId(), trip);
        call.enqueue(new CustomCallback<Trip>(context, call) {
            @Override
            public void onResponse(Call<Trip> call, Response<Trip> response) {
                super.onResponse(call, response);
                if (response.isSuccessful() & response.body() != null) {
                    updateTripClassification(response.body());
                    if (TripTabFragment.getInstance().getMainActivity().getUser() != null && HomeTabFragment.getInstance().isAdded()) {
                        GeneralHelper.onSwipeUpdateUserMiles(context, response.body(), TripTabFragment.getInstance().getMainActivity().getUser());
                        HomeTabFragment.getInstance().setUpPieChart();
                    }
                }
            }
        });
    }

    private void updateTripClassification(Trip trip) {
        for (Trip allTrip : TripTabFragment.getInstance().getAllTripCardsAdapter().getArrayTrips()) {
            if (allTrip.getTokenId().equals(trip.getTokenId())) {
                allTrip.setPurpose(trip.getPurpose());
                notifyItemChanged(TripTabFragment.getInstance().getAllTripCardsAdapter().getArrayTrips().indexOf(allTrip));
            }
        }
    }

    public void onSwipeRight(int position) {
        MainActivity mainActivity = (MainActivity) context;

        Timber.d("method=onSwipeRight position=%d", position);
        Trip trip = arrayTrips.get(position);
        trip.setPurpose(context.getResources().getString(R.string.str_work));

        notifyItemChanged(position);

        if (mainActivity.getUser().getIncomeSources().isEmpty()) {
            arrayTrips.remove(trip);
            announceItemHasBeenRemoved(position);
        } else { //render to MyWorkSourcesActivity if user has one or more income sources
            Intent intent = new Intent(context, MyWorkSourcesActivity.class);
            intent.putExtra(Constants.TRIP_WORK_SOURCE_USER_EXTRA, mainActivity.getUser());
            intent.putExtra(Constants.TRIP_WORK_SOURCE_TRIP_EXTRA, trip);
            mainActivity.startActivityForResult(intent, Constants.TRIP_WORK_SOURCE);
        }
    }

    private void onSwipeClick(ViewHolderStaticMap viewHolderStaticMap, String purpose) {
        if (viewHolderStaticMap.getAdapterPosition() < arrayTrips.size()) {
            int staticMapPosition = viewHolderStaticMap.getAdapterPosition();
            mItemManager.removeShownLayouts(viewHolderStaticMap.swipe_layout);
            Trip trip = arrayTrips.get(staticMapPosition);
            trip.setPurpose(purpose);
            updateTrip(trip);

            arrayTrips.remove(staticMapPosition);
            announceItemHasBeenRemoved(staticMapPosition);

            mItemManager.closeAllItems();
            TripTabFragment.getInstance().mItemTouchHelper.attachToRecyclerView(TripTabFragment.getInstance().mRecyclerView);
        }
    }

    /**
     * Tells all interested views that list's data has been modified.
     */
    public void announceDataHasChanged() {
        try {
            Timber.d("method=announceDataHasChanged");
            notifyDataSetChanged();
            TripTabFragment.getInstance().setupCardViewMessage(arrayTrips.isEmpty(), true);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e(e, "method=announceDataHasChanged");
        }
    }

    /**
     * Tells all interested views that an item of the list's data has been removed.
     *
     * @param adapterPosition int which represents de viewHolderStaticMap's adapterPosition. Will be used to #notifyItemRemoved
     */
    private void announceItemHasBeenRemoved(int adapterPosition) {
        notifyItemRemoved(adapterPosition);
        TripTabFragment.getInstance().setupCardViewMessage(arrayTrips.isEmpty(), true);
    }

    /**
     * Tells that corresponding trip's income source has been removed.
     */
    public void announceTripWorkSourceHasBeenRemoved(Trip trip) {
        for (Trip t : arrayTrips) {
            if(t.getTokenId().equals(trip.getTokenId())){
                int position = arrayTrips.indexOf(t);
                arrayTrips.remove(position);
                announceItemHasBeenRemoved(position);
                break;
            }
        }
    }

    public void removeStaticMap(int position) {
        arrayTrips.remove(position);
        announceItemHasBeenRemoved(position);
    }

    public void removeTripFromArray(String tokenId) {
        for (int i = 0; i < arrayTrips.size(); i++) {
            if (arrayTrips.get(i).getTokenId().equals(tokenId)) {
                arrayTrips.remove(i);
            }
        }
        announceDataHasChanged();
    }

    public void removeStaticMap(String tripId) {
        int position = -1;
        for (Trip t : arrayTrips) {
            if (t != null && t.getTokenId().equals(tripId)) {
                position = arrayTrips.indexOf(t);
            }
        }
        if (position >= 0) {
            arrayTrips.remove(position);
            announceItemHasBeenRemoved(position);
        }
    }

    /**
     * Clean all elements of the recycler
     */
    public boolean clear() {
        boolean liveCardAdded = false;
        if (arrayTrips.contains(null)) {
            liveCardAdded = true;
        }
        arrayTrips.clear();
        notifyDataSetChanged();
        return liveCardAdded;
    }

    /**
     * Add a list of items
     *
     * @param list list of trips from the webservice
     */
    public void addAll(List<Trip> list, boolean liveCardAdded) {
        arrayTrips.addAll(list);
        if (liveCardAdded) {
            addLiveCardToArray();
        }
        notifyDataSetChanged();
    }

}
