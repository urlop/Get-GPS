package com.example.ruby.getgps.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.TripHelper;

/**
 * BroadcastReceiver for StartTripService.
 * Will do any visual change in the MainActivity when it is detected that a trip is starting.
 *
 * @see com.example.ruby.getgps.trip_mode.start_trip.StartTripService
 * @see com.example.ruby.getgps.ui.activities.MainActivity
 */
public class StartTripReceiver extends BroadcastReceiver {

    private final TripTabFragment tripTabFragment;

    public StartTripReceiver(TripTabFragment tripTabFragment) {
        this.tripTabFragment = tripTabFragment;
    }

    /**
     * Method executed when an intent is received by the broadcast.
     * Gives the date the trip started to the view.
     *
     * @param context required param. It is the context of whoever called the receiver
     * @param intent  contains information such as in which circumstances the receiver was called
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            //TODO: check its uses
            String date = intent.getStringExtra(Constants.START_TRIP_DATE_EXTRA);
            if (TripTabFragment.getInstance().getNewTripCardsAdapter() != null) {
                TripHelper.createLiveCard();
            }
        }
    }
}
