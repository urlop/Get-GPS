package com.example.ruby.getgps.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * View for showing basic statistics and settings
 *
 * @see Fragment
 */
public class HomeTabFragment extends Fragment {
    private static HomeTabFragment fragment;
    @Bind(R.id.switch_automatic_detection)
    Switch switch_automatic_detection;
    @Bind(R.id.bt_classify_personal)
    Button bt_classify_personal;
    @Bind(R.id.bt_classify_off)
    Button bt_classify_off;
    @Bind(R.id.bt_classify_work)
    Button bt_classify_work;
    @Bind(R.id.pie_chart)
    PieChart pie_chart;
    private String totalSumMiles = "";
    @Bind(R.id.tv_personal_miles)
    TextView tv_personal_miles;
    @Bind(R.id.tv_work_miles)
    TextView tv_work_miles;
    @Bind(R.id.tv_unclassified_miles)
    TextView tv_unclassified_miles;
    @Bind(R.id.tv_charity_miles)
    TextView tv_charity_miles;

    private boolean classify_personal = false, classify_off = true, classify_work = false;
    private MainActivity mainActivity;

    public static HomeTabFragment getInstance() {
        if (fragment != null) {
            return fragment;
        } else {
            fragment = new HomeTabFragment();
        }
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and non-graphical fragments can return null (which is the default implementation).
     * <p/>
     * Sets switch actions.
     * If on, StartTripService should be on to track if a trip starts. If off, that service should be stopped
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        switch_automatic_detection.setChecked(PreferencesManager.getInstance(getActivity().getApplicationContext()).getAutomatictTrackingSwitchState());

        switch_automatic_detection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferencesManager.getInstance(getActivity().getApplicationContext()).saveAutomaticTrackingSwitchState(true);
                    askPermissionsToStartTripService();
                } else {
                    PreferencesManager.getInstance(getActivity().getApplicationContext()).saveAutomaticTrackingSwitchState(false);
                    ServiceHelper.stopStartTripService();
                }
                sendAutoTracking(isChecked);
            }
        });
        checkAutoClassifyState();
        toggleClassifyButtons();
        return view;
    }

    /**
     * Asks for Permission to turn on GPS
     */
    public void askPermissionsToStartTripService() {
        if (mainActivity != null) {
            mainActivity.askPermissionsToStartTripService();
        }

    }

    private void checkAutoClassifyState() {
        String classifyState = PreferencesManager.getInstance(getContext()).getAutoClassifyState();
        Timber.d("method=checkAutoClassifyState classifyState='%s'", classifyState);
        if (classifyState.equals(getContext().getString(R.string.str_work))) {
            classify_off = false;
            classify_personal = false;
            classify_work = true;
        } else if (classifyState.equals(getString(R.string.str_personal))) {
            classify_off = false;
            classify_personal = true;
            classify_work = false;
        } else {
            classify_off = true;
            classify_personal = false;
            classify_work = false;
        }
    }

    public void setUpPieChart() {
        pie_chart.setCenterTextColor(ContextCompat.getColor(pie_chart.getContext(), R.color.colorPrimaryEv));
        pie_chart.setCenterTextSize(10f);
        pie_chart.setHoleRadius(60f);
        pie_chart.setTransparentCircleRadius(40f);
        pie_chart.setDescription("");
        pie_chart.getLegend().setEnabled(false);
        pie_chart.setDrawSliceText(false);
        pie_chart.setRotationEnabled(false);
        pie_chart.setData(generatePieData());
        pie_chart.setCenterText(generateCenterText(totalSumMiles));
        pie_chart.notifyDataSetChanged();
        pie_chart.invalidate();
    }

    @OnClick(R.id.bt_classify_work)
    protected void autoClassifyWork() {
        if (!classify_work) {
            setAutoClassify(getContext().getString(R.string.str_work), true);
        }
    }

    @OnClick(R.id.bt_classify_personal)
    protected void autoClassifyPersonal() {
        if (!classify_personal) {
            setAutoClassify(getContext().getString(R.string.str_personal), true);
        }
    }

    @OnClick(R.id.bt_classify_off)
    protected void autoClassifyOff() {
        if (!classify_off) {
            setAutoClassify(getContext().getString(R.string.str_off), true);
        }
    }

    public void setAutoClassify(String state, boolean sendChanges) {
        classify_off = false;
        classify_work = false;
        classify_personal = false;

        if (state.equalsIgnoreCase(mainActivity.getString(R.string.str_work))) {
            classify_work = true;
        } else if (state.equalsIgnoreCase(mainActivity.getString(R.string.str_personal))) {
            classify_personal = true;
        } else {
            classify_off = true;
        }

        PreferencesManager.getInstance(getContext()).saveAutoClassifyState(state);

        if(sendChanges) {
            sendAutoClassify(state);
        }

        if (TripTabFragment.getInstance().getNewTripCardsAdapter() != null) {
            TripTabFragment.getInstance().getNewTripCardsAdapter().announceDataHasChanged();
        }
        toggleClassifyButtons();
    }

    private void toggleClassifyButtons() {
        bt_classify_personal.setEnabled(!classify_personal);
        bt_classify_personal.setTextColor(classify_personal ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        bt_classify_work.setEnabled(!classify_work);
        bt_classify_work.setTextColor(classify_work ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        bt_classify_off.setEnabled(!classify_off);
        bt_classify_off.setTextColor(classify_off ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));
    }

    private void sendAutoTracking(boolean autoTracking) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("auto_detect_enabled", autoTracking);
        sendUserChange(map);
    }

    private void sendAutoClassify(String newAutoClassify) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("auto_classify", newAutoClassify);
        sendUserChange(map);
    }

    private void sendUserChange(HashMap<String, Object> map) {
        Call<User> call = RequestManager.getDefault(getContext()).updateCurrentUser(map);
        call.enqueue(new CustomCallback<User>(getContext(), call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    Timber.d("method=sendUserChange result='Home change sent'");
                }
            }
        });
    }

    public void refreshView(String autoClassify, boolean automaticDetection) {
        setAutoClassify(autoClassify, false);
        switch_automatic_detection.setChecked(automaticDetection);
        setUpPieChart();
    }

    /**
     * @return switch for automatic detection
     */
    public Switch getSwitch_automatic_detection() {
        return switch_automatic_detection;
    }

    private PieData generatePieData() {
        ArrayList<Entry> usersMiles = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        xVals.add(getResources().getString(R.string.str_charity));
        xVals.add(getResources().getString(R.string.str_unclassified));
        xVals.add(getResources().getString(R.string.str_personal));
        xVals.add(getResources().getString(R.string.str_work));

        float charityMiles = Float.parseFloat(mainActivity.user.getTotalCharityMiles()) + Float.parseFloat(mainActivity.user.getTotalMedicalMiles());
        float unclassifiedMiles = Float.parseFloat(mainActivity.user.getTotalUncategorizedMiles());
        float personalMiles = Float.parseFloat(mainActivity.user.getTotalPersonalMiles());
        float workMiles = Float.parseFloat(mainActivity.user.getTotalBusinessMiles());
        float totalMiles = charityMiles + unclassifiedMiles + personalMiles + workMiles;
        Timber.d("method=generatePieData charityMiles=%f unclassMiles=%f personalMiles=%f workMiles=%f", charityMiles, unclassifiedMiles, personalMiles, workMiles);
        usersMiles.add(new Entry(charityMiles, 0));
        usersMiles.add(new Entry(unclassifiedMiles, 1));
        usersMiles.add(new Entry(personalMiles, 2));
        usersMiles.add(new Entry(workMiles, 3));

        DecimalFormat oneDigit = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));//format to 1 decimal place
        totalMiles = Float.valueOf(oneDigit.format(totalMiles));
        Timber.d("method=generatePieData totalMiles=%f", totalMiles);
        tv_charity_miles.setText(String.format("%s", Float.valueOf(oneDigit.format(usersMiles.get(0).getVal()))));
        tv_unclassified_miles.setText(String.format("%s", Float.valueOf(oneDigit.format(usersMiles.get(1).getVal()))));
        tv_personal_miles.setText(String.format("%s", Float.valueOf(oneDigit.format(usersMiles.get(2).getVal()))));
        tv_work_miles.setText(String.format("%s", Float.valueOf(oneDigit.format(usersMiles.get(3).getVal()))));

        totalSumMiles = totalMiles < 1000 ? String.valueOf(totalMiles) : getString(R.string.distance_kilo, (int) (totalMiles / 1000));
        PieDataSet ds1 = new PieDataSet(usersMiles, "");
        ds1.setColors(new int[]{R.color.pie_chart_orange, R.color.pie_chart_grey, R.color.pie_chart_blue, R.color.pie_chart_green}, pie_chart.getContext());
        ds1.setSliceSpace(3f);
        ds1.setValueTextColor(Color.WHITE);
        ds1.setValueTextSize(10f);
        ds1.setDrawValues(false);
        PieData d = new PieData(xVals, ds1);

        return d;
    }

    private SpannableString generateCenterText(String totalSumMiles) {
        SpannableString s = new SpannableString(totalSumMiles);
        s.setSpan(new RelativeSizeSpan(2.1f), 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return s;
    }
}

