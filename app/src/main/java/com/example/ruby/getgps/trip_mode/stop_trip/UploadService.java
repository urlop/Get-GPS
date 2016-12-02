package com.example.ruby.getgps.trip_mode.stop_trip;

import android.app.IntentService;
import android.content.Intent;

import com.example.ruby.getgps.models.LocationSave;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.models.TripSave;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.GeneralHelper;
import com.example.ruby.getgps.utils.MapboxHelper;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.TimeHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.example.ruby.getgps.utils.retrofit.RequestManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Service for uploading missing data to the web service.
 * Uploads all finished trips which could not be uploaded before.
 * If it fails the first time, tries again. (NOT ANYMORE)
 */
public class UploadService extends IntentService {
    public static final String ACTION = "com.example.ruby.getgps.UploadService.SendTrip";
    private TripSave currentTripSave;

    public UploadService() {
        super(UploadService.class.getName());
    }

    /**
     * Uploads trips not uploaded yet to the web service.
     * TRIP_ID_EXTRA is the id of the current finished trip if there is one.
     *
     * @param intent value passed to startService(Intent).
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(Constants.TRIP_ID_EXTRA)) {
                currentTripSave = getTripSaved(intent.getLongExtra(Constants.TRIP_ID_EXTRA, 0L));
                if (currentTripSave != null) {
                    generateMissingData(currentTripSave.getLocations());
                } else {
                    Timber.w("Trip not found in Local DB");
                }
            }
            uploadAllMissingTrips();
        }
    }

    /**
     * Gets recent trip saved
     *
     * @param id recent trip's identifier
     * @return TripSave class
     * @see TripSave
     */
    private TripSave getTripSaved(Long id) {
        return TripSave.findById(TripSave.class, id);
    }

    /**
     * If user has wifi connection, calls web service to send all trips not uploaded yet.
     *
     * @see #hasConnection()
     */
    private void uploadAllMissingTrips() {
        List<TripSave> trips = TripSave.getAllTripsToUpload();
        for (TripSave ts : trips) {
            callUploadTrip(ts);
        }
    }

    /**
     * Tells if user has connection
     *
     * @return true if a connection is detected
     */
    //TODO: Check if it is going to be used
    private boolean hasConnection() {
        return true;
    }

    /**
     * Uploads tripSave to web service.
     *
     * @param tripSave      to to be uploaded
     */
    private void callUploadTrip(final TripSave tripSave) {
        Timber.d("method=callUploadTrip");
        if (!tripSave.getLocations().isEmpty()) {
            Trip trip = new Trip((float) (tripSave.getMiles()), true,
                    tripSave.getMapboxUrl(), tripSave.getFromAddress(), tripSave.getToAddress(), TripHelper.locationsToString(tripSave.getLocations()),
                    tripSave.getStartMethod(), tripSave.getStopMethod(),
                    tripSave.getFromStreet(), tripSave.getFromSublocality(), tripSave.getFromCity(), tripSave.getFromSubstate(), tripSave.getFromState(), tripSave.getFromCountry(), tripSave.getFromPostalCode(),
                    tripSave.getToStreet(), tripSave.getToSublocality(), tripSave.getToCity(), tripSave.getToSubstate(), tripSave.getToState(), tripSave.getToCountry(), tripSave.getToPostalCode());

            if (!PreferencesManager.getInstance(getApplicationContext()).autoClassifyStateIsOff(getApplicationContext())) {
                trip.setPurpose(PreferencesManager.getInstance(getApplicationContext()).getAutoClassifyState());
            } else {
                trip.setPurpose(null);
            }
            trip.setStartedAt(TimeHelper.longToTimeFormat(tripSave.getLocations().get(0).getTime()));
            trip.setEndedAt(TimeHelper.longToTimeFormat(tripSave.getLocations().get(tripSave.getLocations().size() - 1).getTime()));
            GeneralHelper.setAndroidDataToTrip(this, trip);

            Call<Trip> call = RequestManager.getDefault(getApplicationContext()).uploadTrip(trip);
            call.enqueue(new Callback<Trip>() {
                @Override
                public void onResponse(Call<Trip> call, Response<Trip> response) {
                    if (response.isSuccessful()) {
                        Timber.d("method=uploadTrip.onResponse action='Trip saved in WS'");
                        tripSave.deleteLocations();
                        tripSave.delete();
                        Trip trip = response.body();
                        ServiceHelper.broadCastTripStopped(getApplicationContext(), trip);
                        TripTabFragment.getInstance().setProgressBarVisibility(false);
                    }
                    //TODO handle server error
                }

                @Override
                public void onFailure(Call<Trip> call, Throwable t) {
                    Timber.d("method=onFailure");
                    TripTabFragment.getInstance().setProgressBarVisibility(false);
                }

            });
        }
    }

    /**
     * Generates missing information fro trip.
     *
     * @param locationSaves Trip's locations
     */
    private void generateMissingData(List<LocationSave> locationSaves) {
        List<LatLng> points = new ArrayList<>();

        for (LocationSave locationSave : locationSaves) {
            points.add(new LatLng(locationSave.getLatitude(), locationSave.getLongitude()));
        }

        if (points.size() > 0) {
            LatLng firstPoint = points.get(0);
            LatLng lastPoint = points.get(points.size() - 1);

            TripHelper.getAddressInfo(firstPoint, getApplicationContext(), currentTripSave, true);
            TripHelper.getAddressInfo(lastPoint, getApplicationContext(), currentTripSave, false);

            String mapboxUrl = MapboxHelper.buildMapUrl(points, firstPoint, lastPoint);
            currentTripSave.setMapboxUrl(mapboxUrl);
            currentTripSave.setFinished();

            currentTripSave.save();
        }
    }

}


