package com.example.ruby.getgps;

import android.app.Application;

import com.example.ruby.getgps.utils.ConfigurationConstants;
import com.orm.SugarContext;

import timber.log.Timber;

public class MyApplication extends Application {

    private static MyApplication sInstance;

    public static MyApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*Crashlytics crashlyticsKit = new Crashlytics.Builder().core(
                new CrashlyticsCore.Builder().disabled(Constants.CRASHLYTICS_DISABLED).build()
        ).build();
        Fabric.with(this, crashlyticsKit);*/

        initializeInstance();

        Timber.d("getPackageName='%s'", getPackageName());
        Timber.d("GEOFENCE_RADIUS='%s'", ConfigurationConstants.GEOFENCE_RADIUS);
        Timber.d("ACTIVITY_CONFIDENCE='%s'", ConfigurationConstants.ACTIVITY_CONFIDENCE);
        Timber.d("DRIVE_STOP_WALKING_CONFIDENCE='%s'", ConfigurationConstants.DRIVE_STOP_WALKING_CONFIDENCE);
        Timber.d("DETECTION_INTERVAL_ACTIVITY='%s'", ConfigurationConstants.DETECTION_INTERVAL_ACTIVITY);
        Timber.d("DETECTION_INTERVAL_ACTIVITY_STOPPING='%s'", ConfigurationConstants.DETECTION_INTERVAL_ACTIVITY_STOPPING);
        Timber.d("LAST_N_LOCATIONS='%s'", ConfigurationConstants.LAST_N_LOCATIONS);
        Timber.d("LIMIT_DISTANCE_BETWEEN_N_LOCATIONS='%s'", ConfigurationConstants.LIMIT_DISTANCE_BETWEEN_N_LOCATIONS);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    private void initializeInstance() {
        sInstance = this;

        /*try {
            leTree = new LogentriesTree(getApplicationContext());
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, "EverlanceApplication", "Cannot instantiate logentries tree.");
            Crashlytics.logException(e);
        }*/

        //if (Constants.DEBUG_LOG) {
        Timber.plant(new Timber.DebugTree());
        /*} else {
            Timber.plant(new CrashlyticsLogTree());
            Timber.plant(leTree);
        }*/

        SugarContext.init(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Timber.w("Device memory is low");
    }
}
