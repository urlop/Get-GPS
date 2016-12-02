package com.example.ruby.getgps.trip_mode.start_trip;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.ruby.getgps.ui.fragments.HomeTabFragment;
import com.example.ruby.getgps.utils.ConfigurationConstants;
import com.example.ruby.getgps.utils.PermissionUtil;
import com.example.ruby.getgps.utils.TripHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import timber.log.Timber;

/**
 * StartTripService's builder which encloses all connection with GoogleApiClient
 *
 * @see StartTripService
 * @see GoogleApiClient
 */
public class StartTripBuilder implements ConnectionCallbacks,
        OnConnectionFailedListener, ResultCallback<Status> {

    private final Context mContext;
    private final GoogleApiClient mGoogleApiClient;
    private PendingIntent mPendingIntent;
    private Location mLastKnowLocation;
    private PendingResult<Status> pendingResult;

    private final GeofencingApi geofencingApi;
    private final ActivityRecognitionApi activityRecognitionApi;

    private static StartTripBuilder instance;

    /**
     * Returns the current StartTripBuilder instance in the app
     *
     * @return current StartTripBuilder
     */
    public static StartTripBuilder getInstance() {
        Timber.d("method=StartTripBuilder instance='%s'", instance != null ? instance.toString() : "NO");
        return instance;
    }

    /**
     * Adds connection with GoogleApiClient
     *
     * @param context           context of whoever called the receiver
     * @param mLastKnowLocation last location registered from the user
     * @see com.google.android.gms.common.api.GoogleApiClient.Builder#addApi(Api)
     */
    public StartTripBuilder(Context context, Location mLastKnowLocation) {
        instance = this;
        mContext = context;
        mPendingIntent = null;
        this.mLastKnowLocation = mLastKnowLocation;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).addApi(ActivityRecognition.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        geofencingApi = LocationServices.GeofencingApi;
        activityRecognitionApi = ActivityRecognition.ActivityRecognitionApi;
    }

    /**
     * When connected to the GoogleApiClient,
     * requests for geofencing and activityRecognition changes
     *
     * @param bundle Bundle of data provided to clients by Google Play services. May be null if no content is provided by the service.
     * @see GeofencingApi
     * @see ActivityRecognitionApi
     */
    @Override
    public void onConnected(Bundle bundle) {
        mPendingIntent = createRequestPendingIntent();

        //getting LastKnowLocation
        if (PermissionUtil.checkLocationPermission(mContext)) {
            if (mLastKnowLocation == null) {
                mLastKnowLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            setupGeofencing();
        }
        setupMotionTRacking();
    }

    private void setupGeofencing() {
        if (mLastKnowLocation != null) {
            GeofencingRequest mGeofencingRequest = new GeofencingRequest.Builder().addGeofence(
                    TripHelper.createGeofence(mLastKnowLocation)).build();
            if (PermissionUtil.checkLocationPermission(mContext)) {
                pendingResult = geofencingApi
                        .addGeofences(mGoogleApiClient, mGeofencingRequest,
                                mPendingIntent);
                pendingResult.setResultCallback(this);
            }
        } else {
            HomeTabFragment.getInstance().askPermissionsToStartTripService();
        }
    }

    private void setupMotionTRacking() {
        pendingResult = activityRecognitionApi.requestActivityUpdates(mGoogleApiClient, ConfigurationConstants.DETECTION_INTERVAL_ACTIVITY, mPendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("method=onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("method=onConnectionFailed");
    }

    @Override
    public void onResult(@NonNull Status status) {
        Timber.d("method=onResult status.message='%s'", status.getStatusMessage());
    }

    /**
     * @return intent to call StartTripService
     */
    private PendingIntent createRequestPendingIntent() {
        if (mPendingIntent == null) {
            Intent intent = new Intent(mContext, StartTripService.class);
            mPendingIntent = PendingIntent.getService(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mPendingIntent;
    }

    /**
     * Stops connection with GoogleApiClient so StartTripService won't be called again
     */
    public void stopService() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            geofencingApi.removeGeofences(mGoogleApiClient, mPendingIntent);
            activityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mPendingIntent);
            mGoogleApiClient.disconnect();
        }
    }
}
