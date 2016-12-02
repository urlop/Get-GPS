package com.example.ruby.getgps.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.TimeHelper;
import com.example.ruby.getgps.utils.TripHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;


public class EditTripActivity extends AppCompatActivity {

    private Trip trip;
    private boolean tripWork = true, tripPersonal = false, tripCharity = false, tripMedical = false;
    private final Calendar myCalendar = Calendar.getInstance();
    @Bind(R.id.et_miles)
    EditText et_miles;
    @Bind(R.id.tv_deduction)
    TextView tv_deduction;
    @Bind(R.id.et_date_picker)
    EditText et_date_picker;
    @Bind(R.id.et_from)
    EditText et_from;
    @Bind(R.id.et_to)
    EditText et_to;
    @Bind(R.id.tv_start_time)
    TextView tv_start_time;
    @Bind(R.id.tv_end_time)
    TextView tv_end_time;
    @Bind(R.id.tv_delta_time)
    TextView tv_delta_time;
    @Bind(R.id.et_add_note)
    EditText et_add_note;
    @Bind(R.id.iv_map)
    ImageView iv_map;
    @Bind(R.id.date_text_input_layout)
    TextInputLayout date_text_input_layout;
    @Bind(R.id.delete_trip)
    ImageView delete_trip;

    private DatePickerDialog.OnDateSetListener date;
    private int itemViewHolderPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setUpDateField();

        Intent intent = getIntent();
        if (intent != null) {
            trip = (Trip) intent.getSerializableExtra(Constants.TRIP_OBJECT_EXTRA);
            itemViewHolderPosition = intent.getIntExtra(Constants.VIEW_HOLDER_POSITION, 0);
            if (trip != null) {
                populateWithTripData();
                setTitle(getString(R.string.title_edit_trip));
            }
        }
    }

    private void populateWithTripData() {
        et_miles.setText(String.valueOf(trip.getMiles()));
        et_from.setText(trip.getFrom());
        et_to.setText(trip.getTo());
        tv_deduction.setText(getString(R.string.money, trip.getDeduction()));
        et_add_note.setText(trip.getNotes());
        tv_start_time.setText(TimeHelper.dateTo12TimeFormat(trip.getStartedAt()));
        tv_end_time.setText(TimeHelper.dateTo12TimeFormat(trip.getEndedAt()));
        tv_delta_time.setText(String.valueOf(TimeHelper.getTimeBetween(trip.getStartedAt(), trip.getEndedAt())));
        if (trip.getMapboxUrl() != null && !trip.getMapboxUrl().isEmpty()) {
            Picasso.with(this).load(trip.getMapboxUrl()).error(R.drawable.bubble_shadow).fit().centerCrop().into(iv_map);
        }
        Timber.d("method=populateWithTripData trip='%s'", trip.getDateFormatted());
        et_date_picker.setText(trip.getDateFormatted());
    }


    private void setUpDateField() {
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (!myCalendar.after(Calendar.getInstance())) {
                    updateDatePickerField();
                } else {
                    Toast.makeText(EditTripActivity.this, getResources().getString(R.string.et_invalid_date), Toast.LENGTH_LONG).show();
                    date_text_input_layout.setError(getResources().getString(R.string.et_invalid_date));
                    et_date_picker.setText(trip.getDateFormatted());
                }
            }

        };
        myCalendar.setTime(Calendar.getInstance().getTime());
        updateDatePickerField();
    }

    private void updateDatePickerField() {
        String myFormat = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        et_date_picker.setText(sdf.format(myCalendar.getTime()));
    }

    @OnClick(R.id.et_date_picker)
    protected void onDatepicker() {
        new DatePickerDialog(EditTripActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @OnClick(R.id.upload_trip)
    protected void uploadTrip() {
        Trip trip = createNewTripObject();
        Call<Trip> call = RequestManager.getDefault(this).updateTrip(this.trip.getTokenId(), trip);
        call.enqueue(new CustomCallback<Trip>(this, call) {
            @Override
            public void onResponse(Call<Trip> call, Response<Trip> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    TripTabFragment.getInstance().filterAllTrips();
                    onBackPressed();
                }
            }
        });
    }

    @OnClick(R.id.delete_trip)
    protected void deleteTrip() {
        TripHelper.deleteTrip(this, this.trip, true, itemViewHolderPosition);
    }

    private Trip createNewTripObject() {
        boolean tripMiles, tripDate;
        String date = "";
        if (et_miles.getText().toString().isEmpty()) {
            et_miles.setError(getResources().getString(R.string.et_error_add_trip));
            tripMiles = false;
        } else {
            tripMiles = true;
        }
        if (et_date_picker.getText().toString().isEmpty()) {
            date_text_input_layout.setError(getResources().getString(R.string.et_error_add_trip));
            tripDate = false;
        } else {
            tripDate = true;
            date = et_date_picker.getText().toString();
        }
        if (tripMiles && tripDate) {
            float miles = Float.parseFloat(et_miles.getText().toString());
            String from = et_from.getText().toString();
            String to = et_to.getText().toString();
            String notes = et_add_note.getText().toString();
            Trip trip = this.trip;
            trip.setMiles(miles);
            trip.setFrom(from);
            trip.setTo(to);
            Timber.d("setting formatted date '%s'", date);
            trip.setDate(date);
            trip.setDateFormatted("");
            trip.setNotes(notes);
            return trip;
        }
        return null;
    }


    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
