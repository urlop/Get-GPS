package com.example.ruby.getgps.ui.adapters;


import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.LocationSave;
import com.example.ruby.getgps.models.TripSave;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.PermissionUtil;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Custom ViewHolder for special mapView CardView
 *
 * @see android.support.v7.widget.RecyclerView.ViewHolder
 */
public class ViewHolderLiveMap extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    public GoogleMap googleMap;
    private final TripTabFragment tripTabFragment;
    public
    @Bind(R.id.tv_trip_distance)
    TextView tv_trip_distance;
    public
    @Bind(R.id.tv_trip_deduction)
    TextView tv_trip_deduction;
    public
    @Bind(R.id.tv_live_card_date)
    TextView tv_live_card_date;
    public
    @Bind(R.id.bt_start_trip)
    Button bt_start_trip;
    public
    @Bind(R.id.bt_stop_trip)
    Button bt_stop_trip;
    private SupportMapFragment supportMapFragment;
    private FragmentManager fragmentManager;

    /**
     * Constructor. Adds a map fragment to the view.
     *
     * @param itemView represents the basic building block for user interface components
     */
    public ViewHolderLiveMap(View itemView) {
        super(itemView);
        tripTabFragment = TripTabFragment.getInstance();
        ButterKnife.bind(this, itemView);
        setUpGoogleMapFragment();
        if (PreferencesManager.getInstance(tripTabFragment.getContext()).getTripStartedState()) {
            toggleButtons(true);
        } else {
            toggleButtons(false);
        }
        Timber.d("method=constructor preferecenceStart='%s'", PreferencesManager.getInstance(tripTabFragment.getContext()).getTripStartedState());
    }

    private void setUpGoogleMapFragment() {
        if (fragmentManager == null) {
            Timber.d("method=setUpGoogleMapFragment action='fragmentManager null'");
            fragmentManager = tripTabFragment.getChildFragmentManager();
        }else {
            Timber.d("method=setUpGoogleMapFragment action='fragmentManager not null'");
            supportMapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.location_map);
        }
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
        }
        supportMapFragment.getMapAsync(this);
        fragmentManager.beginTransaction().replace(R.id.location_map, supportMapFragment).commit();
    }

    /**
     * Executed when map was successfully loaded, so the corresponding methods can be executed.
     *
     * @param googleMap map loaded
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        TripTabFragment.getInstance().setGoogleMap(googleMap);
        TripTabFragment.getInstance().setTv_trip_distance(tv_trip_distance);
        TripTabFragment.getInstance().setTv_trip_deduction(tv_trip_deduction);
        getLocations();
        if (PermissionUtil.checkLocationPermission(tripTabFragment.getMainActivity())) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(false);

            //Set Zoom right when the map is showed
            try {
                LocationManager locationManager = (LocationManager) tripTabFragment.getContext().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                Location location = locationManager.getLastKnownLocation(locationManager
                        .getBestProvider(criteria, false));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(15)
                        .build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } catch (NullPointerException e) {
                e.printStackTrace();
                Timber.e(e, "method=onMapReady error=Error when setting map zoom");
            }
        }
    }

    /**
     * If a trip was already started, shows it on the loaded map.
     */
    private void getLocations() {
        TripSave tripSave = TripHelper.tripOngoing();
        if (tripSave != null) {
            for (LocationSave locationSave : tripSave.getLocations()) {
                if (locationSave != null) {
                    tripTabFragment.drawPolyLine(new LatLng(locationSave.getLatitude(), locationSave.getLongitude()));
                }
            }
        }
    }

    @OnClick(R.id.bt_start_trip)
    public void manualStartTrip() {
        toggleButtons(true);
        ServiceHelper.startTripTrackingService(tripTabFragment.getContext(), Constants.START_BUTTON);
         //startMethod: "StartButton"
    }

    @OnClick(R.id.bt_stop_trip)
    public void manualStopTrip() {
        ServiceHelper.stopTripTrackingService(tripTabFragment.getContext(), Constants.STOP_BUTTON); //stopMethod: "StopButton"
    }

    public void toggleButtons(boolean toggle) {
        bt_start_trip.setVisibility(toggle ? View.GONE : View.VISIBLE);
        bt_stop_trip.setVisibility(toggle ? View.VISIBLE : View.GONE);
    }
}
