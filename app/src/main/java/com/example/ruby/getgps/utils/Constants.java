package com.example.ruby.getgps.utils;

import android.Manifest;

import com.example.ruby.getgps.BuildConfig;
import com.google.android.gms.location.DetectedActivity;

/**
 * Where are constants are stored for easier access
 */
public final class Constants {

    // Build configs
    public static final String BASE_API_URL = BuildConfig.BASE_API_URL;
    public static final boolean CRASHLYTICS_DISABLED = BuildConfig.CRASHLYTICS_DISABLED;
    public static final boolean DEBUG_LOG = BuildConfig.DEBUG;
    public static final boolean MUST_GET_MOTION_TO_STOP = BuildConfig.MUST_GET_MOTION_TO_STOP;

    //Intent Constant for activityInformationToTripTrackingService
    public static final String ACTIVITY_TYPE_EXTRA = "ActivityType";
    public static final String ACTIVITY_CONFIDENCE_EXTRA = "ActivityConfidence";

    //Intent Constant for broadcasting TripTrackingService
    public static final String LATITUDE_EXTRA = "latitude";
    public static final String LONGITUDE_EXTRA = "longitude";
    public static final String DISTANCE_EXTRA = "distance";

    //Intent Constant for EditProfile
    public static final int EDIT_PROFILE = 2;
    public static final String EDIT_PROFILE_EXTRA = "EditProfile";
    public static final String EDIT_PROFILE_USER_EXTRA = "User";

    //Intent Constant for Switch Work
    public static final int TRIP_WORK_SOURCE = 3;
    public static final String TRIP_WORK_SOURCE_USER_EXTRA = "User";
    public static final String TRIP_WORK_SOURCE_TRIP_EXTRA = "Trip";
    public static final String TRIP_WORK_SOURCE_STRING_EXTRA = "WorkSourceString";
    public static final String TRIP_WORK_SOURCE_PREVIOUS_TRIP_PURPOSE_EXTRA = "TripPurpose";
    public static final String TRIP_WORK_SOURCE_ORIGIN_EXTRA = "Origin";

    public static final int ORIGIN_ADD_TRIP_ACTIVITY = 10;

    //Intent Constant for StartTripReceiver
    public static final String DATE_FROM_TRIP_EXTRA = "dateTrip";

    //Intent Constant for UploadService
    public static final String TRIP_ID_EXTRA = "savedLocations";

    //Permissions
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 20;
    public static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //Constant for Mapbox Static Map
    public static final String MAPBOX_API_KEY = "pk.eyJ1Ijoid2lsbHlyaDQ5NSIsImEiOiJjaWpwd2tka3MwMWYzdDlrb2w5bDcycjZ0In0.eOcaLDji1e5DP2b5NBFRjw";

    //MainActivity Constant for requesting fine location
    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    //Constant for StartTripService
    public static final String START_TRIP_DATE_EXTRA = "startTripDate";

    //Constant for NewTripCardsAdapter
    public static final int TRIP_CARD_VIEW_TYPE_LIVE = 0;
    public static final int TRIP_CARD_VIEW_TYPE_STATIC = 1;

    //Constant for AllTripCardsAdapter
    public static final String VIEW_HOLDER_POSITION = "ViewHolderPosition";

    //Intent Constant for UploadService
    public static final String TRIP_OBJECT_EXTRA = "tripObjectExtra";

    //DetectedActivity
    public static final int STOPPING_DETECTED_CONDITION = DetectedActivity.ON_FOOT;
    public static final int STARTING_DETECTED_CONDITION = DetectedActivity.IN_VEHICLE;

    public static final String IMAGE_FILE = "image/*";
    //Start Trip Methods
    public static final String EXIT_GEOFENCE = "ExitGeofence";
    public static final String START_BUTTON = "StartButton";
    public static final String DRIVING_MOTION = "AutomotiveMotion";
    //Stop Trip Methods
    public static final String STOP_BUTTON = "StopButton";
    public static final String WALKING_MOTION = "WalkingMotion";
    public final static String TIMER = "Timer";
    public static final String APP_WILL_TERMINATE = "AppWillTerminate";

    //ANIMATION SWIPE
    public static final int ANIMATION_DURATION = 500;
    public static final int ANIMATION_DELAY = 100;

}
