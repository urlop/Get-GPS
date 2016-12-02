package com.example.ruby.getgps.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.Trip;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.models.WorkSource;
import com.example.ruby.getgps.ui.adapters.MyWorkSourcesAdapter;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.UIHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class MyWorkSourcesActivity extends AppCompatActivity {

    protected
    @Bind(R.id.rv_work)
    RecyclerView rv_work;
    protected
    @Bind(R.id.v_progress)
    View v_progress;

    private EditText et_name;

    private Dialog dialog;
    private final List<WorkSource> list = new ArrayList<>();
    private String[] colors;
    private User user;
    private Trip trip;
    private String prevTripPupose;
    private int origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_work_sources);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            user = (User) getIntent().getSerializableExtra(Constants.TRIP_WORK_SOURCE_USER_EXTRA);
            trip = (Trip) getIntent().getSerializableExtra(Constants.TRIP_WORK_SOURCE_TRIP_EXTRA);
            origin = getIntent().getIntExtra(Constants.TRIP_WORK_SOURCE_ORIGIN_EXTRA, 0);
        }
        setupView();
        setupList();
    }

    private void setupView() {
        UIHelper.setupAlternativeStatusBar(this);

        GridLayoutManager lLayout = new GridLayoutManager(MyWorkSourcesActivity.this, 3);
        rv_work.setHasFixedSize(true);
        rv_work.setLayoutManager(lLayout);

        //DIALOG
        dialog = new Dialog(this);
        dialog.setTitle("Create Custom");
        dialog.setContentView(R.layout.dialog_work_sources_add);
        dialog.setCancelable(true);

        et_name = (EditText) dialog.findViewById(R.id.et_name);
        et_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.add || id == EditorInfo.IME_NULL) {
                    addWorkSource();
                    return true;
                }
                return false;
            }
        });

        Button bt_ws_add = (Button) dialog.findViewById(R.id.bt_ws_add);
        bt_ws_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWorkSource();
            }
        });
    }

    private void setupList() {
        String[] names = getResources().getStringArray(R.array.work_sources_names);
        TypedArray drawables = getResources().obtainTypedArray(R.array.work_sources_icons);
        colors = getResources().getStringArray(R.array.work_sources_colors);

        for (String incomeSource : user.getIncomeSources()) {
            WorkSource workSource;
            int currentAssetsIndicator = -1;
            for (int i = 0; i < names.length; i++) {
                if (names[i].equalsIgnoreCase(incomeSource)) {
                    currentAssetsIndicator = i;
                    break;
                }
            }
            if (currentAssetsIndicator > -1) {
                workSource = new WorkSource(incomeSource, drawables.getDrawable(currentAssetsIndicator), colors[currentAssetsIndicator]);
            } else {
                workSource = new WorkSource(incomeSource, null, colors[new Random().nextInt(colors.length)]);
            }
            list.add(workSource);
        }
        list.add(new WorkSource(getString(R.string.mws_none), null, colors[0]));
        drawables.recycle();

        MyWorkSourcesAdapter adapter;
        adapter = new MyWorkSourcesAdapter(list, this);
        rv_work.setAdapter(adapter);
    }

    @OnClick(R.id.fab)
    public void btnFab(View view) {
        dialog.show();
    }

    private void addWorkSource() {
        dialog.cancel();
        String name = et_name.getText().toString();
        if (!name.trim().isEmpty() && name.matches("[a-zA-Z ]+")) {
            list.add(new WorkSource(et_name.getText().toString(), null, colors[new Random().nextInt(colors.length)], true));

            //Add income source to user
            List<String> incomeSources = user.getIncomeSources();
            incomeSources.add(name);
            user.setIncomeSources(incomeSources);

            Toast.makeText(this, R.string.ws_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.ws_error_name, Toast.LENGTH_SHORT).show();
        }

        et_name.setText("");
        sendNewWorkSource();
    }

    private void returnWorkSourceString(String workSource) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TRIP_WORK_SOURCE_STRING_EXTRA, workSource);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void sendNewWorkSource() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("income_sources", user.getIncomeSources());
        Call<User> call = RequestManager.getDefault(this).updateCurrentUser(map);
        call.enqueue(new CustomCallback<User>(this, call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), R.string.ws_ok_send, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.ws_error_send, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * Finishes the activity returning all the data selected by the user to the previous activity (MainActivity)
     *
     * @param workSource
     * @see MainActivity
     */
    public void sendTripWorkSource(String workSource) {
        if (origin != Constants.ORIGIN_ADD_TRIP_ACTIVITY) {
            trip.setPurpose(getString(R.string.str_work));
            trip.setIncomeSource(workSource);
            returnNewUserInfo();
        } else {
            returnWorkSourceString(workSource);
        }
    }

    private void returnNewUserInfo() {
        Timber.d("trip.purpose=%s trip.incomeSource=%s", trip.getPurpose(), trip.getIncomeSource());
        Timber.d("previoustrip.purpose=%s", prevTripPupose);
        Intent intent = new Intent();
        intent.putExtra(Constants.TRIP_WORK_SOURCE_USER_EXTRA, user);
        intent.putExtra(Constants.TRIP_WORK_SOURCE_TRIP_EXTRA, trip);
        intent.putExtra(Constants.TRIP_WORK_SOURCE_PREVIOUS_TRIP_PURPOSE_EXTRA, prevTripPupose);
        setResult(RESULT_OK, intent);
        finish();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
