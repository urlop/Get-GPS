package com.example.ruby.getgps.trip_mode.start_trip;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.trip_mode.on_trip.TripTrackingService;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.LoggingHelper;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Service to detect if user is starting a trip. Uses GeofencingEvent and ActivityRecognition for this purpose.
 *
 * @see GeofencingEvent
 * @see com.google.android.gms.location.ActivityRecognitionResult
 */
public class StartTripService extends IntentService {

    private final int GEOFENCE_NOTIFICATION_ID = 101;
    private final int MOTION_TRACKING_NOTIFICATION_ID = 102;

    public static final String ACTION = "com.example.ruby.getgps.StartTripService.TripStarted";

    public StartTripService() {
        super(StartTripService.class.getName());
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Checks if user exits the geofence or is in a vehicle.
     * If she/she does, notifies the user, starts TripTrackingService, and calls the broadcast
     * @param intent    value passed to startService(Intent).
     *
     * @see TripTrackingService
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("method=StartTripService.onHandleIntent intent='%s'", intent.toUri(0));

        if (intent != null) {
            //TODO: put each part into a separate method
            //GEOFENCE
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (!geofencingEvent.hasError()) {
                int transition = geofencingEvent.getGeofenceTransition();
                Timber.d("method=StartTripService.onHandleIntent transition='%s'", transition);
                if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) { //int GEOFENCE_TRANSITION_ENTER = 1; int GEOFENCE_TRANSITION_EXIT = 2; int GEOFENCE_TRANSITION_DWELL = 4;
                    if (!isTripTrackingServiceRunning(TripTrackingService.class)) {
                        Timber.d("method=StartTripService.onHandleIntent action='Starting trip tracking service' startMethod='%s'", Constants.EXIT_GEOFENCE);
                        sendNotification(this, GEOFENCE_NOTIFICATION_ID, getApplicationContext().getString(R.string.notification_geofence_exited), getApplicationContext().getString(R.string.notification_geofence_exited)); //TODO: strings just for testing

                        ServiceHelper.stopStartTripService();
                        ServiceHelper.startTripTrackingService(getApplicationContext(), Constants.EXIT_GEOFENCE); //startMethod: "ExitGeofence"
                        broadcastStartTripIntent();
                    } else {
                        Timber.w("method=StartTripService.onHandleIntent action='Trip tracking service already running' startMethod='%s'", Constants.EXIT_GEOFENCE);
                    }
                }

            } else if (ActivityRecognitionResult.hasResult(intent)) {  //MOTION TRACKING
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                DetectedActivity detectedActivity = result.getMostProbableActivity();

                Timber.d("method=StartTripService.onHandleIntent activity.name='%s' activity.confidence=%s", LoggingHelper.getActivityString(getApplicationContext(), detectedActivity.getType()), detectedActivity.getConfidence());

                if (TripHelper.isUserDriving(detectedActivity)) {
                    if (!isTripTrackingServiceRunning(TripTrackingService.class)) {
                        Timber.d("action='Starting trip tracking service' startMethod='%s'", Constants.DRIVING_MOTION);
                        sendNotification(this, MOTION_TRACKING_NOTIFICATION_ID, getApplicationContext().getString(R.string.notification_activity_driving), getApplicationContext().getString(R.string.notification_activity_driving)); //TODO: strings just for testing

                        ServiceHelper.stopStartTripService();
                        ServiceHelper.startTripTrackingService(getApplicationContext(), Constants.DRIVING_MOTION); //startMethod: "AutomotiveMotion"
                        broadcastStartTripIntent();
                    } else {
                        Timber.d("method=StartTripService.onHandleIntent action='Trip tracking service already running' startMethod='%s'", Constants.DRIVING_MOTION);
                    }
                }
            } else {
                Timber.w("method=StartTripService.onHandleIntent Intent had no data returned");
            }
        }

    }

    /**
     * Send notification to user informing that he/she exited the geofence or is in a vehicle
     * @param context           this context
     * @param id                id of the notification
     * @param notificationText  message displayed
     * @param notificationTitle title of the notification
     */
    //TODO: TripTrackingService has the same method. Put it in just one place
    //TODO: it is not necessary to pass context, use this
    private void sendNotification(Context context, int id, String notificationText,
                                  String notificationTitle) {

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(false);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificationBuilder.build());

        wakeLock.release();
    }

    /**
     * Tells the broadcast to change the date in the view
     */
    private void broadcastStartTripIntent() {
        Intent in = new Intent(ACTION);
        //TODO send current CORRECT date format
        String date = (new SimpleDateFormat("MMM", Locale.US)).format(Calendar.getInstance().getTime()) + " " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ", " + Calendar.getInstance().get(Calendar.YEAR);
        Timber.d("method=StartTripService.broadcastStartTripIntent startDate='%s'", date);
        in.putExtra(Constants.START_TRIP_DATE_EXTRA, date);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
    }

    /**
     * Tells if TripTrackingService is already running. To send it the ActivityDetected data
     * @param serviceClass  Class to be detected is is started
     * @return              yes is service is running
     */
    //TODO: use "TripTrackingService.class" instead of serviceClass
    private boolean isTripTrackingServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
