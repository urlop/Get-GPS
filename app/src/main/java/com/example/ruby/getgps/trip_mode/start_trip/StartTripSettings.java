package com.example.ruby.getgps.trip_mode.start_trip;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.utils.PermissionUtil;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import timber.log.Timber;

/**
 * Contains all listeners and callbacks to be implemented in StartTripService
 *
 * @see StartTripService
 */
public class StartTripSettings implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private final MainActivity mainActivity;

    public StartTripSettings(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Connects to GoogleApiClient
     */
    public synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mainActivity.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Creates mLocationRequest to check user's locations
     *
     * @see LocationRequest
     */
    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates mLocationSettingsRequest to check if GPS is on
     *
     * @see LocationSettingsRequest
     */
    public void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Setups to receive callbacks when location changes
     */
    public void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(mainActivity);
    }

    /**
     * Starts requesting for LocationUpdates
     */
    private void startLocationUpdate() {
        if (PermissionUtil.checkFineLocationPermission(mainActivity)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this).setResultCallback(this);
        }
    }

    /**
     * When location changes, starts StartTripService
     * @param location  current location detected
     *
     * @see MainActivity#startStartTripService(Location)
     */
    @Override
    public void onLocationChanged(Location location) {
        Timber.d("method=onLocationChanged action=StartTripService");
        ServiceHelper.startStartTripService(mainActivity.getApplicationContext(), location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * When connected to GoogleApiClient, requests for location updates
     * @param bundle    Bundle of data provided to clients by Google Play services. May be null if no content is provided by the service.
     *
     * @see #startLocationUpdate()
     */
    @Override
    public void onConnected(Bundle bundle) {
        Timber.d("onConnected");
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }


    @Override
    public void onResult(@NonNull Status status) {

    }
}
