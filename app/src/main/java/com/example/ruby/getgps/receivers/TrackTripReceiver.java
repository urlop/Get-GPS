package com.example.ruby.getgps.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ruby.getgps.trip_mode.on_trip.TripTrackingService;
import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.TripHelper;
import com.google.android.gms.maps.model.LatLng;

/**
 * BroadcastReceiver for TripTrackingService.
 * Will do any visual change in the TripTabFragment of the MainActivity while the user location changes.
 *
 * @see com.example.ruby.getgps.trip_mode.on_trip.TripTrackingService
 * @see com.example.ruby.getgps.ui.fragments.TripTabFragment
 */
public class TrackTripReceiver extends BroadcastReceiver {

    private final TripTabFragment tripTabFragment;
    private final MainActivity mainActivity;

    /**
     * @param tripTabFragment fragment to be changed
     * @param mainActivity    activity of the fragment
     */
    public TrackTripReceiver(TripTabFragment tripTabFragment, MainActivity mainActivity) {  //TODO: check uses of MainActivity here
        this.tripTabFragment = tripTabFragment;
        this.mainActivity = mainActivity;
    }

    /**
     * Method executed when an intent is received by the broadcast.
     * Draws the new route in the map of the TripFragment, and changes "the distance traveled" showed in the TripFragment.
     *
     * @param context required param. It is the context of whoever called the receiver
     * @param intent  contains information such as in which circumstances the receiver was called
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TripTrackingService.ACTION)) {
            Double lat = intent.getDoubleExtra(Constants.LATITUDE_EXTRA, 0);
            Double lon = intent.getDoubleExtra(Constants.LONGITUDE_EXTRA, 0);
            float distance = intent.getFloatExtra(Constants.DISTANCE_EXTRA, 0f);
            LatLng latLng = new LatLng(lat, lon);
            if (TripTabFragment.getInstance().getNewTripCardsAdapter() != null && TripTabFragment.getInstance().getNewTripCardsAdapter().getViewHolderLiveMap() != null && TripTabFragment.getInstance().getNewTripCardsAdapter().getViewHolderLiveMap().googleMap != null) {
                tripTabFragment.drawPolyLine(latLng);
                tripTabFragment.setZoom(latLng);
                float distanceMiles = TripHelper.convertMetersToMiles(distance);
                if (mainActivity.getUser() != null && mainActivity.getUser().getCustomMileageRate() != null) {
                    tripTabFragment.setTripDistanceAndDeduction(distanceMiles + " mi", TripHelper.convertToTwoDecimals(distanceMiles * mainActivity.getUser().getCustomMileageRate()));
                }
            }
        } else if (intent.getAction().equals(TripTrackingService.ACTION_STOPPED)) {
            TripTabFragment.getInstance().resetMap();
            TripHelper.removeLiveCard();
        }
    }
}