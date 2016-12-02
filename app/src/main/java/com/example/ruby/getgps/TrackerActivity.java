package com.example.ruby.getgps;

import android.*;
import android.Manifest;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ruby.getgps.trip_mode.start_trip.StartTripSettings;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.permissions.PermissionGranted;
import com.example.ruby.getgps.utils.permissions.TektonLabs;

public class TrackerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        setupChecking();
    }

    private void setupChecking(){
        //java.lang.SecurityException: Activity detection usage requires the com.google.android.gms.permission.ACTIVITY_RECOGNITION permission
        //java.lang.SecurityException: Permission Denial: reading com.android.providers.media.MediaProvider uri content://media/external/fs_id from pid=5887, uid=10138 requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()
        TektonLabs.initialize(this);
        TektonLabs.checkPermissions("Please, grant access to needed permissions.", true, new PermissionGranted() {
                    @Override
                    public void alreadyGranted() {
                        ServiceHelper.startStartTripService(TrackerActivity.this, null);

                    }
                }, Manifest.permission.ACCESS_FINE_LOCATION);

    }
}
