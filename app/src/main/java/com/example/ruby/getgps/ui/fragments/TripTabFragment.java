package com.example.ruby.getgps.ui.fragments;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.ui.adapters.AllTripCardsAdapter;
import com.example.ruby.getgps.ui.adapters.NewTripCardsAdapter;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.TouchHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * View for showing and managing all user trips
 *
 * @see Fragment
 */
public class TripTabFragment extends Fragment {

    private static TripTabFragment fragment;
    private NewTripCardsAdapter newTripCardsAdapter;
    private AllTripCardsAdapter allTripCardsAdapter;
    private MainActivity mainActivity;
    public RecyclerView mRecyclerView;
    public ArrayList<Trip> newTripsArrayList;
    public List<Trip> allTripsArrayList;
    private List<LatLng> polyLinePoints;
    private TouchHelper mTouchHelper;
    public ItemTouchHelper mItemTouchHelper;
    private GoogleMap googleMap;
    private TextView tv_trip_distance;
    private TextView tv_trip_deduction;

    private PolylineOptions mPolylineOptions;

    @Bind(R.id.bt_new_trips)
    Button bt_new_trips;
    @Bind(R.id.bt_all_trips)
    Button bt_all_trips;
    @Bind(R.id.progress_bar)
    public ProgressBar progress_bar;
    @Bind(R.id.swipeRefreshLayout)
    protected SwipeRefreshLayout swipeRefreshLayout;

    //CardView Message
    @Bind(R.id.ll_cv_message_container)
    View ll_cv_message_container;
    @Bind(R.id.iv_cv_message)
    ImageView iv_cv_message;
    @Bind(R.id.tv_cv_message_title)
    TextView tv_cv_message_title;
    @Bind(R.id.tv_cv_message_description)
    TextView tv_cv_message_description;

    private ColorStateList defaultTextViewColor;

    public static TripTabFragment getInstance() {
        if (fragment != null) {
            return fragment;
        } else {
            fragment = new TripTabFragment();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newTripsArrayList = new ArrayList<>();
        allTripsArrayList = new ArrayList<>();
        polyLinePoints = new ArrayList<>();
        mainActivity = (MainActivity) getActivity();
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and non-graphical fragments can return null (which is the default implementation).
     * <p/>
     * Sets RecyclerView with information from the webservice
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_tab, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_trips);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        setupTripGroupButtons(true);
        defaultTextViewColor = tv_cv_message_description.getTextColors();
        setSwipeRefreshLayout();
        return view;
    }

    public void setSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTrips();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryEv);
    }

    private void refreshTrips() {
        final boolean uncategorized;
        uncategorized = !(mRecyclerView.getAdapter() instanceof AllTripCardsAdapter);
        Call<ArrayList<Trip>> call = RequestManager.getDefault(getContext()).getTrips(uncategorized, 1);
        call.enqueue(new CustomCallback<ArrayList<Trip>>(getContext(), call) {
            @Override
            public void onResponse(Call<ArrayList<Trip>> call, Response<ArrayList<Trip>> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    if (uncategorized) {
                        boolean liveCardAdded = newTripCardsAdapter.clear();
                        newTripCardsAdapter.addAll(response.body(), liveCardAdded);
                    } else {
                        allTripCardsAdapter.clear();
                        allTripCardsAdapter.addAll(response.body());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Adds a live card if a trip is going
     */
    private void checkIfTripIsGoing() {
        Timber.d("method=checkIfTripIsGoing isTripOngoing='%s'", TripHelper.tripOngoing());
        if (TripHelper.tripOngoing() != null && TripTabFragment.getInstance().getNewTripCardsAdapter() != null) {
            TripTabFragment.getInstance().getNewTripCardsAdapter().addLiveCardToArray();
            TripTabFragment.getInstance().getNewTripCardsAdapter().announceDataHasChanged();
        }
    }

    public void setUpRecyclerView(boolean isNewTripCardAdapter) {
        Timber.d("method=setUpRecyclerView isNewTripCardAdapter=%b", isNewTripCardAdapter);
        if (mTouchHelper == null) {
            mTouchHelper = new TouchHelper();
            mItemTouchHelper = new ItemTouchHelper(mTouchHelper);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }

        if (newTripCardsAdapter == null) {
            newTripCardsAdapter = new NewTripCardsAdapter(newTripsArrayList, mainActivity);
            mTouchHelper.setmNewTripCardsAdapter(newTripCardsAdapter);
        }
        if (allTripCardsAdapter == null) {
            allTripCardsAdapter = new AllTripCardsAdapter(allTripsArrayList, mainActivity);
            mTouchHelper.setmAllTripCardsAdapter(allTripCardsAdapter);
        }

        if (isNewTripCardAdapter) {
            mRecyclerView.setAdapter(newTripCardsAdapter);
            Timber.d("method=setUpRecyclerView action=isNewTripCardAdapter adapterItemCount=%d", newTripCardsAdapter.getItemCount());
            setupCardViewMessage(newTripsArrayList.isEmpty(), true);
        } else {
            mRecyclerView.setAdapter(allTripCardsAdapter);
            setupCardViewMessage(allTripsArrayList.isEmpty(), false);
        }

        checkIfTripIsGoing();
    }

    public void setupCardViewMessage(boolean mustBeShowed, boolean isNewTripCard) {
        if (mustBeShowed) {
            Timber.d("method=setupCardViewMessage action=mustBeShowed");
            String[] titles = getResources().getStringArray(R.array.cv_message_titles);
            String[] descriptions = getResources().getStringArray(R.array.cv_message_descriptions);

            ll_cv_message_container.setVisibility(View.VISIBLE);
            if (isNewTripCard) {
                if (!PreferencesManager.getInstance(getContext()).autoClassifyStateIsOff(getContext())) {
                    Timber.d("method=setupCardViewMessage action='ll_cv_message_container show autoClassify'");
                    String classifyState = PreferencesManager.getInstance(getContext()).getAutoClassifyState();
                    boolean isWork = classifyState.equals(getContext().getString(R.string.str_work));
                    iv_cv_message.setVisibility(View.VISIBLE);
                    iv_cv_message.setImageResource(isWork ? R.drawable.ic_undo_green : R.drawable.ic_undo_blue);
                    tv_cv_message_title.setText(isWork ? titles[0] : titles[1]);
                    tv_cv_message_title.setTextColor(isWork ? ContextCompat.getColor(getContext(), R.color.colorPrimaryEv) : ContextCompat.getColor(getContext(), R.color.pie_chart_blue));
                    tv_cv_message_description.setText(isWork ? descriptions[0] : descriptions[1]);
                    tv_cv_message_description.setTextColor(defaultTextViewColor);

                } else {
                    ll_cv_message_container.setVisibility(View.GONE); //TODO: remove this line
                    //TODO: set Automatic Trip Detection is ON/OFF
                }
            } else {
                iv_cv_message.setVisibility(View.GONE);
                tv_cv_message_title.setText(titles[4]);
                tv_cv_message_title.setTextColor(ContextCompat.getColor(getContext(), R.color.color_red));
                tv_cv_message_description.setText(descriptions[4]);
                tv_cv_message_description.setTextColor(ContextCompat.getColor(getContext(), R.color.color_red));
            }
        } else {
            Timber.d("method=setupCardViewMessage action='ll_cv_message_container gone'");
            ll_cv_message_container.setVisibility(View.GONE);
        }
    }


    /**
     * Draws current trip in the newTripCardsAdapter.viewHolderLiveMap.googleMap
     *
     * @param latLng new latitude and longitude to be drawn
     * @see NewTripCardsAdapter
     * @see com.example.ruby.getgps.ui.adapters.ViewHolderLiveMap
     * @see GoogleMap#addPolyline(PolylineOptions)
     */
    public void drawPolyLine(LatLng latLng) {
        setupTripCardView();
        googleMap.addPolyline(mPolylineOptions.add(latLng));
        polyLinePoints.add(latLng);
    }

    private void setupTripCardView() {
        if (googleMap == null) {
            googleMap = newTripCardsAdapter.getViewHolderLiveMap().googleMap;
        }
        if (tv_trip_distance == null) {
            tv_trip_distance = newTripCardsAdapter.getViewHolderLiveMap().tv_trip_distance;
        }
        if (tv_trip_deduction == null) {
            tv_trip_deduction = newTripCardsAdapter.getViewHolderLiveMap().tv_trip_distance;
        }
    }

    public void resetMap() {
        if (newTripCardsAdapter != null && newTripCardsAdapter.getViewHolderLiveMap() != null) {
            mPolylineOptions = new PolylineOptions();
            mPolylineOptions.color(Color.BLUE).width(10);
            GoogleMap googleMap = newTripCardsAdapter.getViewHolderLiveMap().googleMap;
            if (googleMap != null) {
                googleMap.clear();
            }
        }
    }

    /**
     * Zooms the newTripCardsAdapter.viewHolderLiveMap.googleMap
     *
     * @param latLng new latitude and longitude from where to start the zoom
     * @see NewTripCardsAdapter
     * @see com.example.ruby.getgps.ui.adapters.ViewHolderLiveMap
     * @see GoogleMap#animateCamera(CameraUpdate)
     */
    public void setZoom(LatLng latLng) {
        setupTripCardView();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom
                .build();// Creates a CameraPosition from the builder

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Fill tv_trip_distance's text with current distance travelled by user
     *
     * @param tripDistance new distance to be displayed
     */
    public void setTripDistanceAndDeduction(String tripDistance, String tripDeduction) {
        setupTripCardView();
        tv_trip_distance.setText(tripDistance);
        tv_trip_deduction.setText(tripDeduction);
    }

    /**
     * Fill tv_live_card_date's text with current date the trip started
     *
     * @param date date when trip started
     */
    public void setTripDate(String date) {
        newTripCardsAdapter.getViewHolderLiveMap().tv_live_card_date.setText(date);
    }

    /**
     * Scroll to the first item in the list (the map);
     */
    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    /**
     * @return CardView's list adapter
     */
    public NewTripCardsAdapter getNewTripCardsAdapter() {
        return newTripCardsAdapter;
    }

    public void clearNewTripCardsAdapter() {
        newTripCardsAdapter = null;
    }

    public ArrayList<Trip> getNewTripsArrayList() {
        return newTripsArrayList;
    }

    @OnClick(R.id.bt_new_trips)
    public void filterNewTrips() {
        if (newTripsArrayList != null) {
            setUpRecyclerView(true);
            setupTripGroupButtons(true);
        }
    }

    @OnClick(R.id.bt_all_trips)
    public void filterAllTrips() {
        if (allTripsArrayList != null) {
            setUpRecyclerView(false);
            setupTripGroupButtons(false);
        }
    }

    public void selectNewTripsTab() {
        setUpRecyclerView(true);
        setupTripGroupButtons(true);
    }

    public void setupTripGroupButtons(boolean isNewTrips){
        bt_all_trips.setEnabled(isNewTrips);
        bt_new_trips.setEnabled(!isNewTrips);
    }

    public void setProgressBarVisibility(boolean visible) {
        if (progress_bar != null) {
            progress_bar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void setTv_trip_distance(TextView tv_trip_distance) {
        this.tv_trip_distance = tv_trip_distance;
    }

    public void setTv_trip_deduction(TextView tv_trip_deduction) {
        this.tv_trip_deduction = tv_trip_deduction;
    }

    public AllTripCardsAdapter getAllTripCardsAdapter() {
        return allTripCardsAdapter;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
