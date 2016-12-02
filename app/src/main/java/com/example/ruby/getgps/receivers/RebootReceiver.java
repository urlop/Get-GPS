package com.example.ruby.getgps.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.ServiceHelper;

import timber.log.Timber;

public class RebootReceiver extends BroadcastReceiver {
    public RebootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Timber.d("method=onReceive action='Trying to restart services on reboot'");
        if (PreferencesManager.getInstance(context).getAutomatictTrackingSwitchState()) {
            ServiceHelper.startStartTripService(context, null);
            ServiceHelper.uploadService(context);
        }
    }
}