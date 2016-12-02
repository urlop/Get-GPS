package com.example.ruby.getgps.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.receivers.StartTripReceiver;
import com.example.ruby.getgps.receivers.StopTripReceiver;
import com.example.ruby.getgps.receivers.TrackTripReceiver;
import com.example.ruby.getgps.trip_mode.on_trip.TripTrackingService;
import com.example.ruby.getgps.trip_mode.start_trip.StartTripBuilder;
import com.example.ruby.getgps.trip_mode.start_trip.StartTripService;
import com.example.ruby.getgps.trip_mode.start_trip.StartTripSettings;
import com.example.ruby.getgps.trip_mode.stop_trip.UploadService;
import com.example.ruby.getgps.ui.adapters.ViewPagerAdapter;
import com.example.ruby.getgps.ui.fragments.HomeTabFragment;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.CustomViewPager;
import com.example.ruby.getgps.utils.PermissionUtil;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Contains the main views such us home and trips
 */
public class MainActivity extends AppCompatActivity implements
        ResultCallback<LocationSettingsResult> {

    private CustomViewPager viewPager;
    private StartTripSettings startTripSettings;
    private AppBarLayout ll_container;
    private boolean manualStartTrip;
    private MainActivity mainActivity;
    private TabLayout tabsStrip;
    public User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mainActivity = this;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }


        if (PreferencesManager.getInstance(this).getAutomatictTrackingSwitchState()) {
            askPermissionsToStartTripService();
        }
        retrieveUserInfo();
        setupView();
        registerBroadcastforServices();
    }


    private void setupView() {
        ll_container = (AppBarLayout) findViewById(R.id.main_appbar);

        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(),
                getResources().getString(R.string.tab_home), getResources().getString(R.string.tab_trips), mainActivity));
        viewPager.setPagingDisabled();
        setUpFloatingActionButton();
        tabsStrip = (TabLayout) findViewById(R.id.tabs);
        tabsStrip.setupWithViewPager(viewPager);
        setUpTabsIcons();
        setUpViewPagerListener();

    }

    private void setUpTabsIcons() {
        TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText(getApplicationContext().getString(R.string.tab_home));
        tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_home_white_selected, 0, 0);
        tabsStrip.getTabAt(0).setCustomView(tabOne);

        TextView tabTwo = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText(getApplicationContext().getString(R.string.tab_trips));
        tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_trips_white_unselected, 0, 0);
        tabTwo.setTextColor(ContextCompat.getColor(mainActivity, R.color.white_unselected));
        tabsStrip.getTabAt(1).setCustomView(tabTwo);
    }

    private void setUpFloatingActionButton() {
        final FloatingActionButton fab_start_tracker = (FloatingActionButton) findViewById(R.id.fab_start_tracker);
        final FloatingActionButton fab_manual_trip = (FloatingActionButton) findViewById(R.id.fab_manual_trip);
        final FloatingActionsMenu floating_action_menu = (FloatingActionsMenu) findViewById(R.id.floating_action_menu);
        floating_action_menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                findViewById(R.id.main_background).setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.background_black));
                findViewById(R.id.main_background).setClickable(true);
            }

            @Override
            public void onMenuCollapsed() {
                findViewById(R.id.main_background).setBackgroundResource(android.R.color.transparent);
                findViewById(R.id.main_background).setClickable(false);
            }
        });
        fab_start_tracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TripTabFragment.getInstance().getNewTripCardsAdapter() != null) {
                    tabsStrip.getTabAt(1).select();
                    TripTabFragment.getInstance().selectNewTripsTab();
                    TripTabFragment.getInstance().selectNewTripsTab();
                    TripTabFragment.getInstance().getNewTripCardsAdapter().addLiveCardToArray();
                    TripTabFragment.getInstance().getNewTripCardsAdapter().announceDataHasChanged();
                    floating_action_menu.collapse();
                }
            }
        });
        fab_manual_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_action_menu.collapse();
                Intent intent = new Intent(MainActivity.this, AddTripActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }

    /**
     * Registers all broadcastReceivers so they can change later any view element in this activity or its fragments.
     */
    private void registerBroadcastforServices() {
        IntentFilter filter = new IntentFilter(StartTripService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new StartTripReceiver(TripTabFragment.getInstance()), filter);

        filter = new IntentFilter(TripTrackingService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new TrackTripReceiver(TripTabFragment.getInstance(), this), filter);

        filter = new IntentFilter(TripTrackingService.ACTION_STOPPED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new TrackTripReceiver(TripTabFragment.getInstance(), this), filter);

        filter = new IntentFilter(UploadService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new StopTripReceiver(), filter);

    }

    /**
     * If there is no trip currently started, starts the StartTripService using its StartTripBuilder.
     * StartTripService will start tracking if a trip should be started.
     *
     * @param mLastKnownLocation the approximate location where the user is
     * @see StartTripBuilder
     * @see StartTripService
     */
    private void startStartTripService(Location mLastKnownLocation) {
        //Builder For Start trip service
        Timber.d("method=startStartTripService");

        if (TripHelper.tripOngoing() == null) {
            Timber.d("method=startStartTripService action='tripOngoing() == null'");
            ServiceHelper.startStartTripService(this, mLastKnownLocation);
        }
    }

    /**
     * Shows permission request if necessary;
     * to later start the methods related to the gps if the permissions were granted
     */
    public void askPermissionsToStartTripService() {
        Timber.d("method=askPermissionsToStartTripService");
        if (!PermissionUtil.isAskingForPermissionNecessary(this, ll_container, Constants.PERMISSIONS_LOCATION[0])) {
            Timber.d("method=askPermissionsToStartTripService action='action=!isAskingForPermissionNecessary'");
            startTripSettings = new StartTripSettings(this);
            startTripSettings.buildGoogleApiClient();
            startTripSettings.createLocationRequest();
            startTripSettings.buildLocationSettingsRequest();
            startTripSettings.checkLocationSettings();
        }
    }

    /*
    * Retrieves Information User's informations from the Webservice
    */
    public void retrieveUserInfo() {
        Call<User> call = RequestManager.getDefault(this).retrieveCurrentUser();
        call.enqueue(new CustomCallback<User>(this, call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    user = response.body();

                    populateWithInitialInformation();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                super.onFailure(call, t);
                if (TripTabFragment.getInstance() != null  && TripTabFragment.getInstance().isAdded()) {
                    TripTabFragment.getInstance().setUpRecyclerView(true);
                }
            }
        });
    }

    private void populateWithInitialInformation() {
        Timber.d("populateWithInitialInformation");
        if (TripTabFragment.getInstance() != null && TripTabFragment.getInstance().isAdded() && TripTabFragment.getInstance().progress_bar != null &&
                TripTabFragment.getInstance().allTripsArrayList != null &&
                TripTabFragment.getInstance().newTripsArrayList != null) {
            TripTabFragment.getInstance().progress_bar.setVisibility(View.GONE);
            TripTabFragment.getInstance().setUpRecyclerView(true);
            TripTabFragment.getInstance().getAllTripCardsAdapter().addAll(user.getTrips());
            TripTabFragment.getInstance().getNewTripCardsAdapter().addAll(new ArrayList<>(user.getUncategorizedTrips()), false);
            PreferencesManager.getInstance(MainActivity.this).saveAutomaticTrackingSwitchState(user.getAutoDetectEnabled());
            PreferencesManager.getInstance(MainActivity.this).saveAutoClassifyState(user.getAutoClassify());
            HomeTabFragment.getInstance().refreshView(user.getAutoClassify(), user.getAutoDetectEnabled());
            TripTabFragment.getInstance().setupTripGroupButtons(true);
            uploadMissingTrips();
        }
    }

    /**
     * Calls UploadService to check if there are trip not sent, if there are, send them.
     */
    private void uploadMissingTrips() {
        ServiceHelper.uploadService(this);
    }

    /**
     * Marshmallow's permissions onResult method.
     * After user accepts or reject permission request,
     * this will check if all the respective permissions were granted.
     * If they were, this will start the corresponding methods related with the feature permitted.
     *
     * @param requestCode  code of the set of permission which were asked (accepted or rejected) to the user
     * @param permissions  permissions asked to the user
     * @param grantResults contains a code for each permission. Explaying it each one was accepted or rejected
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.d("method=onRequestPermissionsResult");
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    // permission was granted, yay! Do the trips-related task you need to do.
                    startTripSettings = new StartTripSettings(this);
                    startTripSettings.buildGoogleApiClient();
                    startTripSettings.createLocationRequest();
                    startTripSettings.buildLocationSettingsRequest();
                    startTripSettings.checkLocationSettings();
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * onResult method for when user accepts(or not) requests such us "Turn on your GPS"
     *
     * @param requestCode code of the request asked to the user
     * @param resultCode  indicates if user accepts or rejects the request
     * @param data        intent containing extra data if necessary
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (!startTripSettings.getmGoogleApiClient().isConnected()) {
                            startTripSettings.buildGoogleApiClient();
                            startTripSettings.createLocationRequest();
                            startTripSettings.buildLocationSettingsRequest();
                            startTripSettings.checkLocationSettings();
                            broadcastStartTripIntent();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
            case Constants.EDIT_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mainActivity.retrieveUserInfo();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
            case Constants.TRIP_WORK_SOURCE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        this.user = (User) data.getSerializableExtra(Constants.TRIP_WORK_SOURCE_USER_EXTRA);
                        Trip currentTrip = (Trip) data.getSerializableExtra(Constants.TRIP_WORK_SOURCE_TRIP_EXTRA);
                        String previousPurpose = data.getStringExtra(Constants.TRIP_WORK_SOURCE_PREVIOUS_TRIP_PURPOSE_EXTRA);

                        if (TripTabFragment.getInstance().getAllTripCardsAdapter() != null) {
                            TripTabFragment.getInstance().getAllTripCardsAdapter().announceTripWorkSourceHasBeenUpdated(currentTrip);
                        }
                        Timber.d("previoustrip.purpose=%s", previousPurpose);
                        TripHelper.updateTrip(this, currentTrip, previousPurpose);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    /**
     * Checks if GPS is on or off.
     * If off, asks the user to turn it on with a popup dialog;
     * If on, continues with the methods which need the GPS to be on
     *
     * @param locationSettingsResult users' decision. if he accepted the location settings change
     */
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();

        Timber.d("method=onResult action=LocationSettingsResult");
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startStartTripService(null);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MainActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Timber.e(e, "method=onResult error=SendIntentException");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }

    /**
     * Changes TripTabFragments date using broadcast
     */
    private void broadcastStartTripIntent() {
        //TODO broadcast date to view
        Intent in = new Intent(StartTripService.ACTION);
        in.putExtra(Constants.DATE_FROM_TRIP_EXTRA, "");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
    }

    /**
     * Restarts trip tracker after trip ends
     */
    public void startTripFromLastPoint() {
        if (PreferencesManager.getInstance(this).getAutomatictTrackingSwitchState()) {
            startStartTripService(null);
        }
    }

    /**
     * Tells if automatic tracking is off
     *
     * @return yes is automatic tracking if off
     */
    public boolean isManualStartTrip() {
        return manualStartTrip;
    }

    /**
     * Changes value of manualStartTrip (opposite of automatic tracking)
     *
     * @param manualStartTrip
     */
    public void setManualStartTrip(boolean manualStartTrip) {
        this.manualStartTrip = manualStartTrip;
    }

    public User getUser() {
        return user;
    }

    public void setUserIncomeSources(List<String> incomeSources) {
        user.setIncomeSources(incomeSources);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setUpViewPagerListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((TextView) mainActivity.tabsStrip.getTabAt(0).getCustomView()).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_home_white_selected, 0, 0);
                    ((TextView) mainActivity.tabsStrip.getTabAt(1).getCustomView()).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_trips_white_unselected, 0, 0);
                    ((TextView) mainActivity.tabsStrip.getTabAt(1).getCustomView()).setTextColor(ContextCompat.getColor(mainActivity, R.color.white_unselected));
                    ((TextView) mainActivity.tabsStrip.getTabAt(0).getCustomView()).setTextColor(ContextCompat.getColor(mainActivity, android.R.color.white));

                } else {
                    ((TextView) mainActivity.tabsStrip.getTabAt(1).getCustomView()).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_trips_white_selected, 0, 0);
                    ((TextView) mainActivity.tabsStrip.getTabAt(0).getCustomView()).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_home_white_unselected, 0, 0);
                    ((TextView) mainActivity.tabsStrip.getTabAt(0).getCustomView()).setTextColor(ContextCompat.getColor(mainActivity, R.color.white_unselected));
                    ((TextView) mainActivity.tabsStrip.getTabAt(1).getCustomView()).setTextColor(ContextCompat.getColor(mainActivity, android.R.color.white));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
